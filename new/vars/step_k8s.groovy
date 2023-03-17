import  com.test.jenkins.utils.tool
import  com.test.jenkins.devops.gitServer
import  com.test.jenkins.devops.build
import  com.test.jenkins.devops.deploy
import  com.test.jenkins.devops.archives
import  com.test.jenkins.devops.docker
import  com.test.jenkins.devops.k8s


// 需要在k8s 中的jenkins
def call(config) {
  def tool = new tool()
  def gitcli =  new gitServer()
  def buildcli =  new build()
  def deploycli =  new deploy()
  def dockercli =  new docker()
  def k8scli =  new k8s()

  pipeline {
    agent {
      kubernetes {
        yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    some-label: some-label-value
spec:
  volumes:
  - name: kubeconfig
    configMap:
      name: kubeconfig
      items:
      - key: config
        path: config

  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
      type: ''

  - name: maven-cache
    persistentVolumeClaim:
      claimName: maven-cache-pvc

  - name: harbor-registry
    projected:
      sources:
      - secret:
          name: harbor-registry
          items:
          - key: .dockerconfigjson
            path: config.json

  containers:
  - name: jnlp
    image: reg.data.com/library/inbound-agent:4.10-3-alpine

  - name: maven
    image: reg.data.com/library/maven:3.5.0-alpine
    command:
    - cat
    tty: true
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2

  - name: kubectl
    image: reg.data.com/bitnami/kubectl:1.23.5
    securityContext:
      runAsUser: 1000
    command:
    - cat
    tty: true
    volumeMounts:
    - name: kubeconfig
      mountPath: /opt/config
      subPath: config

  - name: kaniko
    image: reg.data.com/library/kaniko-executor:v1.9.1-debug
    imagePullPolicy: IfNotPresent
    command:
    - sleep
    args:
    - 1d
    tty: true
    volumeMounts:
      - name: harbor-registry
        mountPath: /kaniko/.docker

  - name: sonar-scanner
    image: reg.data.com/library/sonar-scanner:lastest
    command:
    - cat
    tty: true

  - name: kustomize
    image: reg.data.com/library/kustomize:v3.8.1
    command:
    - cat
    tty: true
"""
      }
    }

      // tools {
      //   jdk 'jdk1.8'
      //   maven 'maven3.5'
      //   git 'Default'
      //   nodejs 'node10.15.1'
      // }

      options {
        // 禁止同时运行多个流水线
        disableConcurrentBuilds()
        // 表示保留5次构建历史
        buildDiscarder(logRotator(numToKeepStr: '15'))
        // 任务时间超过10分钟，终止构建
        timeout(time: 20, unit: 'MINUTES')
      }

      stages {


        stage('清理工作空间') { //阶段名称
          steps {
            script {
              tool.clean(config)
            }
          }
        }

        //下载代码
        stage('获取代码') { //阶段名称
          steps {
            script {
              gitcli.CheckOutCode(config)
            }
          }
        }
        stage('项目信息') { //阶段名称
          steps {
            script {
              tool.show(config)
            }
          }
        }

 // //代码扫描
  // stage("代码扫描") {
  //     script {
  //         sonar.sonarScan(paramMap)
  //     }
  // }

        //构建代码
        stage('构建代码') {
          steps {
            container('maven') {
              script {
                tool.printMessage('编译打包','blue')
                buildcli.Build(config)
              }
            }
          }
        }

        stage('资源生成') {
          steps {
            container('kustomize') {
              script {
                // deploy.IninRes(config)
                k8scli.IninDeploymentRes(config)
              }
            }
          }
        }

        //部署
        stage('构建镜像') {
          steps {
            container('kaniko') {
              script {
                k8scli.k8s_image_build(config)
              }
            }
          }
        }

        stage('部署到k8s') {
          steps {
            container('kubectl') {
              script {
                k8scli.Deploy(config)
              }
            }
          }
        }
      }

  // 操作后的告警
  }
}
