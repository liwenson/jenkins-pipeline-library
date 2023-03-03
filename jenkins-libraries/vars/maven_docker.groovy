import  com.data.devops.gitServer
import  com.data.devops.build
import  com.data.devops.ansible
import  com.data.devops.archives
import  com.data.devops.utils
import  com.data.devops.param

def call(config) {
  def gitcli =  new gitServer()
  def param =  new param()
  def buildcli =  new build()
  def ansible =  new ansible()
  def archives = new archives()
  def utils = new utils()

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
        stage('Setup parameters') {
          steps {
            script {
              param.param(config)
            }
          }
        }

        stage('Start') {
            steps {
              script {
                config.info.Branch = "${ BRANCH }"
                config.info.ENVIR = "${ ENVIR }"
                config.info.CMD = "${ CMD }"

              // maven_start(config)
              }
            }
        }

        stage('清理工作空间') { //阶段名称
          steps {
            script {
              utils.clean(config)
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
            utils.show(config)
          // utils.info(config)
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
            buildcli.MyBuild(config)
          }
        }
      }
      // //部署
      stage('部署代码') {
        agent {
            docker {
                image 'my_ansible:2.12.2'
                reuseNode true
                args '-v /opt/jenkinsAgentSwarn/deploy/ansible/config:/etc/ansible -v /opt/jenkinsAgentSwarn/deploy/playbook:/data/deploy/playbook -v /opt/jenkinsAgentSwarn/deploy/back:/data/back'
            }
        }

        steps {
          script {
            ansible.deploy(config)
          }
        }
      }
      //是否归档ls

      stage('项目归档') {
        steps {
          script {
            archives.file(config)
          }
        }
      }
      }
  }
  return this
}
