package com.pipeline.devops

import com.data.devops.utils

def param(config) {
  if ( config.info.pipelineType == 'image' ) {
    properties([
      parameters([
        gitParameter(name: 'BRANCH', branchFilter: 'origin/(.*)', defaultValue: config.info.branch, listSize: '10', useRepository: config.info.repository_name, type: 'PT_BRANCH', description: '分支/标签' ),
        // string(name: 'BRANCH', defaultValue: config.info.branch, description: 'develop 开发分支----永久留存,是当前最新开发合并出来的代码,包括从feature和hotfix上面合过来的代码 <br />feature/开发分支版本      新需求会在该分支开发,需要在develop上面创建feature分支进行开发 <br />release/预发分支版本    是为新版本测试、发布做准备的,(预发分支)(只能保留允许存在一个子集)<br />hotfix/修复分支版本       修复分支---线上bug产生的分支合集(只能保留允许存在一个子集)<br />master                     生产环境分支----永久留存',trim: true)
        choice(name: 'ENVIR', choices: config.info.envir, description: '环境'),
        string(name: 'Version', defaultValue:'' , description: '版本信息', trim: true),
        text(name: 'CMD', defaultValue: config.info.build_cmd, description: '编译命令')
        ])
    ])
  }else if ( config.info.pipelineType == 'k8s') {
    properties([
      parameters([
        string(name: 'gitBranch', defaultValue: config.info.branch, description: '分支', trim: true),
        string(name: 'gitUrl', defaultValue: config.info.repository_url, description: 'GIT仓库', trim: true),
        choice(name: 'ENVIR', choices: config.info.envir, description: '环境'),
        text(name: 'CMD', defaultValue: config.info.build_cmd, description: '编译命令\r\n'),
        string(name: 'dockerRegistryUrl', defaultValue: config.info.dockerRegistryUrl, description: 'docker仓库', trim: true),
        string(name: 'outpath', defaultValue: config.info.outpath, description: 'target', trim: true),
        // string(name: 'devops_cd_git', defaultValue: 'git.data.com/ops/project-resource.git', description: 'ci_git', trim: true)
        ])
    ])
  }else if ( config.info.type == 'maven') {
    properties([
      parameters([
        gitParameter(name: 'BRANCH', branchFilter: 'origin/(.*)', defaultValue: config.info.branch, listSize: '10', useRepository: config.info.repository_name, type: 'PT_BRANCH', description: '分支/标签' ),
        // string(name: 'BRANCH', defaultValue: config.info.branch, description: 'develop 开发分支----永久留存,是当前最新开发合并出来的代码,包括从feature和hotfix上面合过来的代码 <br />feature/开发分支版本      新需求会在该分支开发,需要在develop上面创建feature分支进行开发 <br />release/预发分支版本    是为新版本测试、发布做准备的,(预发分支)(只能保留允许存在一个子集)<br />hotfix/修复分支版本       修复分支---线上bug产生的分支合集(只能保留允许存在一个子集)<br />master                     生产环境分支----永久留存',trim: true)
        choice(name: 'ENVIR', choices: config.info.envir, description: '环境'),
        text(name: 'CMD', defaultValue: config.info.build_cmd, description: '编译命令')
        ])
    ])
  } else {
    properties([
      parameters([
        gitParameter(name: 'BRANCH', branchFilter: 'origin/(.*)', defaultValue: config.info.branch, listSize: '10', useRepository: config.info.repository_name, type: 'PT_BRANCH', description: '分支/标签' ),
        // string(name: 'BRANCH', defaultValue: config.info.branch, description: '分支', trim: true)
        choice(name: 'ENVIR', choices: config.info.envir, description: '环境'),
        string(name: 'OUTPACKAGE', defaultValue: config.info.outpath, description: '制品目录', trim: true),
        choice(name: 'modules', choices: ['no_delete', 'delete'], description: 'node_modules选择'),
        text(name: 'INSTALLCMD', defaultValue: config.info.install_cmd, description: '依赖安装\n tyarn upgrade zcloud-ui'),
        text(name: 'BUILDCMD', defaultValue: config.info.build_cmd, description: '编译命令')
      ])
    ])
  }
}
