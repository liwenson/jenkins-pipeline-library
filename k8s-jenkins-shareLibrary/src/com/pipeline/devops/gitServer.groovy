package com.pipeline.devops

import com.data.devops.utils

def CheckOutCode(config) {
  def util = new utils()

  def branchName = config.info.Branch
  def credentialsId = config.info.gitCredentialsId
  def srcUrl = config.info.repository_url

  // util.printMessage(config.env, 'green')
  // util.printMessage(config.info, 'green')
  // util.printMessage('获取代码', 'green')
  // util.info()

  util.printMessage(branchName, 'green')

  checkout([$class: 'GitSCM', branches: [[name: "${branchName}"]],
              doGenerateSubmoduleConfigurations: false,
              extensions                       : [[$class: 'CheckoutOption', timeout: 45], [$class: 'CloneOption', noTags: false, reference: '', shallow: false, timeout: 45]],
              submoduleCfg                     : [],
              userRemoteConfigs                : [[credentialsId: "${credentialsId}", url: "${srcUrl}"]]])
}
