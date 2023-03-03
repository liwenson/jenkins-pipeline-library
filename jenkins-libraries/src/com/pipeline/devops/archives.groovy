package com.pipeline.devops

import com.data.devops.utils

def file(config) {
  def util = new utils()

  if (config.info.ENVIR == config.info.archives_env ) {
    def IsNull = true
    def FORM = null

    while ( IsNull ) {
      IsNull = false

      FORM = input message: '是否需要归档？', ok: '确定',
        parameters: [choice(name: 'ARCHIVE', choices: ['No', 'Yes'], description: '是否归档'), string(name: 'VERSION', defaultValue: '', description: '填写版本号', trim: true)]

      util.printMessage(FORM['ARCHIVE'], 'green')

      if ( FORM['ARCHIVE'] == 'Yes'  && FORM['VERSION'] == '' ) {
        IsNull = true
        util.printMessage('版本号不能为空, VERSION is null', 'green')
      }
    }

    def targetName = '/' + config.info.jobName + '/' + FORM['VERSION'] + '_' + config.info.BUILD_ID

    switch (FORM['ARCHIVE']) {
      case 'Yes':
        minio bucket: config.info.minio_bucket, credentialsId: config.info.minio_credentialsId, excludes: '', host: config.info.minio_url, includes: 'tmpdir/' + config.info.appName, targetFolder: targetName
        util.printMessage('执行归档完成', 'green')
        break
      default:
        util.printMessage('不归档', 'green')
    }
  }else {
    return
  }
}
