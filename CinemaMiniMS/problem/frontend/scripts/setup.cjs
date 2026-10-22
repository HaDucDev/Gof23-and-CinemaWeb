// The system npm only bootstraps local tools. All subsequent installs use local Node and npm.
const { existsSync } = require('node:fs');
const { resolve, dirname, delimiter } = require('node:path');
const { spawnSync } = require('node:child_process');
const root = resolve(__dirname, '..');
const localNode = resolve(root, 'node_modules/node/bin', process.platform === 'win32' ? 'node.exe' : 'node');
const localNpm = resolve(root, 'node_modules/npm/bin/npm-cli.js');
const bootstrapNpm = [process.env.npm_execpath,
  resolve(dirname(process.execPath), 'node_modules/npm/bin/npm-cli.js'),
  ...(process.env.PATH || '').split(delimiter).map(folder => resolve(folder, 'node_modules/npm/bin/npm-cli.js')),
].find(candidate => candidate && existsSync(candidate));
const env = { ...process.env, npm_config_cache: resolve(root, '.npm-cache'), npm_config_prefix: resolve(root, '.npm-global'), npm_config_global: 'false', npm_config_userconfig: resolve(__dirname, 'npm-user.conf'), npm_config_globalconfig: resolve(__dirname, 'npm-global.conf') };
function install(runtime, cli) {
  const result = spawnSync(runtime, [cli, 'install', '--no-audit', '--no-fund', ...process.argv.slice(2)], { cwd: root, stdio: 'inherit', env });
  if (result.status !== 0) process.exit(result.status || 1);
}
if (!existsSync(localNode) || !existsSync(localNpm)) {
  if (!bootstrapNpm) { console.error('Cannot find bootstrap npm. Run: npm run setup'); process.exit(1); }
  install(existsSync(localNode) ? localNode : process.execPath, bootstrapNpm);
}
install(localNode, localNpm);
