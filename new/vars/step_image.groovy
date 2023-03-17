import  com.test.jenkins.utils.tool
import  com.test.jenkins.devops.gitServer
import  com.test.jenkins.devops.build
import  com.test.jenkins.devops.deploy
import  com.test.jenkins.devops.archives
import  com.test.jenkins.devops.docker

def call(config){
  def tool = new tool()
  def gitcli =  new gitServer()
  def buildcli =  new build()
  def deploycli =  new deploy()
  def dockercli =  new docker()

  pipeline {
      agent any

      // tools {
      //   jdk 'jdk1.8'
      //   maven 'maven3.5'
      //   git 'Default'
      //   nodejs 'node10.15.1'
      // }

      options {
        // 禁止同时运行多个流水线
        disableConcurrentBuilds()
        // 表示保留10次构建历史
        buildDiscarder(logRotator(numToKeepStr: '15'))
        // 任务时间超过10分钟，终止构建
        timeout(time: 20, unit: 'MINUTES')
      }

      environment {
        examples_var1 = sh(script: 'echo "当前的时间是: `date`"', returnStdout: true).trim()
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
        agent {
          docker {
            image 'bitnami/git:2.37.1'
            reuseNode true
          }
        }
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
        agent {
          docker {
            image 'build:0.1'
            reuseNode true
            args '-v /opt/jenkinsAgentSwarn/deploy/m2:/root/.m2'
          // args '-v /opt/jenkinsAgentSwarn/deploy/m2/settings.xml:/root/.m2/settings.xml'
          }
        }

        steps {
          script {
            buildcli.Build(config)
          }
        }
      }
      stage('构建镜像') {
        steps {
          script {
            docker.docker_build(config)
          }
        }
      }
    }
  }
  
  return this
}