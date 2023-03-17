
package com.test.jenkins.devops

import  com.test.jenkins.utils.tool

def deploy(config) {

  switch (config.project_cfg.build_type) {
    case 'maven':
      this.be_backPackage(config).executePlaybook(config)
      // this.executePlaybook(config)
      break
    case 'node' :
      this.fe_backPackage(config)
      this.executePlaybook(config)
      break
  }
}



def backPackage(config){
  if (config.project_cfg.build_type == "maven"){
    def backdir = config.project_cfg.backdir
    def jobName = config.project_cfg.JOB_NAME
    def tmplist = config.project_cfg.outputDirectory.split('\\.')
    def suffix = tmplist[tmplist.length - 1]
    def appName = config.project_cfg.BUILD_ID + '.' + suffix
    config.project_cfg.appName = appName

    sh """
      [ ! -d ${backdir}/${jobName} ] && mkdir -p ${backdir}/${jobName} || echo "已存在"
      [ ! -d tmpdir ] && mkdir -p tmpdir || echo "已存在"
    
      cp -f ${outpath} tmpdir/${appName}
      cp -f ${outpath} ${backdir}/${jobName}/${appName}
    """
  }else {
    def backdir = config.project_cfg.backdir
    def jobName = config.project_cfg.JOB_NAME
    def outpath = config.project_cfg.outputDirectory
    def appName = config.project_cfg.BUILD_ID + '.tgz'

    config.project_cfg.appName = appName

    sh """
    [ ! -d ${backdir}/${jobName} ] && mkdir -p ${backdir}/${jobName} || echo "已存在"
    [ ! -d tmpdir ] && mkdir -p tmpdir || echo "已存在"
    """

    sh """
    tar zcvf tmpdir/${appName}  ${outpath}
    cp -f tmpdir/${appName} ${backdir}/${jobName}/
    """
  }

  return config
}
def executePlaybook(config) {

  def tool = new tool()

  if (config.project_cfg.build_type == "maven"){
    
    def isconfig = false
    def configfile = ''

    if (config.project_cfg.config == ""){
      tool.printMessage( '没有指定项目配置文件,不会推送配置文件', 'green')
    }
    else {

      if ( config.project_cfg.project_name == 'scm' ) {
        configfile = config.project_cfg.project_name + '/' + config.project_cfg.project_group + '/' + config.project_cfg.project_app + '/' + "${ENVIR}" + '/' + config.project_cfg.config
      }else if ( config.project_cfg.project_name == 'feilong' ) {
        configfile =  config.project_cfg.project_name + '/' + "${ENVIR}" + '/' + config.project_cfg.config
      } else {
        configfile =   'feilong/' + "${ENVIR}" + '/' + config.project_cfg.config
      }

      isconfig =  true
    }

    def jvm_opt = ''
    if ( config.project_cfg.option["${ENVIR}"].jvm != "" ){
      jvm_opt = config.project_cfg.option["${ENVIR}"].jvm
    }

    // def debugPort = "${ DebugPort }"
    if ( "${ DebugPort }".length() > 1 ) {
      jvm_opt = jvm_opt + " -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${ DebugPort }"
    }

    ansiColor('xterm') {
      ansiblePlaybook(
        credentialsId: project_cfg.ssh_credentialsId,
        playbook: config.project_cfg.playbookPath,
        installation: 'ansible-playbook',
        inventory: config.project_cfg.inventory,
        colorized: true,
        sudoUser: null,
        extraVars: [
            appname: config.project_cfg.JOB_NAME,
            hostname: "${ENVIR}" + '-' + config.project_cfg.JOB_NAME,
            filename: config.project_cfg.appName,
            dport: config.project_cfg.port,
            env: "${ENVIR}",
            config: configfile,
            jvm_opt: jvm_opt,
            isconfig: isconfig,
            ]
      )
    }

  }else{
    ansiColor('xterm') {
      ansiblePlaybook(
        credentialsId: config.project_cfg.ssh_credentialsId,
        playbook: config.project_cfg.playbookPath,
        installation: 'ansible-playbook',
        inventory: config.project_cfg.inventory,
        colorized: true,
        sudoUser: null,
        extraVars: [
          appname: config.project_cfg.JOB_NAME,
          hostname: "${ENVIR}" + '-' + config.project_cfg.JOB_NAME,
          filename: config.project_cfg.appName,
          env: "${ENVIR}",
        ]
      )
    }
  }

  return config
}