# ES 版本，需与下方 IK 分词器版本保持一致
ARG ES_VERSION=9.4.2

# 基础镜像
FROM elasticsearch:${ES_VERSION}

# FROM 之后需重新声明 ARG 才能在后续指令中引用
ARG ES_VERSION

# ES 插件目录
ENV ES_PLUGINS_PATH=/usr/share/elasticsearch/plugins

# IK 分词器插件包（版本需与 ES_VERSION 一致）
WORKDIR /usr/share/elasticsearch/plugins

COPY elasticsearch-analysis-ik-${ES_VERSION}.zip ik.zip

RUN unzip ik.zip -d ik && rm -rf ik.zip
