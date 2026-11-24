# 构建 ES + IK 镜像

默认版本为 Elasticsearch 9.4.8。将同版本的 [IK 安装包](https://get.infini.cloud/elasticsearch/analysis-ik/9.4.8) 保存为本目录下的 `elasticsearch-analysis-ik-9.4.8.zip`，然后在本目录构建镜像。

```bash
docker build -t chestnutcms/elasticsearch-ik:9.4.8 .
```

更新离线发行包时，在仓库根目录运行 `./docker/offline/export-base-images.sh` 重新导出基础镜像归档。

# 启动前设置挂载目录权限
chown 1000:1000 -R elasticsearch/

# 启动后配置访问账号密码

```
# 1. 进入容器
docker exec -it elasticsearch bash

# 2. 初始化访问密码
cd /usr/share/elasticsearch/bin/
sh elasticsearch-setup-passwords interactive
```

