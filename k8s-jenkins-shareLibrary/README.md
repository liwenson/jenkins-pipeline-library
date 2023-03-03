
# 依赖的插件

```txt
ansible   # 用于执行ansible Playbook  非k8s 使用

AnsiColor    # 展示颜色

Groovy Postbuild    # 历史栏添加信息

Minio Plugin   # 归档  非k8s 使用

build user vars    # 用户信息

SonarQube Scanner   # 静态代码检查

OWASP Dependency-Track  # 代码漏洞检测

http Request    # http 请求

Pipeline Utility Steps  # 扩展流水线解析JSON数据

Kubernetes Continuous Deploy   # 部署应用到k8s  ,插件很久没有更新，新版k8s无用

```

# 配置凭证

```txt
gitlab               

Dependency-Track     secret txt

dockerhub
```

|软件|凭证类型|ID|描述|
|---|---|---|---|
|gitlab|Username with password|gitlab|...|
|Dependency-Track|secret txt|dependencytrack|...|
|harbor |Username with password |dockerhub|...|

# 配置k8s 共享库

#