#基础镜像
FROM elasticsearch:8.5.2

#es插件目录
ENV ES_PLUGINS_PATH /usr/share/elasticsearch/plugins

#定义maven的安装目录
ENV MAVEN_BASE_PATH /opt

#定义编译ik分词器源码的目录
ENV IK_SRC_COMPILE_PATH /opt/ik_build


WORKDIR /usr/share/elasticsearch/plugins

COPY elasticsearch-analysis-ik-8.5.2.zip ik.zip

RUN unzip ik.zip -d ik && rm -rf ik.zip
	
VOLUME [ "/usr/share/elasticsearch/config/elasticsearch.yml", "/usr/share/elasticsearch/plugins","/usr/share/elasticsearch/data", "/usr/share/elasticsearch/logs"]
