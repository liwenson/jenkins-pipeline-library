def call(config) {
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
              sh 'echo "node"'
            }
          }
        }
      }
    }
}