package com.test.jenkins.devops

import  com.test.jenkins.utils.tool

import groovy.json.*

def IninDeploymentRes(config) {
  // def tools = new tools()
  // def imageTag = tools.createVersion()
  def tool = new tool()
  // def project = config.info.project_name
  def appName = config.project_cfg.jobName
  def k8sConfig = config.project_cfg.k8sDeployment

  if ( k8sConfig == '[]') {
    tool.printMessage(config.project_cfg.project_group, 'blue')
    return
  }

  tool.Write_to_yaml(config.project_cfg.k8sDeployment, 'kube-generator.yaml')

  sh """
      cp ${config.project_cfg.outputDirectory} app.jar

      curl -O https://datares.oss-cn-hangzhou.aliyuncs.com/deploy/deploymentgen
      chmod +x deploymentgen
      ./deploymentgen gitlabdown -s gitlab.data.com -p 527 -t  bQGAharrxmcTJjw9XYQLY2bumvJSrnGkbHlsOTCVnDg= --path ${config.args_configfile}
      ls -lh
  """

  if ( config.project_cfg.option["${ENVIR}"].jvm == '' && config.project_cfg.config == '' ) {
    sh """
      ./deploymentgen dockerfile -i reg.data.com/scm_jre_baseimage/openjdk8-jre-scm:20221019 -p ${appName} -op app.jar
      cat Dockerfile
    """
  }else if ( config.project_cfg.option["${ENVIR}"].jvm == '' && config.project_cfg.config != '') {
    sh """
      ./deploymentgen dockerfile -i reg.data.com/scm_jre_baseimage/openjdk8-jre-scm:20221019 -p ${appName} -c ${config.info.config} -op app.jar
      cat Dockerfile
    """
  }else if ( config.project_cfg.option["${ENVIR}"].jvm != '' && config.project_cfg.config == '' ) {
    sh """
      ./deploymentgen dockerfile -i reg.data.com/scm_jre_baseimage/openjdk8-jre-scm:20221019 -p ${appName} -jvm [${config.args_jvm_opts}]  -op app.jar
      cat Dockerfile
    """
  }else {
    sh """
      ./deploymentgen dockerfile -i reg.data.com/scm_jre_baseimage/openjdk8-jre-scm:20221019 -p ${appName} -jvm [${config.args_jvm_opts}] -c ${config.info.config} -op app.jar
      cat Dockerfile
    """
  }
}

def k8s_image_build(config) {
    def tool = new tool()
    tool.printMessage('构建镜像','green')

    def imageTag = tool.createVersion()

    config.imageTag = imageTag

    sh """
    ls -lh
    /kaniko/executor --use-new-run --snapshot-mode=redo  -f `pwd`/Dockerfile -c `pwd` --insecure=true --skip-tls-verify=true --insecure-pull --skip-tls-verify-registry=${config.project_cfg.docker_registry} --cache=true --destination=${config.project_cfg.docker_registry}/data/${env.JOB_NAME}:${imageTag}
    """
}


def Deploy(config) {
  
  def tag = config.imageTag

  sh """

  ./deploymentgen deployfile -c kube-generator.yaml -t ${tag}
  ls -lh
  kubectl --kubeconfig=/opt/config get nodes
  kubectl --kubeconfig=/opt/config apply -f resources-kube.yaml

  """
}