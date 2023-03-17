package com.test.jenkins.devops

import com.test.jenkins.utils.tool

def Build(config) {
  switch (config.project_cfg.build_type) {
    case 'maven' :
      this.MavenBuild(config)
      break
    case 'node' :
      this.NodeBuild(config)
      break
  }
}

def MavenBuild(config) {
  def tool = new tool()
  def build_cmd = config.project_cfg.build_cmd
  
  // 清除maven缓存
  CleanM2()

  tool.printMessage('编译程序', 'green')

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
  def tool = new tool()

  if (config.info.Module == 'delete') {

    tool.printMessage('安装依赖', 'green')

    try {
      ansiColor('xterm') {
        sh """
          # change script mode from debugging to command logging
          set +x +v

          [ -s /usr/local/nvm/nvm.sh ] && . /usr/local/nvm/nvm.sh

          nvm use ${NodeVer}

          set -x +v

          npm config set registry ${config.project_cfg.npm_registry}

          ${InstallCMD}

          # reset the shell to debugging mode

        """
      }
    }catch (Exception err) {
      echo "${err}"
      currentBuild.result = 'FAILURE'
      throw err
    }
  }

  util.printMessage('编译程序', 'green')
  try {
    sh """
      # change script mode from debugging to command logging
      set +x +v

      [ -s /usr/local/nvm/nvm.sh ] && . /usr/local/nvm/nvm.sh

      nvm use ${NodeVer}
      set -x +v

      npm config set registry ${config.project_cfg.npm_registry}
      ${BuildCMD}

      # reset the shell to debugging mode

    """
  }catch (Exception err) {
    currentBuild.result = 'FAILURE'
    throw err
  }
}

// CleanM2 删除指定的路径的maven 缓存
def CleanM2() {
  def tool = new tool()
  tool.printMessage('删除 Maven 缓存', 'green')

  def cleanJar = "${ CleanJar }"

  if (cleanJar.length() > 1 ) {
    String[] jars = cleanJar.split('\n')

    for (jar in jars) {
      file = '/root/.m2/repository/' + jar

      if ( file.lastIndexOf('.jar') > 0 ) {
        sh """
          rm -rf ${file}
        """
      }else {
        sh """
          rm -rf ${file}
        """
      }

      tool.printMessage(file, 'green')
    }

  }else {
    tool.printMessage('没有需要清除的Maven 缓存', 'green')
  }
}
