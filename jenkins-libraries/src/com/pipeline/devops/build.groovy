package com.pipeline.devops

import com.data.devops.utils

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
  def cmd = config.info.CMD
  
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

  // 清除maven缓存
  CleanM2()

  util.printMessage('编译程序', 'green')
  try {
    ansiColor('xterm') {
      sh  """
    ${cmd}
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

          set -x +v

          npm config set registry http://npm.data.com

          ${install_cmd}

          # reset the shell to debugging mode

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
      set -x +v

      npm config set registry http://npm.data.com
      ${build_cmd}

      # reset the shell to debugging mode

    """
  }catch (err) {
    throw err
  }
}

// CleanM2 删除指定的路径的maven 缓存
def CleanM2() {
  def util = new utils()
  util.printMessage('删除 Maven 缓存', 'green')

  def cleanJar = "${ CleanJar }"

  if (cleanJar.length() > 1 ) {
    String[] jars = cleanJar.split('\n')

    for (jar in jars) {
      file = '/root/.m2/repository/' + jar

      if ( file.lastIndexOf('.jar') > 0 ) {
        // fileOperations([
        // fileDeleteOperation(includes : file)])
        sh """
          rm -rf ${file}
        """
      }else {
        // fileOperations([
        // folderDeleteOperation(folderPath: file)])

        sh """
          rm -rf ${file}
        """
      }

      util.printMessage(file, 'green')
    }
  }else {
    util.printMessage('没有需要清除的Maven 缓存', 'green')
  }
}
