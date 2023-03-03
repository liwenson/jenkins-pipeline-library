import  com.data.devops.gitServer
import  com.data.devops.build
import  com.data.devops.ansible
import  com.data.devops.archives
import  com.data.devops.utils

def call(config) {
  // def pipelinerun = new pipelinerun()
  def gitcli =  new gitServer()
  def buildcli =  new build()
  def ansible =  new ansible()
  def archives = new archives()
  def utils = new utils()

  stage('清理工作空间') { //阶段名称
    script {
      utils.clean(config)
    }
  }
  
  //下载代码
  stage('获取代码') { //阶段名称
    script {
      gitcli.CheckOutCode(config)
    }
  }
  stage('项目信息') { //阶段名称
    script {
      utils.show(config)
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
        script {
      buildcli.MyBuild(config)
        }
  }
  // //部署
  stage('部署代码') {
    script {
        ansible.deploy(config)
    }
  }
  //是否归档
  stage('项目归档') {
    script {
      archives.file(config)
    }
  }

  return this
}
