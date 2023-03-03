package com.pipeline.devops

import com.data.devops.utils

def deploy(config) {
  def type = config.info.type

  switch (type) {
    case 'maven':
      this.be_backPackage(config)
      this.executePlaybook(config)
      break
    case 'node' :
      this.fe_backPackage(config)
      this.executePlaybook(config)
      break
  }
}

/**
 * 执行ansible剧本
 * @param params
 * @param playbookPath
 * @param inventoryPath
 * @return
 */
def executePlaybook(config) {
  def type = config.info.type
  def configfile = ''

  def util = new utils()

  if ( config.info.project_name == 'scm' ) {
    configfile = config.info.project_name + '/' + config.info.project_group + '/' + config.info.project_app + '/' + config.info.ENVIR + '/' + config.info.config
  }else if ( config.info.project_name == 'feilong' ) {
    configfile =  config.info.project_name + '/' + config.info.ENVIR + '/' + config.info.config
  } else {
    configfile =   'feilong/' + config.info.ENVIR + '/' + config.info.config
  }

  util.printMessage("配置文件: "+configfile, 'green')

  try {
    switch (type) {
      case 'maven' :
      
        def jvm_opts = ''
        if ( config.info.jvm_opt[0] != '-' ) {

          def jvm = config.info.jvm_opt[0]

          if ( jvm[config.info.ENV] == null ) {
            jvm_opts = ''
          }else {
              jvm_opts = jvm[config.info.ENV]
          }
        }else {
            jvm_opts = config.info.jvm_opt
          
        }

        def debugPort = "${ DebugPort }"
        if (debugPort.length() > 1 ) {
          jvm_opts = jvm_opts + " -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address="+ debugPort
        }

        ansiColor('xterm') {
            ansiblePlaybook(
              credentialsId: 'skey',
              playbook: config.info.playbookPath,
              installation: 'ansible-playbook',
              inventory: '/data/deploy/playbook/inventory',
              colorized: true,
              sudoUser: null,
              extraVars: [
                  appname: config.jobName,
                  hostname: config.info.ENVIR + '-' + config.info.jobName,
                  filename: config.info.appName,
                  dport: config.info.port,
                  env: config.info.ENVIR,
                  config: configfile,
                  jvm_opt: jvm_opts,
                  isconfig: config.info.isconfig,
                  ]
          )
        }
        break
      case 'node' :

        ansiColor('xterm') {
          ansiblePlaybook(
            credentialsId: 'skey',
            playbook: config.info.playbookPath,
            installation: 'ansible-playbook',
            inventory: '/data/deploy/playbook/inventory',
            colorized: true,
            sudoUser: null,
            extraVars: [
              appname: config.jobName,
              hostname: config.info.ENVIR + '-' + config.info.jobName,
              filename: config.info.appName,
              env: config.info.ENVIR,
              delete: config.info.fe_delete,
            ]
          )
        }
        break
    }
  }catch (err) {
    util.printMessage(err, 'green')
    throw err
  }
}

def be_backPackage(config) {
  def backdir = config.info.backdir
  def jobName = config.info.jobName
  def outpath = config.info.outpath
  def tmplist = config.info.outpath.split('\\.')
  def suffix = tmplist[tmplist.length - 1]
  def appName = config.info.BUILD_ID + '.' + suffix
  config.info.appName = appName

  sh """
  [ ! -d ${backdir}/${jobName} ] && mkdir -p ${backdir}/${jobName} || echo "已存在"
  [ ! -d tmpdir ] && mkdir -p tmpdir || echo "已存在"

  cp -f ${outpath} tmpdir/${appName}

  cp -f ${outpath} ${backdir}/${jobName}/${appName}
  """
}

def fe_backPackage(config) {
  def backdir = config.info.backdir
  def jobName = config.info.jobName
  def outpath = config.info.OUTPACKAGE
  // def tmplist = config.info.outpath.split('\\.')
  // def suffix = tmplist[tmplist.length - 1]
  def appName = config.info.BUILD_ID + '.tgz'

  config.info.appName = appName

  sh """
  [ ! -d ${backdir}/${jobName} ] && mkdir -p ${backdir}/${jobName} || echo "已存在"
  [ ! -d tmpdir ] && mkdir -p tmpdir || echo "已存在"
  """

  sh """
  tar zcvf tmpdir/${appName}  ${outpath}
  cp -f tmpdir/${appName} ${backdir}/${jobName}/
  """
}
