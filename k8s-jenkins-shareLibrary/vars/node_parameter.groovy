def call(config) {
  pipeline {
      agent any

      // tools {
      //   jdk config.info.java_ver
      //   maven config.info.mvn_ver
      //   git 'Default'
      //   nodejs config.info.node_ver
      // }

      options {
        // 禁止同时运行多个流水线
        disableConcurrentBuilds()
      }

      parameters {
        gitParameter( name: 'BRANCH', defaultValue: config.info.branch, branchFilter: 'origin/(.*)', listSize: '10', useRepository: config.info.repository_name, type: 'PT_BRANCH', description: '分支/标签' )

        // string(name: 'BRANCH', defaultValue: config.info.branch, description: '分支', trim: true)

        choice(name: 'ENVIR', choices: config.info.envir, description: '环境')
        string(name: 'OUTPACKAGE', defaultValue: config.info.outpath, description: '制品目录', trim: true)
        choice(name: 'modules', choices: ['no_delete', 'delete'], description: 'node_modules选择')
        text(name: 'INSTALLCMD', defaultValue: config.info.install_cmd, description: '依赖安装\r\n tyarn upgrade zcloud-ui \r\n ')
        text(name: 'BUILDCMD', defaultValue: config.info.build_cmd, description: '编译命令\r\n')
      }

      environment {
        examples_var1 = sh(script: 'echo "当前的时间是: `date`"', returnStdout: true).trim()
      }
      stages {
        stage('Start') {
            steps {
              script {
                config.info.Branch = "${ BRANCH }"
                config.info.ENV = "${ ENV }"
                config.info.Module = "${modules}"
                config.info.install_cmd = "${INSTALLCMD}"
                config.info.build_cmd = "${BUILDCMD}"
                config.info.OUTPACKAGE = "${OUTPACKAGE}"
                node_start(config)
              }
            }
        }
      }
  }
}
