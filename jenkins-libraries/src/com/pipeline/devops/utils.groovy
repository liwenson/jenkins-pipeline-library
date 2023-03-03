package com.pipeline.devops

import com.data.devops.utils

// import hudson.model.User

/**
 * 检出项目代码
 * @param srcUrl
 * @param branchName
 * @return
 */
def checkOutCode(config) {
    // def utils = new utils()
    this.printMessage('获取代码', 'green')
    this.printMessage(config.info.gitCredentialsId, 'green')
    checkOut(config.info.repository_url, config.info.branch, config.info.gitCredentialsId)
}

/**
 * 检出代码
 * @param srcUrl
 * @param branchName
 * @return
 */

def checkOut(srcUrl, branchName, credentialsId) {
    checkout([$class                           : 'GitSCM', branches: [[name: "${branchName}"]],
              doGenerateSubmoduleConfigurations: false,
              extensions                       : [],
              submoduleCfg                     : [],
              userRemoteConfigs                : [[credentialsId: "${credentialsId}", url: "${srcUrl}"]]])
}

/**
 * 格式化输出
 * @param value
 * @param color
 * @return
 */
def printMessage(value, color) {
    def colors = ['groovy': "\033[40;31m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m",
                  'blue'  : "\033[47;34m ${value} \033[0m",
                  'green' : "\033[1;32m>>>>>>>>>>>\n${value}\n\033[0m",
                  'green1': "\033[40;32m >>>>>>>>>>> ${value} <<<<<<<<<<< \033[0m"]
    ansiColor('xterm') {
        println(colors[color])
    }
}

/**
 * 输出项目信息
 * @param value
 * @return
 */
def show(config) {
    // this.printMessage('获取代码', 'green')
    // def user = User.current()

    def branch = config.info.Branch
    def envs = config.info.ENVIR
    def repository = config.info.repository_url
    def gitChange = this.getChangeString()
    wrap([$class: 'BuildUser']) {
        script {
            BUILD_USER_ID = "${env.BUILD_USER_ID}"
            BUILD_USER = "${env.BUILD_USER}"
        }
    }

    def content = 'GIT地址: ' + repository + '\n构建用户: ' + BUILD_USER + '\n构建分支: ' + branch + '\n构建环境: ' + envs + '\n' + gitChange
    this.printMessage(content, 'green')

    // 构建历史中插入信息
    manager.addShortText("分支: ${branch}  环境: ${envs}",'grey', 'white', '0px', 'white')
}

/**
 * 获取git commit变更集
 * @param value
 * @param color
 * @return
 */

// @NonCPS
// def getChangeString() {
//     // def result = []
//     def changeString = []
//     // def authors = []
//     def MAX_MSG_LEN = 20
//     def changeLogSets = currentBuild.changeSets
//     for (int i = 0; i < changeLogSets.size(); i++) {
//         def entries = changeLogSets[i].items
//         for (int j = 0; j < entries.length; j++) {
//             def entry = entries[j]
//             truncatedMsg = entry.msg.take(MAX_MSG_LEN)
//             commitTime = new Date(entry.timestamp).format('yyyy-MM-dd HH:mm:ss')
//             changeString << "${truncatedMsg} [${entry.author} ${commitTime}]\n"
//         // authors << "${entry.author} "
//         }
//     }

//     if (!changeString) {
//         changeString = '-> No new changes'
//         // authors = 'No new changes, No authors'
//         // result << changeString << authors
//         // result << changeString
//         this.printMessage("${changeString}", 'green')
//         // return result
//     } else {
//         if (changeString.size() > 5) {
//             changeString = changeString[0, 4]
//             changeString.add('......')
//         }
//         changeString = '-> ' + changeString.join('\n')
//         // authors.join(', ')
//         // result << changeString << authors.unique()
//         this.printMessage("${changeString}", 'green')
//         // result << changeString
//         // return result
//     }
// }

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

// 清理项目空间
def clean(config) {
    def util = new utils()

    if (config.info.Module != 'no_delete' ) {
        util.printMessage('清理项目空间', 'green')
        try {
            deleteDir()

            this.info(config)
        }catch (err) {
            throw err
        }
    }
}

// debug
def info(config) {
    def util = new utils()

    if ( config.info.debug.equals('true') ) {
        util.printMessage('Debug:\n' + config.info, 'green')
    }
}

def getRepositoryName(config) {
    def tmpName = config.repository_url.split('/')
    def RepositoryName = tmpName[tmpName.length - 1]
    config.repository_name = '.*' + RepositoryName
}

@NonCPS
def setDescription(config) {
    echo env.JOB_NAME
    // 设置描述信息
    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
    item.setDescription(config.info.desc)
    item.save()
}
