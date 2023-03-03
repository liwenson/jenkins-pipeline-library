package com.pipeline.tools

//格式化输出
def PrintMes(value, color) {
  colors = ['red'   : "\033[40;31m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m",
              'blue'  : "\033[47;34m ${value} \033[0m",
              'green' : "\033[1;32m>>>>>>>>>>>\n${value}\n\033[0m",
              'green1' : "\033[40;32m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m" ]
  ansiColor('xterm') {
        println(colors[color])
  }
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
