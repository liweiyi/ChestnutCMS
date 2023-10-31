# ChestnutCMS v1.3.21

### 系统简介

ChestnutCMS是前后端分离的企业级内容管理系统。项目基于[RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)重构，集成[SaToken](https://gitee.com/dromara/sa-token)用户权限，[xxl-job](https://gitee.com/xuxueli0323/xxl-job)任务调度。支持站群管理、多平台静态化、元数据模型扩展、轻松组织各种复杂内容形态、多语言、全文检索。

### 系统预览

后台预览地址：<http://admin.1000mz.com>

账号：demo / a123456

企业站演示地址：<http://swikoon.1000mz.com>

> 企业演示站的静态资源已提交到仓库[chestnut-cms-wwwroot](https://gitee.com/liweiyi/chestnut-cms-wwwroot)。

资讯站演示地址：<http://news.1000mz.com>（会员演示账号：xxx333@126.com / a123456）

图片站演示地址：<http://tpz.1000mz.com>


### 开发环境
- JDK 17
- Maven 3.8
- MySQL 8.0
- Redis 6.x

### 主要技术框架

| 技术框架              | 版本      | 应用说明    |
|-------------------|---------|---------|
| Spring Boot       | 3.1.5   | 基础开发框架  |
| Spring Boot Admin | 3.1.7   | 监控框架    |
| Mybatis Plus      | 3.5.3.2 | ORM框架   |
| Flyway            | 9.22.3  | 数据库版本管理 |
| Yitter            | 1.0.6   | 雪花ID    |
| Redisson          | 3.24.1  | 分布式锁    |
| FreeMarker        | 2.3.32  | 模板引擎    |
| Sa-Token          | 1.37.0  | 权限认证    |
| Xxl-Job           | 2.4.0   | 任务调度    |
| Lombok            | 1.18.20 | 你懂的     |

### 相关文档


- [Wiki-快速上手](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B)
- [WiKi-常用配置](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E5%B8%B8%E7%94%A8%E9%85%8D%E7%BD%AE%E8%AF%B4%E6%98%8E)
- [WiKi-Docker部署说明](https://gitee.com/liweiyi/ChestnutCMS/wikis/Docker%E9%83%A8%E7%BD%B2%E8%AF%B4%E6%98%8E)
- [WiKi-站点访问配置](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E7%AB%99%E7%82%B9%E8%AE%BF%E9%97%AE%E9%85%8D%E7%BD%AE)
- [WiKi-使用手册](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E7%B3%BB%E7%BB%9F%E4%BD%BF%E7%94%A8%E6%89%8B%E5%86%8C/%E5%BB%BA%E7%AB%99%E6%B5%81%E7%A8%8B)
- [WiKi-模板手册](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E6%A8%A1%E6%9D%BF%E6%89%8B%E5%86%8C/%E6%A8%A1%E6%9D%BF%E5%85%A8%E5%B1%80%E5%8F%98%E9%87%8F%E8%AF%B4%E6%98%8E)
- [WiKi-二次开发手册](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E4%BA%8C%E6%AC%A1%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C/%E5%AE%9A%E6%97%B6%E4%BB%BB%E5%8A%A1)
- [WiKi-常见问题](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)
- [WiKi-版权声明](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E7%89%88%E6%9D%83%E5%A3%B0%E6%98%8E)
- [WiKi-免责声明](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E5%85%8D%E8%B4%A3%E5%A3%B0%E6%98%8E)
- [WiKi-版本变更日志](https://gitee.com/liweiyi/ChestnutCMS/wikis/%E7%89%88%E6%9C%AC%E5%8F%98%E6%9B%B4%E6%97%A5%E5%BF%97)

### 功能模块

| 模块           | 简介                                           |
|--------------|----------------------------------------------|
| 站点管理         | 多站点，支持图片水印、标题查重、扩展模型等扩展配置                    |
| 栏目管理         | 普通栏目+链接栏目，扩展配置优先级高于站点扩展配置                    |
| 内容管理         | 内容类型：文章+图片集+音视频集，页面部件：动态自定义区块+广告，内容回收站       |
| 资源管理         | 图片、音视频等各类静态资源管理，支持OSS/COS/MinIO对象存储          |
| 发布通道         | 支持多通道不同类型静态文件发布，可同时发布到PC、H5，html、json等       |
| 模板管理         | 静态化模板，支持在线编辑                                 |
| 模板指令         | FreeMarker自定义标签及模板函数的参数及用法说明                 |
| 文件管理         | 当前站点资源目录及发布通道静态化目录管理，支持文本在线编辑                |
| 扩展模型         | 站点、栏目及内容的动态模型扩展，系统默认数据表保存，支持自定义              |
| 词汇管理         | 热词、TAG词、敏感词、易错词                              |
| 内容索引         | 默认支持ElasticSearch+IK创建内容索引，支持标题内容全文检索        |
| 检索词库         | 自定义检索词库，支持扩展词和停用词动态扩展                        |
| 检索日志         | 用户搜索的日志记录，支持一键加入扩展词库                         |
| 友链管理         | 友情链接                                         |
| 广告管理         | 广告基于页面部件扩展的简单广告功能，支持权重及上下线时间配置，支持广告点击/展现日志统计 |
| 评论管理         | 基础功能模块                                       |
| 调查问卷         | 基础功能模块，默认支持文字类型单选、多选、输入                      |
| 自定义表单        | 基于元数据模块扩展，支持模板标签                             |
| 会员管理         | 支持自定义会员等级，等级经验值来源动态配置                        |
| 访问统计         | 对接百度统计API                                    |
| 用户管理         | 后台用户管理，支持用户独立权限配置                            |
| 机构管理         | 多级系统组织机构（公司、部门、小组）                           |
| 角色管理         | 支持按角色分配菜单权限、站点和栏目相关操作权限配置                    |
| 岗位管理         | 配置系统用户所属担任职务                                 |
| 菜单管理         | 配置系统菜单，操作权限，按钮权限标识等                          |
| 字典管理         | 对系统中经常使用的一些固定的数据进行维护，代码层面定义                  |
| 参数管理         | 对系统动态配置常用参数，代码层面定义                           |
| 通知公告         | 系统通知公告信息发布维护                                 |
| 安全配置         | 密码强度、密码过期、首次登陆强制修改、登陆异常策略配置                  |
| 国际化          | 为菜单等动态数据国际化配置提供基础支持，可覆盖后台代码配置                |
| 安全配置         | 密码强度、密码过期、首次登陆强制修改、登陆异常策略配置                  |
| 系统日志         | 统一日志管理，支持扩展                                  |
| 操作日志         | 系统操作日志扩展，记录操作参数、异常信息及请求耗时                    |
| 登录日志         | 系统登录日志扩展，记录用户登录日志，包含登录异常                     |
| 在线用户         | 当前系统中活跃用户状态监控，支持踢下线                          |
| 任务调度         | 基于XXL-JOB的分布式任务调度                            |
| 定时任务         | 基于Spring的TaskScheduler实现的单机定时任务              |
| 异步任务         | 异步任务状态查看，支持手动结束                              |
| 服务监控         | 监视当前系统CPU、内存、磁盘、堆栈等相关信息                      |
| 缓存监控         | 对系统的缓存信息查询，命令统计等                             |
| GroovyScript | 支持Groovy脚本在线执行                               |

### 开源协议补充说明

1. ChestnutCMS 遵循《Apache-2.0开源协议》，使用本系统不得用于违反国家有关政策的相关软件和应用。
2. 系统可免费商用，但是必须包含原始版权声明和许可声明（项目根目录下的LICENSE文件不可修改或删除），不可移除或修改后台登录页面底部的版权申明“Copyright © 2022-2023 ChestnutCMS (1000mz.com) All Rights Reserved.”。
3. 本项目所包含的第三方源码版权信息需另行标注。

### QQ交流群
群①：568506424
群②：643215654
统一入群口令：举个栗子

如果本项目对您有一丢丢小帮助 :kissing_heart: 点个小星星吧 :star2: