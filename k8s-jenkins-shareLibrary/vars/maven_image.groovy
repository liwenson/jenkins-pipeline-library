import  com.data.devops.gitServer
import  com.data.devops.build
import  com.data.devops.ansible
import  com.data.devops.archives
import  com.data.devops.docker
import  com.data.devops.utils
import  com.data.devops.param

def call(config) {
  def gitcli =  new gitServer()
  def param =  new param()
  def buildcli =  new build()
  def docker =  new docker()
  // def ansible =  new ansible()
  // def archives = new archives()
  def utils = new utils()

  pipeline {
      agent any

      tools {
        jdk config.info.java_ver
        maven config.info.mvn_ver
        git 'Default'
        nodejs config.info.node_ver
      }

      options {
        // 禁止同时运行多个流水线
        disableConcurrentBuilds()
        // 表示保留5次构建历史
        buildDiscarder(logRotator(numToKeepStr: '5'))
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
        //构建代码
        stage('构建代码') {
          steps {
            script {
              buildcli.MyBuild(config)
            }
          }
        }
        stage('构建镜像') {
          steps {
            script {
              docker.docker_image_build(config)
            }
          }
        }
      }
  }
  return this
}
