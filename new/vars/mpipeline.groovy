import com.test.jenkins.pipeline.MPipeline

/**
 *  入口
 */
def call(body) {

  // Groovy "构建器模式"
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()
  
  config.repo = config.repo ?: null
  config.script = config.script ?: null
  config.autoCheckout = config.autoCheckout ?: true

  if (config.script == null) {
      throw new Exception('<script=this>是必填参数')
  }

  node {

      config.script.env.JOB_NAME = "${JOB_NAME}"
      config.script.env.BUILD_ID = "${BUILD_ID}"

      def mpipe = new MPipeline(config.script)

      // 初始化资源
      mpipe.initializeResources()
  }

  return this

}
