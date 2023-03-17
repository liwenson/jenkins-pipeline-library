package com.test.jenkins.devops

import  com.test.jenkins.utils.tool

def build_image(config){

    def tool = new tool()

    util.printMessage('构建docker镜像', 'green')

    def tag = ''

    if ( "${ Version }" == '') {
        tag = 'latest'
    }else {
        tag = "${ Version}"
    }
    
    def appName = config.project_cfg.appName
    def registry = config.project_cfg.docker_registry
    def image_name = "${registry}/"+ config.project_cfg.project_name +"/${appName}:${tag}-${env.BUILD_NUMBER}"

    sh """
    ls -lh
    cat Dockerfile
    
    docker image build -t ${image_name} --build-arg projectName=${appName} -f Dockerfile .
    """

    withCredentials([usernamePassword(
            credentialsId: 'harbor',
            usernameVariable: 'USER',
            passwordVariable: 'PASS'
    )]) {
        sh """
        docker login ${registry} -u '$USER' -p '$PASS'

        docker image push ${image_name}
        """
    }
}

