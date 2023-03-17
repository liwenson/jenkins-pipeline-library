package com.test.jenkins.devops

def CheckOutCode(config) {

  checkout([
    $class: 'GitSCM', 
    branches: [[name: "${BRANCH}"]],
    doGenerateSubmoduleConfigurations: false,
    extensions: [
      [$class: 'CheckoutOption', timeout: 45], 
      [$class: 'CloneOption', noTags: false, reference: '', shallow: false, timeout: 45]
    ],
    submoduleCfg        : [],
    userRemoteConfigs   : [[
      credentialsId: config.project_cfg.gitCredentialsId, 
      url: config.project_cfg.repository_url
    ]]
  ])

}
