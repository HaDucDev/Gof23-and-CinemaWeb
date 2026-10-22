const { existsSync } = require('node:fs');
const { resolve, dirname, delimiter } = require('node:path');
const { spawnSync } = require('node:child_process');
const root = resolve(__dirname, '..');
const runtime = resolve(root, 'node_modules/node/bin', process.platform === 'win32' ? 'node.exe' : 'node');
const cli = resolve(root, 'node_modules/npm/bin/npm-cli.js');
const args = process.argv.slice(2);
if (args.includes('-g') || args.includes('--global')) {
  console.error('This launcher is for project-local npm commands only.');
  process.exit(1);
}
if (args[0] === 'ci') {
  console.error('Use install with this launcher: local Node/npm are stored in node_modules.');
  process.exit(1);
}
if (!existsSync(runtime) || !existsSync(cli)) {
  console.log('Installing project-local Node.js and npm...');
  const setup = spawnSync(process.execPath, [resolve(__dirname, 'setup.cjs'), ...args.filter(arg => arg.startsWith('--offline'))], { cwd: root, stdio: 'inherit' });
  if (setup.status !== 0) process.exit(setup.status || 1);
  if (!args.length || args[0] === 'install' || args[0] === 'ci') process.exit(0);
}
const env = { ...process.env, PATH: [dirname(runtime), resolve(root, 'node_modules/.bin'), process.env.PATH].join(delimiter), npm_config_cache: resolve(root, '.npm-cache'), npm_config_prefix: resolve(root, '.npm-global'), npm_config_global: 'false', npm_config_userconfig: resolve(__dirname, 'npm-user.conf'), npm_config_globalconfig: resolve(__dirname, 'npm-global.conf') };
const result = spawnSync(runtime, [cli, ...(args.length ? args : ['--version'])], { cwd: root, stdio: 'inherit', env });
if (result.error) console.error(result.error.message);
process.exit(result.status || (result.error ? 1 : 0));
