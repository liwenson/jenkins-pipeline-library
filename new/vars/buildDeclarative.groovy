import  com.test.jenkins.utils.tool
import  com.test.jenkins.step.maven

def call(config) {
  def tool = new tool()
  
  // 添加项目描述
  currentBuild.rawBuild.project.description = config.project_cfg.desc

  if ( config.project_cfg.build_type == 'node') {

    properties([
      parameters([
        gitParameter(name: 'BRANCH', branchFilter: 'origin/(.*)', defaultValue: config.project_cfg.repository_branch, selectedValue: 'DEFAULT', listSize: '10', useRepository: config.info.repository_name, type: 'PT_BRANCH', description: '分支/标签' ),
        // string(name: 'BRANCH', defaultValue: config.info.branch, description: '分支', trim: true)
        choice(name: 'ENVIR', choices: config.project_cfg.envir, description: '环境'),
        choice(name: 'NodeVer', choices: config.project_cfg.node_versions, description: 'node版本'),
        string(name: 'OUTPACKAGE', defaultValue: config.project_cfg.outputDirectory, description: '制品目录', trim: true),
        choice(name: 'IsModules', choices: ['no_delete', 'delete'], description: '是否删除modules'),
        text(name: 'InstallCMD', defaultValue: config.project_cfg.install_cmd, description: '依赖安装\n tyarn upgrade zcloud-ui'),
        text(name: 'BuildCMD', defaultValue: config.project_cfg.build_cmd, description: '编译命令')
      ])
    ])
  }else if ( config.project_cfg.build_type == 'maven' ) {
    properties([
      parameters([
        gitParameter(name: 'BRANCH', branchFilter: 'origin/(.*)', defaultValue: config.project_cfg.repository_branch, selectedValue: 'DEFAULT', listSize: '10', useRepository: config.project_cfg.repository_url, type: 'PT_BRANCH', description: '分支/标签' ),
        // string(name: 'BRANCH', defaultValue: config.info.branch, description: 'develop 开发分支----永久留存,是当前最新开发合并出来的代码,包括从feature和hotfix上面合过来的代码 <br />feature/开发分支版本      新需求会在该分支开发,需要在develop上面创建feature分支进行开发 <br />release/预发分支版本    是为新版本测试、发布做准备的,(预发分支)(只能保留允许存在一个子集)<br />hotfix/修复分支版本       修复分支---线上bug产生的分支合集(只能保留允许存在一个子集)<br />master                     生产环境分支----永久留存',trim: true)
        choice(name: 'ENVIR', choices: config.project_cfg.envir, description: '环境'),
        string(name: 'Version', defaultValue:'' , description: '版本信息', trim: true),
        text(name: 'CMD', defaultValue: '', description: '编译命令'),
        text(name: 'CleanJar', defaultValue: '', description: '清理Maven缓存中的jar <br />一行一条<br />MavenDefault:.m2/repository/<br />只需要填写该目录后续的路径')
        ])
    ])

  }else {
    tool.printMessage( '项目类型错误', 'green')
  }

  if ( config.project_cfg.option["${ENVIR}"].pipelineType == "" ) {
    // 如果早项目配置中没有配置 pipelineType 参数，则使用全局默认参数
    config.project_cfg.option["${ENVIR}"].pipelineType = config.project_cfg.pipelineType
  }

  if ( config.project_cfg.option["${ENVIR}"].pipelineType == 'docker') {
    step_docker(config)
  } else if ( config.project_cfg.option["${ENVIR}"].pipelineType == 'k8s'){
    step_k8s(config)
  }else if ( config.project_cfg.option["${ENVIR}"].pipelineType == 'image'){
    step_image(config)
  }else if ( config.project_cfg.option["${ENVIR}"].pipelineType == 'basic'){
    step_basic(config)
  }else{
    tool.printMessage( "类型错误:" + config.project_cfg.option["${ENVIR}"].pipelineType, 'green')
  }
  
}