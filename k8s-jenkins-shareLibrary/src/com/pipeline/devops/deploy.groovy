package com.pipeline.devops

import com.data.tools.tools
import groovy.json.*

def IninDeploymentRes(config) {
  // def tools = new tools()
  // def imageTag = tools.createVersion()
  def util = new utils()
  // def project = config.info.project_name
  def appName = config.info.jobName
  def k8sConfig = config.info.k8sDeployment

  if ( k8sConfig == '[]') {
    util.printMessage(config.info.project_group, 'blue')
    return
  }

  util.Write_to_yaml(config.info.k8sDeployment, 'kube-generator.yaml')

  sh """
      cp ${config.info.outpath} app.jar

      curl -O https://datares.oss-cn-hangzhou.aliyuncs.com/deploy/deploymentgen
      chmod +x deploymentgen
      ./deploymentgen gitlabdown -s gitlab.data.com -p 527 -t  bQGAharrxmcTJjw9XYQLY2bumvJSrnGkbHlsOTCVnDg= --path ${config.args_configfile}
      ls -lh
  """

  if ( config.args_jvm_opts == '' && config.args_configfile == '' ) {
    sh """
      ./deploymentgen dockerfile -i reg.data.com/scm_jre_baseimage/openjdk8-jre-scm:20221019 -p ${appName} -op app.jar
      cat Dockerfile
    """
  }else if ( config.args_jvm_opts == '' && config.args_configfile != '') {
    sh """
      ./deploymentgen dockerfile -i reg.data.com/scm_jre_baseimage/openjdk8-jre-scm:20221019 -p ${appName} -c ${config.info.config} -op app.jar
      cat Dockerfile
    """
  }else if ( config.args_jvm_opts != '' && config.args_configfile == '' ) {
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

//
}

def IninRes(config) {
  def tools = new tools()
  def imageTag = tools.createVersion()

  config.imageTag = imageTag

  sh """
      chmod +x getgitlabfile
      ./getgitlabfile -token "vTtVUdGD6o2W5N5nTJDZALIW8OejBH92yff6RL0Kcok=" -s git.data.com -out deploy -pid 52 -p ${JOB_NAME}
      ./getgitlabfile -token "vTtVUdGD6o2W5N5nTJDZALIW8OejBH92yff6RL0Kcok=" -s git.data.com -out deploy -pid 52 -p tools

      ls -lh
      cd deploy
      chmod +x deploymentgen
      ./deploymentgen -f kube-generator.yaml -t ${imageTag}
      ls output
      cat output/resources-kube.yaml
  """
}

def Inink8s2(config) {
  imageTag = config.imageTag
  withCredentials([[$class: 'UsernamePasswordMultiBinding',
  credentialsId: '    ci-devops',
  usernameVariable: 'DEVOPS_USER',
  passwordVariable: 'DEVOPS_PASSWORD']]) {
    container('kustomize') {
      script {
        sh """
          chmod +x getgitlabfile
          ./getgitlabfile -token M3fBmA501w0pE4xo35jfHL0r633oFnBv4XGuyAMZESw= -s git.data.com -out deploy -pid 52 -p ${JOB_NAME}
          ./getgitlabfile -token M3fBmA501w0pE4xo35jfHL0r633oFnBv4XGuyAMZESw= -s git.data.com -out deploy -pid 52 -p tools

          chmod +x deploy/deploymentgen

          ./deploy/deploymentgen -f deploy/${JOB_NAME}/kube-generator.yaml -t ${imageTag}
          ls

          ls output
          cat output/be-data-test-logdemo-kube.yaml
        """
      }

    // kubernetesDeploy(enableConfigSubstitution: false, kubeconfigId: 'kubeconfig', configs: "devops-cd/output/${JOB_NAME}-kube.yaml")
    }
  }
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

// def Inink8s(config) {
//   imageTag = config.imageTag
//   withCredentials([[$class: 'UsernamePasswordMultiBinding',
//   credentialsId: 'ci-devops',
//   usernameVariable: 'DEVOPS_USER',
//   passwordVariable: 'DEVOPS_PASSWORD']]) {
//       container('kustomize') {
//           script {
//         // APP_DIR="${JOB_NAME}".split("_")[0]
//         sh """
//               git remote set-url origin http://${DEVOPS_USER}:${DEVOPS_PASSWORD}@${devops_cd_git}
//               git config --global user.name "jenkins-cd"
//               git config --global user.email "ops@163.com"
//               git clone http://${DEVOPS_USER}:${DEVOPS_PASSWORD}@${devops_cd_git} /opt/devops-cd
//               cd /opt/devops-cd
//               git pull
//               cd /opt/devops-cd/
//               sed "s@{{appname}}@${JOB_NAME}@g;s@{{image}}@${dockerRegistryUrl}/data/${JOB_NAME}:${imageTag}@g" etc/template.yaml > etc/kube-generator.yaml
//               chmod +x deploymentgen
//               ./deploymentgen
//               cd output
//               """
//           }
//           kubernetesDeploy(enableConfigSubstitution: false, kubeconfigId: 'kubeconfig', configs: "${JOB_NAME}-kube.yaml")
//       }
//   }
// }

def DeployK8s(config) {
  container('kustomize') {
    script {
      sh '''
        ls -lh /opt/
        cd /opt/devops-cd/output
        ls -lh
        '''
        kubernetesDeploy(enableConfigSubstitution: false, kubeconfigId: 'kubeconfig', configs: "${JOB_NAME}-kube.yaml")
    }
  }
}
