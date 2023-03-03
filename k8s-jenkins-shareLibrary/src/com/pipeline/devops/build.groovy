package com.pipeline.devops

import com.data.devops.utils
import com.data.tools.tools

def MyBuild(config) {
  def type = config.info.type

  switch (type) {
    case 'maven':
      // println('GetCode -->' + type)
      // println('GetCode -->' + config.info.build_cmd)
      this.MavenBuild(config)
      break
    case 'node' :
      // println('Build -->' + type)
      this.NodeBuild(config)
      break
  }
}

def MavenBuild(config) {
  def util = new utils()
  def build_cmd = config.info.CMD
  /*
  util.printMessage('强制更新本地缓存', 'green')
  try {
    sh  '''
    mvn dependency:purge-local-repository
    '''
  }catch (err) {
    throw err
  }
  */
  util.printMessage('编译程序', 'green')
  try {
    ansiColor('xterm') {
      sh  """
      ${build_cmd}
      """
    }
  }catch (err) {
    throw err
  }
}

def NodeBuild(config) {
  def util = new utils()
  def install_cmd = config.info.install_cmd
  def build_cmd = config.info.build_cmd
  def nodejs = config.info.node_ver

  if (config.info.Module == 'delete') {
    util.printMessage('安装依赖', 'green')

    try {
      ansiColor('xterm') {
        sh """
          # change script mode from debugging to command logging
          set +x +v

          [ -s /usr/local/nvm/nvm.sh ] && . /usr/local/nvm/nvm.sh

          nvm use ${nodejs}
          npm config set registry http://npm.data.com
          sentrycli_cdnurl=https://npm.taobao.org/mirrors/sentry-cli/
          ${install_cmd}

          # reset the shell to debugging mode
          set -x +v

        """
      }
    }catch (err) {
      echo "${err}"
      sh 'exit 1'
    }
  }

  util.printMessage('编译程序', 'green')
  try {
    sh """
      # change script mode from debugging to command logging
      set +x +v

      [ -s /usr/local/nvm/nvm.sh ] && . /usr/local/nvm/nvm.sh

      nvm use ${nodejs}
      npm config set registry http://npm.data.com
      ${build_cmd}

      # reset the shell to debugging mode
      set -x +v
    """
  }catch (err) {
    throw err
  }
}
