package com.pipeline.devops

import com.data.devops.utils

def docker_build(config) {
    def util = new utils()
    util.printMessage('构建docker镜像', 'green')
    def project = config.info.project_name
    def appName = config.info.jobName
    def tag = ''

    if ( "${ Version }" == '') {
        tag = 'latest'
    }else {
        tag = "${ Version}"
    }
    def configfile = ''
    if ( config.info.project_name == 'scm' ) {
        configfile = config.info.project_name + '/' + config.info.project_group + '/' + config.info.project_app + '/' + config.info.ENVIR + '/' + config.info.config
    }else if ( config.info.project_name == 'feilong' ) {
        configfile =  config.info.project_name + '/' + config.info.ENVIR + '/' + config.info.config
    } else {
        configfile =   'feilong/' + config.info.ENVIR + '/' + config.info.config
    }

    sh """
      
      ls -lh
      cat Dockerfile
      
      docker image build -t reg.data.com/${project}/${appName}:${tag}-${env.BUILD_NUMBER} --build-arg projectName=${appName} -f Dockerfile .
    """

    withCredentials([usernamePassword(
            credentialsId: 'harbor',
            usernameVariable: 'USER',
            passwordVariable: 'PASS'
    )]) {
        sh """
        docker login reg.data.com -u '$USER' -p '$PASS'

        docker image push reg.data.com/${project}/${appName}:${tag}-${env.BUILD_NUMBER}
        """
    }
}
