// Use the project-local Node runtime so the system Node installation can stay as-is.
const { existsSync } = require('node:fs');
const { resolve } = require('node:path');
const { spawn } = require('node:child_process');
const root = resolve(__dirname, '..');
const localNode = resolve(root, 'node_modules/node/bin', process.platform === 'win32' ? 'node.exe' : 'node');
const child = spawn(existsSync(localNode) ? localNode : process.execPath,
  [resolve(root, 'node_modules/vite/bin/vite.js'), ...process.argv.slice(2)],
  { cwd: root, stdio: 'inherit' });
child.on('error', error => { console.error(error.message); process.exitCode = 1; });
child.on('exit', code => { process.exitCode = code == null ? 1 : code; });
