package com.test.jenkins.utils

/**
 * 格式化输出
 * @param value
 * @param color
 * @return
 */
def printMessage(value, color) {
  def colors = ['groovy': "\033[40;31m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m",
                  'blue'  : "\033[47;34m ${value} \033[0m",
                  'green' : "\033[1;32m>>>>>>>>>>>\n${value}\n\033[0m"]

  ansiColor('xterm') {
    println(colors[color])
  }
}

// 清理项目空间
def clean(config) {

  try {
    if ( config.project_cfg.build_type == 'node' && "${IsModules}" == "no_delete") {
      this.printMessage('不清理项目空间', 'green')
    }else {
      this.printMessage('清理项目空间', 'green')
      deleteDir()
    }
    
  }catch (err) {
    throw err
  }

}


/**
 * 输出项目信息
 * @param value
 * @return
 */
def show(config) {
    // this.printMessage('获取代码', 'green')

    def gitChange = this.getChangeString()

    wrap([$class: 'BuildUser']) {
        script {
            BUILD_USER_ID = "${env.BUILD_USER_ID}"
            BUILD_USER = "${env.BUILD_USER}"
        }
    }

    def content = 'GIT地址: ' + config.project_cfg.repository_url + '\n构建用户: ' + BUILD_USER + '\n构建分支: ' + "${BRANCH}" + '\n构建环境: ' + "${ENVIR}" + '\n' + gitChange
    this.printMessage(content, 'green')

    // 构建历史中插入信息
    manager.addShortText("分支: ${BRANCH}  环境: ${ENVIR}",'grey', 'white', '0px', 'white')
}


@NonCPS
def getChangeString() {
    MAX_MSG_LEN = 100
    def changeString = ''

    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def commit_msg = entry.msg.take(MAX_MSG_LEN)
            def commit_id = entry.commitId
            // def commit_time = entry.timestamp
            def commit_author = entry.author
            changeString += "\nauthor: ${commit_author}\ncommit id: ${commit_id}\ncommit message: ${commit_msg}\n"
        }
    }

    if (!changeString) {
        changeString = 'No new commit in this build'
        return changeString
    // this.printMessage("${changeString}", 'green')
    }else {
        return changeString
    // this.printMessage("${changeString}", 'green')
    }
}

def Write_to_yaml(map_data, yaml_path) {
    // 将数据写入文件
    writeYaml file: yaml_path , data: map_data
}


// 获取镜像版本
def createVersion() {
  // 定义一个版本号作为当次构建的版本，输出结果 20191210175842_69
  return new Date().format('yyyyMMddHHmmss') + "_${env.BUILD_ID}"
}

// 获取时间
def getTime() {
  // 定义一个版本号作为当次构建的版本，输出结果 20191210175842
  return new Date().format('yyyyMMddHHmmss')
}

// 数组转字符串
def String listTostr(ArrayList array, int startIndex, int endIndex) {
  System.out.println(array)
  if (array == null) {
    return null
  }
  if (endIndex - startIndex <= 0) {
    return null
  }

  StringBuilder sb = new StringBuilder()
  for (int i = startIndex; i < endIndex; i++) {
    sb.append(array[i]).append(',')
  }
  return sb.substring(0, sb.length() - 1)
}