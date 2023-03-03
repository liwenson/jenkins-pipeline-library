package com.pipeline.devops

import com.data.devops.utils
import com.data.tools.tools

def docker_image_build(config) {
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

    sh """
  cp ${config.info.outpath} app.jar
  curl -O https://datares.oss-cn-hangzhou.aliyuncs.com/deploy/deploymentgen
  chmod +x deploymentgen
  ./deploymentgen dockerfile -i reg.data.com/scm_jre_baseimage/openjdk8-jre-scm:20221019 -p ${appName} -op app.jar
  ./deploymentgen gitlabdown -s gitlab.data.com -p 527 -t  bQGAharrxmcTJjw9XYQLY2bumvJSrnGkbHlsOTCVnDg= --path ${config.args_configfile}
  docker image build -t reg.data.com/${project}/${appName}:${tag}-${env.BUILD_NUMBER} -f Dockerfile .
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

def k8s_image_build(config) {
    def util = new utils()
    util.printMessage('构建镜像','green')
    def tools = new tools()
    def imageTag = tools.createVersion()

    config.imageTag = imageTag

    sh """
    ls -lh
    /kaniko/executor --use-new-run --snapshot-mode=redo  -f `pwd`/Dockerfile -c `pwd` --insecure=true --skip-tls-verify=true --insecure-pull --skip-tls-verify-registry=${config.info.registryUrl} --cache=true --destination=${config.info.registryUrl}/data/${env.JOB_NAME}:${imageTag}
  """

  // docker 方案
  // withCredentials([[$class: 'UsernamePasswordMultiBinding',
  //   credentialsId: 'dockerhub',
  //   usernameVariable: 'DOCKER_HUB_USER',
  //   passwordVariable: 'DOCKER_HUB_PASSWORD']]) {
  //     container('docker') {
  //       script {
  //         tools.PrintMes('构建镜像','green')
  //         imageTag = tools.createVersion()
  //         config.imageTag = imageTag

//         sh """
//           pwd
//           ls -lh
//           cp -rf ${outpath} ./app.jar
//           echo '${DOCKER_HUB_PASSWORD}' | docker login ${dockerRegistryUrl} --username ${DOCKER_HUB_USER} --password-stdin
//           docker build -t ${dockerRegistryUrl}/data/${JOB_NAME}:${imageTag} .
//           docker push ${dockerRegistryUrl}/data/${JOB_NAME}:${imageTag}
//           docker rmi ${dockerRegistryUrl}/data/${JOB_NAME}:${imageTag}
//           """
//       }
//     }
//   }
}
