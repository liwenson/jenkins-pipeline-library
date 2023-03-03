
import org.yaml.snakeyaml.Yaml

import com.data.devops.utils

def call(def config = [script: null]) {
    def utils = new utils()

    def script = config.script

    def project_name = script.env.project_name
    def project_group = script.env.project_group
    def project_app = script.env.project_app

    if ( project_group == null) {
        // 项目配置文件
        configFile = project_name + '/' + project_app + '/pipelineCfg.yaml'
    }else {
        // 项目配置文件
        configFile = project_name + '/' + project_group + '/' + project_app + '/pipelineCfg.yaml'
    }

    // 全局配置文件
    globalFile = 'global/pipelineCfg.yaml'

    Yaml yaml = new Yaml()
    // 读取全局配置
    String globalString = libraryResource(globalFile)
    Map<String, Object> globj = yaml.load(globalString)

    // 读取项目配置
    String resourceString = libraryResource(configFile)
    Map<String, Object> obj = yaml.load(resourceString)

    // 定义项目ansible 脚本路径
    def playbookPath = null
    if (obj.playbookPath  == null) {
        /* 使用全局配置中的路径 */
        playbookPath = globj.playbookPath + '/' + script.env.project_type + '/main.yaml'
    }else {
        /*  使用项目配置中的路径 */
        playbookPath = obj.playbookPath + '/' + script.env.project_type + '/main.yaml'
    }

    // 封装全局变量
    def info = [:]
    info.type = obj.type ?: globj.type
    info.name =  obj.name ?: globj.name
    info.outpath =  obj.outpath ?: globj.outpath
    info.repository_url =  obj.repository_url ?: globj.repository_url
    info.branch =  obj.branch ?: 'master'
    info.install_cmd =  obj.install_cmd ?: globj.install_cmd
    info.build_cmd =  obj.build_cmd ?: globj.build_cmd
    info.gitCredentialsId = obj.git_credentials_id ?: globj.git_credentials_id
    info.playbookPath = playbookPath
    info.port = obj.port ?: globj.port
    info.jobName =  script.env.jobName ?: null
    info.desc = obj.desc ?: null
    info.BUILD_ID = script.env.BUILD_ID ?: null
    info.backdir = obj.backup_dir ?: globj.backup_dir
    info.archives_env = obj.archives_env ?: globj.backup_dir
    info.envir = obj.env ?: globj.env
    info.debug = obj.debug ?: globj.debug
    info.jvm_opt = obj.jvm_opt ?: globj.jvm_opt
    info.fe_delete = obj.fe_delete ?: globj.fe_delete
    info.config = obj.config ?: globj.config
    info.isconfig = obj.isconfig ?: globj.isconfig
    info.minio_url = obj.minio_url ?: globj.minio_url
    info.minio_bucket = obj.minio_bucket ?: globj.minio_bucket
    info.minio_credentialsId = obj.minio_credentialsId ?: globj.minio_credentialsId
    info.java_ver =   obj.java_ver ?: globj.java_ver
    info.node_ver =   obj.node_ver ?: globj.node_ver
    info.mvn_ver =   obj.mvn_ver ?: globj.mvn_ver
    info.mvn_build_img =   obj.mvn_build_img ?: globj.mvn_build_img
    info.node_build_img =   obj.node_build_img ?: globj.node_build_img
    info.isdocker =   obj.isdocker ?: globj.isdocker
    info.project_name = project_name
    info.project_group = project_group
    info.project_name = project_name
    info.project_app = project_app
    info.image = obj.image ?: globj.image
    info.registryUrl = obj.registryUrl ?: globj.registryUrl
    info.k8sDeployment = obj.k8sDeployment ?: []
    info.pipelineType = obj.pipelineType ?: globj.pipelineType // k8s ,image ,docker ,basic

    // 处理变量
    utils.getRepositoryName(info)

    script.info = info
}
