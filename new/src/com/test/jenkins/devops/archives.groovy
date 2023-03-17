package com.test.jenkins.devops

import  com.test.jenkins.utils.tool


def archives(config){
  def tool = new tool()

  if ( "${ENVIR}" == config.project_cfg.archives_env ) {
    def IsNull = true
    def FORM = null

    while ( IsNull ) {
      IsNull = false

      FORM = input message: '是否需要归档？', ok: '确定',
        parameters: [choice(name: 'ARCHIVE', choices: ['No', 'Yes'], description: '是否归档'), string(name: 'VERSION', defaultValue: '', description: '填写版本号', trim: true)]

      tool.printMessage(FORM['ARCHIVE'], 'green')

      if ( FORM['ARCHIVE'] == 'Yes'  && FORM['VERSION'] == '' ) {
        IsNull = true
        tool.printMessage('版本号不能为空, VERSION is null', 'green')
      }
    }

    def targetName = '/' + config.project_cfg.JOB_NAME + '/' + FORM['VERSION'] + '_' + config.project_cfg.BUILD_ID

    switch (FORM['ARCHIVE']) {
      case 'Yes':
        minio bucket: config.project_cfg.minio_bucket, credentialsId: config.project_cfg.minio_credentialsId, excludes: '', host: config.project_cfg.minio_url, includes: 'tmpdir/' + config.project_cfg.appName, targetFolder: targetName
        tool.printMessage('执行归档完成', 'green')
        break
      default:
        tool.printMessage('不归档', 'green')
    }
  }else {
    tool.printMessage('无归档动作', 'green')
    return
  }
}