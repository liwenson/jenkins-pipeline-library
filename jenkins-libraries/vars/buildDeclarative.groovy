
import  com.data.devops.utils

def call(config) {
  def util = new utils()

  // 添加项目描述
  currentBuild.rawBuild.project.description = config.info.desc

  // 为差异化准备
  if ( config.info.image == 'true' && config.info.isdocker == 'false') {
    util.printMessage( 'image', 'green')
    maven_image(config)
  }else if ( config.info.type == 'node' && config.info.isdocker == 'true' ) {
    util.printMessage( 'docker', 'green')
    node_docker(config)
  }else if ( config.info.type == 'maven' && config.info.isdocker == 'true' ) {
    util.printMessage( 'docker', 'green')
    maven_docker(config)
  }else if ( config.info.type == 'maven'  && config.info.isdocker == 'false' ) {
    util.printMessage( 'parameter', 'green')
    maven_parameter(config)
  }else if (config.info.type == 'node'  && config.info.isdocker == 'false') {
    util.printMessage( 'parameter', 'green')
    node_parameter(config)
  }else {
    util.printMessage( '项目类型错误', 'green')
  }
}
