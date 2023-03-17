import org.yaml.snakeyaml.Yaml
import com.test.jenkins.utils.tool


def call(def config = [script: null]) {
    def tool = new tool()

    def script = config.script
    def project_name = script.env.project_name // scm
    def project_group = script.env.project_group // ofc
    def project_app = script.env.project_app // gc

    if (project_group == null) {
        // 项目配置文件
        configFile = project_name + '/' + project_app + '/pipelineCfg.yaml'
    } else {
        // 项目配置文件
        configFile = project_name + '/' + project_group + '/' + project_app + '/pipelineCfg.yaml'
    }

    // 全局配置文件
    globalFile = 'global/pipelineCfg.yaml'

    Yaml yaml = new Yaml()
        // 读取全局配置
    String globalString = libraryResource(globalFile)
    Map < String, Object > global_obj = yaml.load(globalString)

    // 读取项目配置
    String resourceString = libraryResource(configFile)
    Map < String, Object > project_obj = yaml.load(resourceString)

    // 定义项目ansible 脚本路径
    def playbookPath = null
    if (project_obj.playbookPath  == null) {
        /* 使用全局配置中的路径 */
        playbookPath = global_obj.playbookPath + '/' + script.env.project_type + '/main.yaml'
    }else {
        /*  使用项目配置中的路径 */
        playbookPath = project_obj.playbookPath + '/' + script.env.project_type + '/main.yaml'
    }


    // 封装全局变量
    def project_cfg = [:]

    // project 
    project_cfg.build_type = project_obj.build_type ?: global_obj.build_type // 构建类型
    project_cfg.desc = project_obj.desc ?: null    // 描述
    project_cfg.outputDirectory = project_obj.outputDirectory ?: global_obj.outputDirectory //  构建包输出的路径
    project_cfg.port = project_obj.port ?: global_obj.port   // 项目端口
    project_cfg.JOB_NAME = script.env.JOB_NAME ?: null    // jenkins  job
    project_cfg.BUILD_ID = script.env.BUILD_ID ?: null    // jenkins  BUILD_ID
    project_cfg.project_name = project_name  // 项目名称
    project_cfg.project_group = project_group  // 项目分组
    project_cfg.project_app = project_app   //  应用名称
    project_cfg.backdir = project_obj.backup_dir ?: global_obj.backup_dir    // 项目备份路径
    project_cfg.archives_env = project_obj.archives_env ?: global_obj.archives_env  // 需要归档环境
    project_cfg.option = project_obj.option ?: global_obj.option    // 应用选项
    project_cfg.config = project_obj.config ?: ""   // 应用配置
    // k8s
    project_cfg.k8sDeployment = project_obj.k8sDeployment ?: [] // k8s 部署参数

    // 环境变量
    // linkedhashset to List
    def envList = project_cfg.option.keySet()
    List<String> envsList = new ArrayList<>(envList);
    project_cfg.envir = envsList          // 应用环境

    // debug
    project_cfg.debug = project_obj.debug ?: global_obj.debug   // 应用jenkins debug
    project_cfg.pipelineType =  global_obj.pipelineType ?: "docker"
    project_cfg.ssh_credentialsId = project_obj.ssh_credentialsId ?: global_obj.ssh_credentialsId
    project_cfg.docker_registry = project_obj.docker_registry ?: global_obj.docker_registry   // docker镜像仓库
    project_cfg.npm_registry = project_cfg.npm_registry ?: global_obj.npm_registry   // npm 仓库
    // git
    project_cfg.repository_url = project_obj.repository_url ?: global_obj.repository_url // git 地址
    project_cfg.repository_branch = project_obj.repository_branch ?: 'master' // git 分支
    project_cfg.gitCredentialsId = project_obj.git_credentialsId ?: global_obj.git_credentialsId // git user

    // build
    project_cfg.install_cmd = project_obj.install_cmd ?: global_obj.install_cmd   // 依赖安装命令
    project_cfg.build_cmd = project_obj.build_cmd ?: global_obj.build_cmd    // 构建命令
    project_cfg.node_versions =  project_obj.node_versions ?: global_obj.node_versions  // node版本
    
    // deploy
    project_cfg.playbookPath = playbookPath
    project_cfg.inventory = global_obj.inventory ?: '/etc/ansible/hosts'  // hosts zhum

    // minio
    project_cfg.minio_url = project_obj.minio_url ?: global_obj.minio_url
    project_cfg.minio_bucket = project_obj.minio_bucket ?: global_obj.minio_bucket
    project_cfg.minio_credentialsId =  project_obj.minio_credentialsId ?: global_obj.minio_credentialsId

    script.project_cfg = project_cfg
}