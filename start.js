const { spawn, execSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const http = require('http');
const net = require('net');

// ---------- 配置 ----------
const PROJECT_DIR = __dirname;
const NACOS_HOME = path.join(PROJECT_DIR, 'nacos');
const LOG_DIR = process.env.TEMP || '/tmp';

const LOGS = {
  nacos: path.join(LOG_DIR, 'nacos.log'),
  auth: path.join(LOG_DIR, 'auth-service.log'),
  backend: path.join(LOG_DIR, 'backend.log'),
  gateway: path.join(LOG_DIR, 'gateway.log'),
  frontend: path.join(LOG_DIR, 'frontend.log'),
};

const PORTS = {
  nacos: 8848,
  auth: 8083,
  backend: 8081,
  gateway: 8080,
  frontend: 5173,
};

// ---------- 辅助函数 ----------
function log(msg, color = '\x1b[36m') { console.log(`${color}${msg}\x1b[0m`); }
function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

function checkPort(port, timeout = 2000) {
  return new Promise(resolve => {
    const socket = new net.Socket();
    const timer = setTimeout(() => { socket.destroy(); resolve(false); }, timeout);
    socket.on('connect', () => { clearTimeout(timer); socket.destroy(); resolve(true); });
    socket.on('error', () => { clearTimeout(timer); resolve(false); });
    socket.connect(port, '127.0.0.1');
  });
}

async function waitForNacos(port, maxRetries = 90) {
  for (let i = 1; i <= maxRetries; i++) {
    await sleep(1000);
    try {
      const resp = await new Promise((resolve, reject) => {
        const req = http.request({ hostname: '127.0.0.1', port, path: '/nacos', method: 'HEAD', timeout: 2000 }, resolve);
        req.on('error', reject);
        req.end();
      });
      if (resp.statusCode === 302 || resp.statusCode === 200) {
        log(`  Nacos 就绪 (${i}s)`);
        return true;
      }
    } catch (_) {}
  }
  return false;
}

function tailLogFile(filePath, lines = 30) {
  if (!fs.existsSync(filePath)) return '日志文件不存在';
  try {
    const content = fs.readFileSync(filePath, 'utf8');
    const linesArr = content.split('\n').filter(l => l.trim());
    return linesArr.slice(-lines).join('\n');
  } catch (_) { return '无法读取日志'; }
}

function startDetached(cmd, args, cwd, logFile, showOutput = true, env = {}) {
  const logStream = fs.createWriteStream(logFile, { flags: 'a' });
  const proc = spawn(cmd, args, {
    cwd,
    stdio: ['ignore', 'pipe', 'pipe'],
    detached: true,
    shell: true,
    env: { ...process.env, ...env }
  });
  proc.stdout.pipe(logStream);
  proc.stderr.pipe(logStream);
  if (showOutput) {
    proc.stdout.pipe(process.stdout);
    proc.stderr.pipe(process.stderr);
  }
  proc.unref();
  proc.on('exit', (code) => {
    if (code !== 0) {
      log(`  进程退出，退出码: ${code}`, '\x1b[31m');
      log(`  日志尾部 (${logFile}):`, '\x1b[33m');
      console.log(tailLogFile(logFile));
    }
  });
  return proc;
}

async function startServiceAndWait(name, cmd, args, cwd, logFile, port, maxWait = 60, env = {}) {
  log(`启动 ${name} (端口 ${port})`);
  log(`  命令: ${cmd} ${args.join(' ')}`, '\x1b[90m');
  const proc = startDetached(cmd, args, cwd, logFile, true, env);
  log(`  PID: ${proc.pid}`);
  for (let i = 0; i < maxWait; i++) {
    await sleep(1000);
    if (await checkPort(port, 500)) {
      log(`  ${name} 就绪 (${i+1}s)`);
      return true;
    }
  }
  log(`  ${name} 超时 (${maxWait}s)`, '\x1b[31m');
  process.exit(1);
}

// ---------- 查找命令（强制优先项目内） ----------
function findCmd(cmd) {
  const isWin = process.platform === 'win32';
  if (cmd === 'mvn') {
    const mvnw = path.join(PROJECT_DIR, isWin ? 'mvnw.cmd' : 'mvnw');
    if (fs.existsSync(mvnw)) return mvnw;
    const localMvn = path.join(PROJECT_DIR, 'maven', 'bin', isWin ? 'mvn.cmd' : 'mvn');
    if (fs.existsSync(localMvn)) return localMvn;
    log(`  错误: 未找到项目内的 Maven (mvnw 或 maven/bin/mvn)`, '\x1b[31m');
    return null;
  }
  if (cmd === 'npm') {
    const localNpm = path.join(PROJECT_DIR, 'node', isWin ? 'npm.cmd' : 'npm');
    if (fs.existsSync(localNpm)) {
      log(`  使用项目内 npm: ${localNpm}`);
      return localNpm;
    }
    log(`  错误: 未找到项目内的 npm (node/npm.cmd)`, '\x1b[31m');
    return null;
  }
  return null;
}

// ---------- 检查并安装前端依赖 ----------
async function ensureFrontendDependencies(npmCmd, frontendDir) {
  const nodeModules = path.join(frontendDir, 'node_modules');
  if (fs.existsSync(nodeModules)) {
    log('  前端依赖已存在，跳过安装');
    return true;
  }
  log('  前端依赖未安装，正在执行 npm install ...');
  return new Promise((resolve) => {
    const proc = spawn(npmCmd, ['install'], {
      cwd: frontendDir,
      stdio: 'inherit',
      shell: true,
    });
    proc.on('exit', (code) => {
      if (code === 0) {
        log('  npm install 完成');
        resolve(true);
      } else {
        log(`  npm install 失败，退出码: ${code}`, '\x1b[31m');
        resolve(false);
      }
    });
  });
}

// ---------- 主流程 ----------
(async () => {
  log('============================================', '\x1b[36m');
  log('  图书馆管理系统 - 顺序启动 (完全项目内依赖)', '\x1b[36m');
  log('============================================', '\x1b[36m');

  // 检查 Java
  try { execSync('java -version', { stdio: 'ignore' }); log('  Java 可用'); } catch (_) { log('  错误: 未找到 Java', '\x1b[31m'); process.exit(1); }

  // ---------- 1. Nacos (使用 java -jar) ----------
  log('[1/5] 检查 Nacos...');
  if (await checkPort(PORTS.nacos)) {
    log(`  Nacos 已运行 (端口 ${PORTS.nacos})`);
  } else {
    log('  启动 Nacos (java -jar) ...');
    const jarFile = path.join(NACOS_HOME, 'target', 'nacos-server.jar');
    if (!fs.existsSync(jarFile)) {
      log(`  错误: 找不到 ${jarFile}`, '\x1b[31m');
      process.exit(1);
    }
    const clusterConf = path.join(NACOS_HOME, 'conf', 'cluster.conf');
    if (fs.existsSync(clusterConf)) fs.unlinkSync(clusterConf);

    const javaArgs = [
      '--add-opens=java.base/java.io=ALL-UNNAMED',
      '--add-opens=java.base/java.lang=ALL-UNNAMED',
      '--add-opens=java.base/java.util=ALL-UNNAMED',
      '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED',
      '--add-opens=java.base/sun.net.www=ALL-UNNAMED',
      '-Xms512m', '-Xmx512m', '-Xmn256m',
      '-Dnacos.standalone=true',
      `-Dnacos.home=${NACOS_HOME}`,
      '-Dnacos.member.list=127.0.0.1:8848',
      '-jar', jarFile,
      `--spring.config.additional-location=file:${path.join(NACOS_HOME, 'conf/')}`,
      `--logging.config=${path.join(NACOS_HOME, 'conf/nacos-logback.xml')}`,
      '--nacos.member.list=127.0.0.1:8848',
      '--nacos.server.mode=standalone'
    ];
    const proc = startDetached('java', javaArgs, PROJECT_DIR, LOGS.nacos, true);
    log(`  Nacos PID: ${proc.pid}`);
    log('  等待 Nacos 就绪 (最长 90 秒)...');
    if (!await waitForNacos(PORTS.nacos)) {
      log(`  错误: Nacos 启动超时`, '\x1b[31m');
      process.exit(1);
    }
  }

  // ---------- 查找 mvn 和 npm（仅项目内） ----------
  const mvn = findCmd('mvn');
  if (!mvn) { log('  错误: 找不到项目内的 Maven', '\x1b[31m'); process.exit(1); }
  log(`  Maven: ${mvn}`);

  const npm = findCmd('npm');
  if (!npm) { log('  错误: 找不到项目内的 npm (node/npm.cmd)', '\x1b[31m'); process.exit(1); }
  log(`  npm: ${npm}`);

  // ---------- 2~5 顺序启动 ----------
  const services = [
    { name: 'auth-service', port: PORTS.auth, cmd: mvn, args: ['-pl', 'auth-service', 'spring-boot:run', '-q', '-DskipTests', '-e', '-Dlombok.disableJavacChecks=true'], cwd: PROJECT_DIR, log: LOGS.auth },
    { name: 'backend', port: PORTS.backend, cmd: mvn, args: ['-pl', 'backend', 'spring-boot:run', '-q', '-DskipTests', '-e', '-Dlombok.disableJavacChecks=true'], cwd: PROJECT_DIR, log: LOGS.backend },
    { name: 'gateway', port: PORTS.gateway, cmd: mvn, args: ['-pl', 'gateway', 'spring-boot:run', '-q', '-DskipTests', '-e', '-Dlombok.disableJavacChecks=true'], cwd: PROJECT_DIR, log: LOGS.gateway },
    { name: 'frontend', port: PORTS.frontend, cmd: npm, args: ['run', 'dev', '--', '--host', '0.0.0.0'], cwd: path.join(PROJECT_DIR, 'frontend'), log: LOGS.frontend },
  ];

  // 启动所有服务（除前端外）先启动
  for (let i = 0; i < services.length - 1; i++) {
    const svc = services[i];
    log(`[${i+2}/5] 启动 ${svc.name}...`);
    await startServiceAndWait(svc.name, svc.cmd, svc.args, svc.cwd, svc.log, svc.port, 60);
  }

  // ---------- 前端特殊处理：先检查依赖 ----------
  const frontendSvc = services[services.length - 1];
  log(`[5/5] 准备启动 ${frontendSvc.name}...`);
  const frontendDir = frontendSvc.cwd;
  // 检查依赖
  const depsOk = await ensureFrontendDependencies(npm, frontendDir);
  if (!depsOk) {
    log('  前端依赖安装失败，退出', '\x1b[31m');
    process.exit(1);
  }
  // 启动前端
  await startServiceAndWait(
    frontendSvc.name,
    frontendSvc.cmd,
    frontendSvc.args,
    frontendSvc.cwd,
    frontendSvc.log,
    frontendSvc.port,
    60
  );

  log('');
  log('============================================', '\x1b[36m');
  log('  所有服务已启动成功！', '\x1b[36m');
  log(`  前端: http://localhost:${PORTS.frontend}`, '\x1b[33m');
  log(`  网关: http://localhost:${PORTS.gateway}`, '\x1b[33m');
  log('  账号: admin / admin123', '\x1b[33m');
  log('============================================', '\x1b[36m');
  process.exit(0);
})();