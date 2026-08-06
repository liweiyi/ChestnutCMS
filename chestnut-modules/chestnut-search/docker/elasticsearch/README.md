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

