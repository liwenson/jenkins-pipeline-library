package com.test.jenkins.pipeline

class MPipeline {

    Script script

    MPipeline(script) {
        this.script = script
    }

    /**
     * 初始化资源库
     */
    def initializeResources() {
        this.getInfo()

        this.script.resource_load([
            script: this.script
        ])
        return this
    }

    def getInfo() {
        String[] str
        str = this.script.JOB_NAME.split('-')
        if (str.size() == 4) {
            this.script.env.project_type = str[0]
            this.script.env.project_org = str[1]
            this.script.env.project_name = str[2]
            this.script.env.project_app = str[3]
        } else if (str.size() == 5) {
            this.script.env.project_type = str[0]
            this.script.env.project_org = str[1]
            this.script.env.project_name = str[2]
            this.script.env.project_group = str[3]
            this.script.env.project_app = str[4]
        } else {
            throw new Exception('JOB_NAME 格式不符合')
        }
    }
}