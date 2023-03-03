
import  com.data.devops.utils

def call(config) {
  def util = new utils()

  // 添加项目描述
  currentBuild.rawBuild.project.description = config.info.desc

  // 为差异化准备
  // pipelineType
  // k8s ,image ,docker ,basic

  util.printMessage( config.info.pipelineType , 'green')

  if ( config.info.pipelineType == 'docker' && config.info.type == 'node') {
    node_docker(config)
  }else if ( config.info.pipelineType == 'basic' && config.info.type == 'node') {
    node_parameter(config)
  }else if (config.info.pipelineType == 'image' && config.info.type == 'maven' ) {
    maven_image(config)
  }else if (config.info.pipelineType == 'docker' && config.info.type == 'maven' ) {
    maven_docker(config)
  }else if (config.info.pipelineType == 'k8s' && config.info.type ==  'maven') {
    maven_k8s(config)
  }else if (config.info.pipelineType == 'basic' && config.info.type ==  'maven') {
    maven_parameter(config)
  }else {
    util.printMessage( '项目类型错误', 'green')
  }
}
