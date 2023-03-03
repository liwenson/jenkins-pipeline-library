package com.pipeline.tools

import com.data.tools.*

/**
 * sonar 封装HTTP请求
 * @param buildType
 * @return
 */
def HttpReq(requestType, requestUrl, requestBody) {
  // 定义sonar api接口
  def sonarServer = 'http://sonar.data.com/api'

  result = httpRequest authentication: 'sonar-admin-user',
            httpMode: requestType,
            contentType: 'APPLICATION_JSON',
            consoleLogResponseBody: true,
            ignoreSslErrors: true,
            requestBody: requestBody,
            url: "${sonarServer}/${requestUrl}"
  return result
}

/**
 * sonar 扫描
 * @param buildType
 * @return
 */
def SonarScan(config) {
  def tools = new tools()

  // sonarScanner安装地址
  def sonarHome = '/opt/sonar-scanner'
  // sonarqube服务端地址
  def sonarServer = 'http://sonar.data.com/'
  // 以时间戳为版本
  def scanTime = sh returnStdout: true, script: 'date +%Y%m%d%H%m%S'
  scanTime = scanTime - '\n'
  def projectName = "${ JOB_NAME }"
  def projectDesc = "${ JOB_NAME }"
  def report = 'target/surefire-reports'

  println(config.info.moduleProject)

  if (config.info.moduleProject.size() > 0 ) {
    println('moduleProject not is null')
    def modules = tools.listTostr(config.info.moduleProject, 0, config.info.moduleProject.size())

    sh """
        ${sonarHome}/bin/sonar-scanner  -Dsonar.host.url=${sonarServer}  \
        -Dsonar.projectKey=${projectName}  \
        -Dsonar.projectName=${projectName}  \
        -Dsonar.projectVersion=${scanTime} \
        -Dsonar.login=admin \
        -Dsonar.password=admin \
        -Dsonar.ws.timeout=45 \
        -Dsonar.projectDescription="${projectDesc}"  \
        -Dsonar.sources=src \
        -Dsonar.java.test.binaries=target/classes \
        -Dsonar.sourceEncoding=UTF-8 \
        -Dsonar.java.binaries=target/classes \
        -Dsonar.java.surefire.report=${report} \
        -Dsonar.modules="${modules}" -X
        """
    }else {
    println('moduleProject is null')

    sh """
        ${sonarHome}/bin/sonar-scanner  -Dsonar.host.url=${sonarServer}  \
        -Dsonar.projectKey=${projectName}  \
        -Dsonar.projectName=${projectName}  \
        -Dsonar.projectVersion=${scanTime} \
        -Dsonar.login=admin \
        -Dsonar.password=admin \
        -Dsonar.ws.timeout=30 \
        -Dsonar.projectDescription="${projectDesc}"  \
        -Dsonar.sources=src \
        -Dsonar.sourceEncoding=UTF-8 \
        -Dsonar.java.binaries=target/classes \
        -Dsonar.java.test.binaries=target/classes \
        -Dsonar.java.surefire.report=${report} -X
        """
  }
  tools.PrintMes("${projectName}  scan success!", 'green')
}

/**
 * sonar 获取soanr项目的状态
 * @param projectName
 * @return
 */
def GetSonarStatus(projectName) {
  def apiUrl = "project_branches/list?project=${projectName}"
  // 发请求
  response = HttpReq('GET', apiUrl, '')
  // 对返回的文本做JSON解析
  response = readJSON text: """${response.content}"""
  // 获取状态值
  result = response['branches'][0]['status']['qualityGateStatus']
  return result
}

/**
 * sonar 获取sonar项目，判断项目是否存在
 * @param projectName
 * @return
 */
def SearchProject(projectName) {
  def apiUrl = "projects/search?projects=${projectName}"
  // 发请求
  response = HttpReq('GET', apiUrl, '')

  println "Search results: ${response}"

  // 对返回的文本做JSON解析
  response = readJSON text: """${response.content}"""
  // 获取total字段，该字段如果是0则表示项目不存在,否则表示项目存在
  result = response['paging']['total']
  // 对result进行判断
  if (result.toString() == '0') {
    return 'false'
    }else {
    return 'true'
  }
}

/**
 * sonar 创建sonar项目
 * @param projectName
 * @return
 */
def CreateProject(projectName) {
  def apiUrl = "projects/create?name=${projectName}&project=${projectName}"
  // 发请求
  response = HttpReq('POST', apiUrl, '')
  println(response)
}

/**
 * sonar 项目质量规则
 * @param projectName,lang, qpname
 * @return
 */
def ConfigQualityProfiles(projectName, lang, qpname) {
  def apiUrl = "qualityprofiles/add_project?language=${lang}&project=${projectName}&qualityProfile=${qpname}"
  // 发请求
  response = HttpReq('POST', apiUrl, '')
  println(response)
}

/**
 * sonar 获取质量阈ID
 * @param gateName
 * @return
 */
def GetQualityGateId(gateName) {
  def apiUrl = "qualitygates/show?name=${gateName}"
  // 发请求
  response = HttpReq('GET', apiUrl, '')
  // 对返回的文本做JSON解析
  response = readJSON text: """${response.content}"""
  // 获取total字段，该字段如果是0则表示项目不存在,否则表示项目存在
  result = response['id']
  return result
}

/**
 * sonar 更新质量阈规则
 * @param projectKey,gateName
 * @return
 */
def ConfigQualityGate(projectKey, gateName) {
  // 获取质量阈id
  gateId = GetQualityGateId(gateName)
  apiUrl = "qualitygates/select?projectKey=${projectKey}&gateId=${gateId}"
  // 发请求
  response = HttpReq('POST', apiUrl, '')
  println(response)
}

/**
 * sonar 获取Sonar质量阈状态
 * @param projectName
 * @return
 */
def GetProjectStatus(projectName) {
  apiUrl = "project_branches/list?project=${projectName}"
  response = HttpReq('GET', apiUrl, '')

  response = readJSON text: """${response.content}"""
  result = response['branches'][0]['status']['qualityGateStatus']

  return result
}
