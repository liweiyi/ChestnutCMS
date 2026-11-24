# ChestnutCMS v1.6.4

[English](README_en.md) · [官方网站](https://www.1000mz.com/) · [Gitee](https://gitee.com/liweiyi/ChestnutCMS) · [GitHub](https://github.com/liweiyi/ChestnutCMS)

ChestnutCMS（栗子内容管理系统）是一套可自行部署的 Java 网站内容管理系统（CMS），开源版采用 Apache License 2.0。它适用于企业官网、分支机构站点、新闻资讯门户和多媒体内容网站，可通过同一套管理后台维护多个站点、栏目和内容，并使用 FreeMarker 发布静态 HTML 或 JSON 文件，或通过 REST API 向站点前端提供内容。

后端基于 Spring Boot，管理后台使用 Vue 3。项目由 RuoYi-Vue 重构而来，使用 Sa-Token 做身份认证和权限控制，集成 XXL-JOB 支持分布式任务调度。[RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) · [Sa-Token](https://gitee.com/dromara/sa-token) · [XXL-JOB](https://gitee.com/xuxueli0323/xxl-job)

## 适用场景

| 场景         | 使用方式                                                      |
|------------|-----------------------------------------------------------|
| 企业官网与站群    | 在一个后台维护品牌官网和分支机构站点，按站点和栏目配置内容、模板与操作权限。                    |
| 新闻资讯与多媒体门户 | 按栏目组织文章、图集、音频和视频，对内容建立索引，提供站内全文检索。                        |
| 多端支持  | 一套内容可以同时发布为 HTML、JSON、XML 等多种格式为多端提供内容，或通过 REST API 提供内容。 |

## 核心功能

| 功能          | ChestnutCMS 提供的能力                                                                                                                |
|-------------|----------------------------------------------------------------------------------------------------------------------------------|
| 站点与栏目       | 多站点管理，支持普通栏目和链接栏目。站点和栏目可配置图片水印、标题查重和扩展模型。同一配置项可由栏目覆盖站点设置。                                                                        |
| 内容与扩展模型     | 管理文章、图集、音频和视频，支持回收站。可为站点、栏目和内容添加结构化字段，例如为产品介绍配置型号和规格。                                                                            |
| 模板与发布       | 在线编辑 FreeMarker 模板及站点文本文件，提供模板标签与函数。不同发布通道可设置各自的访问地址、模板和文件后缀，生成 HTML、JSON 等文件。支持动态模板。                                            |
| 资源与页面部件     | 管理图片、音视频和其他文件，支持本地文件系统、阿里云 OSS、腾讯云 COS、MinIO 和 Amazon S3。页面可配置自定义区块、友情链接和广告，广告支持权重、定时上下线及点击与展现统计。                                |
| 内容检索与词汇     | 使用 Elasticsearch 和 IK 中文分词插件索引内容标题与正文，提供扩展词、停用词和搜索日志。可管理热词、标签词、敏感词和易错词，并在文章编辑时检测和替换敏感词与易错词。                                      |
| 会员与互动       | 支持会员等级、经验值、基础订阅、关注、签到、收藏、点赞、评论、调查问卷和自定义表单。 |
| 权限、安全与国际化   | 管理后台用户、组织机构、岗位和角色，按菜单、操作、站点和栏目授权。可配置密码强度和有效期、首次登录改密及登录异常策略。支持中英文后台界面及菜单等动态数据的国际化配置。 |
| 站点运营与系统管理   | 提供站点访问统计、百度统计 API 对接、系统日志、操作日志、登录日志、在线用户管理、服务与缓存监控。支持字典、系统参数、通知公告、单机定时任务、XXL-JOB 调度、异步任务查看和 Groovy 脚本执行。消息模块支持邮件、短信、钉钉机器人和飞书机器人。 |

## 在线演示

| 演示 | 访问地址 | 公共演示账号 |
| --- | --- | --- |
| CMS 管理后台 | [管理后台](https://admin.1000mz.com) | `demo` / `a123456` |
| 企业官网 | [思威控企业站](https://swikoon.1000mz.com) | |
| 资讯门户 | [资讯站](https://news.1000mz.com) | 会员 `xxx333@126.com` / `a123456` |
| 图片网站 | [桌面端](https://tpz.1000mz.com) · [移动端](https://mtpz.1000mz.com) | |
| 游戏网站 | [桌面端](https://game.1000mz.com) · [移动端](https://mgame.1000mz.com) | |
| 影视网站 | [影视站（桌面端及移动端）](https://movie.1000mz.com) | |
| 科技企业官网 | [科技企业站](https://demo3.1000mz.com) | |

[更多站点与主题演示](https://www.1000mz.com/themes/)

## 本地运行

### 1. 准备开发环境

安装 OpenJDK 17、Maven、MySQL 8.0、Redis 7.x、Node.js 20.19.4 和 npm。

在 MySQL 中创建空数据库，然后在 `application-dev.yml` 中配置 `spring.datasource.dynamic.datasource.master` 和 `spring.data.redis`。全新数据库可设置 `spring.flyway.enabled=true`，由 Flyway 在后端启动时执行 MySQL 迁移脚本。项目的开发配置默认关闭 Flyway 和 XXL-JOB。[开发配置](chestnut-admin/src/main/resources/application-dev.yml) · [MySQL 迁移脚本](chestnut-admin/src/main/resources/db/migration/mysql/)

```sql
CREATE DATABASE chestnut_cms CHARACTER SET utf8mb4;
```

### 2. 启动后端

在仓库根目录构建并运行 Spring Boot 应用，或在 IDE 中直接运行 `com.chestnut.ChestnutApplication`。开发配置的后端端口为 `9080`。[Java 启动类](chestnut-admin/src/main/java/com/chestnut/ChestnutApplication.java)

```bash
mvn -pl chestnut-admin -am -Dmaven.test.skip=true package
java -jar chestnut-admin/target/chestnut-admin.jar --spring.profiles.active=dev
```

### 3. 启动管理前端

在另一个终端从仓库根目录执行以下命令。然后打开 [http://localhost:89/admin/](http://localhost:89/admin/)，Vite 会将 `/dev-api` 请求代理到后端。全新初始化的数据库默认管理账号为 `admin` / `admin`，请在部署时修改默认密码。[Vite 配置](chestnut-ui-vue3/vite.config.js)

```bash
cd chestnut-ui-vue3
npm install
npm run dev
```

首次发布可在后台创建站点、栏目和发布通道，为通道配置访问地址和模板，然后发布首页或内容。[建站流程](https://www.1000mz.com/docs/manual/668421334257733.shtml) · [站点访问配置](https://www.1000mz.com/docs/deploy/668421333463109.shtml)

## 部署与文档

管理前端通过 `npm run build:prod` 构建，产物位于 `chestnut-ui-vue3/dist/`。生产环境的后端配置、静态文件目录和 Nginx 转发规则请参阅对应的部署指南。

| 任务 | 文档 |
| --- | --- |
| 配置本地环境与站点参数 | [快速上手](https://www.1000mz.com/docs/quickstart/668421333332037.shtml) · [常用配置](https://www.1000mz.com/docs/quickstart/668421333397573.shtml) |
| 部署后端与管理前端 | [JAR 部署](https://www.1000mz.com/docs/deploy/668421334356037.shtml) · [Docker 部署](https://www.1000mz.com/docs/deploy/668421333434437.shtml) · [Nginx 站点访问配置](https://www.1000mz.com/docs/deploy/668421333463109.shtml) |
| 建站、制作和编辑模板 | [建站流程](https://www.1000mz.com/docs/manual/668421334257733.shtml) · [FreeMarker 模板手册](https://www.1000mz.com/docs/template/variables/668421333688389.shtml) |
| 排查问题与查看更新 | [常见问题](https://www.1000mz.com/docs/quickstart/668421333995589.shtml) · [版本变更日志](https://www.1000mz.com/changlog/) |

## 技术栈与源码入口

| 目录 | 主要职责与技术 |
| --- | --- |
| [chestnut-admin](chestnut-admin/) | Spring Boot 4.0.7 后端启动模块，包含运行配置与 Flyway 11.14.1 数据库迁移。 |
| [chestnut-ui-vue3](chestnut-ui-vue3/) | 管理后台，使用 Vue 3.5.16、Element Plus 2.11.4 和 Vite 6.3.5。 |
| [chestnut-cms](chestnut-cms/) | CMS 业务模块，包含站点、栏目、内容、发布通道、扩展模型、检索等。核心逻辑位于 [chestnut-cms-contentcore](chestnut-cms/chestnut-cms-contentcore/)。 |
| [chestnut-modules](chestnut-modules/) | 通用业务模块，包含系统管理、会员、元数据、消息和搜索。 |
| [chestnut-common](chestnut-common/) | 共享组件，包含数据访问、缓存、对象存储和模板静态化。使用 MyBatis-Plus 3.5.17、Redisson 4.6.1 和 FreeMarker 2.3.34。 |
| [docker](docker/) | Docker Compose 配置与部署文件。 |

后端鉴权使用 Sa-Token 1.45.0，监控可接入 Spring Boot Admin 4.0.4，编译使用 Lombok 1.18.46。项目版本与依赖声明见 [根 Maven POM](pom.xml) 和 [前端 package.json](chestnut-ui-vue3/package.json)。

## 常见问题

### 站点、栏目和发布通道有什么关系？

站点是内容与资源的管理单位，栏目组织站点中的导航和内容。发布通道决定内容使用哪套模板生成文件，以及生成的文件通过什么地址访问。例如，企业站可以在「新闻」栏目维护同一篇文章，再通过桌面端和移动端两个通道发布。

### 自定义前端如何获取已发布的内容？

自定义前端可以读取发布通道生成的 JSON 文件，也可以调用 `GET /api/cms/content/list`。调用列表接口时传入站点 ID `sid` 和发布通道编码 `pp`。[内容 API 源码](chestnut-cms/chestnut-cms-contentcore/src/main/java/com/chestnut/contentcore/controller/front/ContentApiController.java)

### 本地运行一定要装 Elasticsearch 或 XXL-JOB 吗？

本地开发的基本运行依赖是 MySQL 和 Redis。使用全文检索时，需要 Elasticsearch、同版本的 IK 插件，并配置 `spring.elasticsearch`。使用 XXL-JOB 分布式调度时，需要部署调度中心并设置 `xxl.job.enable=true`。[Elasticsearch 与 IK 镜像构建](chestnut-modules/chestnut-search/docker/elasticsearch/README.md)

## 许可证与社区

ChestnutCMS 的软件著作权登记号为 `2023SR1343023`。[开源许可证](LICENSE) · [NOTICE](NOTICE) · [版权声明](https://www.1000mz.com/docs/others/668421333913669.shtml) · [免责声明](https://www.1000mz.com/docs/others/668421333942341.shtml)

- 问题反馈可提交至 [Gitee Issues](https://gitee.com/liweiyi/ChestnutCMS/issues) 或 [GitHub Issues](https://github.com/liweiyi/ChestnutCMS/issues)。
- QQ 交流群的入群口令为 `烤个栗子`。[568506424](https://qm.qq.com/q/rOw3kwePg) · [1022939737](https://qm.qq.com/q/CDrFdmqa1G)