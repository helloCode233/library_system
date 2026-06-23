const { spawn } = require('child_process');
const path = require('path');

const NACOS_HOME = path.join(__dirname, 'nacos');
const jarFile = path.join(NACOS_HOME, 'target', 'nacos-server.jar');

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

console.log(`启动命令: java ${javaArgs.join(' ')}`);
const proc = spawn('java', javaArgs, { stdio: 'inherit' });
proc.on('exit', code => console.log(`退出码: ${code}`));