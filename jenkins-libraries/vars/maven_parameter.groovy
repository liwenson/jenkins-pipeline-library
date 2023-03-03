
def call(config) {
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
        buildDiscarder(logRotator(numToKeepStr: '15'))
        // 任务时间超过10分钟，终止构建
        timeout(time: 20, unit: 'MINUTES')
      }

      parameters {
        gitParameter(name: 'BRANCH', branchFilter: 'origin/(.*)', defaultValue: config.info.branch, listSize: '10', useRepository: config.info.repository_name, type: 'PT_BRANCH', description: '分支/标签' )
        // string(name: 'BRANCH', defaultValue: config.info.branch, description: 'develop 开发分支----永久留存,是当前最新开发合并出来的代码,包括从feature和hotfix上面合过来的代码 <br />feature/开发分支版本      新需求会在该分支开发,需要在develop上面创建feature分支进行开发 <br />release/预发分支版本    是为新版本测试、发布做准备的,(预发分支)(只能保留允许存在一个子集)<br />hotfix/修复分支版本       修复分支---线上bug产生的分支合集(只能保留允许存在一个子集)<br />master                     生产环境分支----永久留存',trim: true)
        choice(name: 'ENVIR', choices: config.info.envir, description: '环境')
        text(name: 'CMD', defaultValue: config.info.build_cmd, description: '编译命令')
      }

      environment {
        examples_var1 = sh(script: 'echo "当前的时间是: `date`"', returnStdout: true).trim()
      }

      stages {
        stage('Start') {
            steps {
              script {
                config.info.Branch = "${ BRANCH }"
                config.info.ENVIR = "${ ENVIR }"
                config.info.CMD = "${ CMD }"
                maven_start(config)
              }
            }
        }
      }
  }
}
