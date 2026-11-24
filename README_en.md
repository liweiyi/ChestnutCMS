# ChestnutCMS

[简体中文](README.md) · [Official website](https://www.1000mz.com/) · [Gitee](https://gitee.com/liweiyi/ChestnutCMS) · [GitHub](https://github.com/liweiyi/ChestnutCMS)

ChestnutCMS (栗子内容管理系统) is a self-hosted Java website content management system (CMS). Its open-source edition uses the Apache License 2.0. It is designed for corporate websites, branch sites, news portals, and multimedia websites. Editors can manage multiple sites, sections, and content items from a single admin console. The CMS uses FreeMarker to publish static HTML or JSON and provides REST APIs for site frontends.

The backend runs on Spring Boot, and the admin console uses Vue 3. The project was rebuilt from RuoYi-Vue, uses Sa-Token for authentication and authorization, and integrates XXL-JOB for distributed job scheduling. [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) · [Sa-Token](https://gitee.com/dromara/sa-token) · [XXL-JOB](https://gitee.com/xuxueli0323/xxl-job)

## Use cases

| Use case | How ChestnutCMS is used |
| --- | --- |
| Corporate websites and multisite management | Manage a corporate site and branch sites in one admin console, with content, templates, and permissions configured by site and section. |
| News and multimedia portals | Organize articles, galleries, audio, and video into sections, then index content for on-site full-text search. |
| Multiple clients | Publish the same content as static HTML, JSON, or XML for different clients, or deliver it through REST APIs. |

## Core features

| Feature | What ChestnutCMS provides |
| --- | --- |
| Sites and sections | Manage multiple sites with standard and link sections. Configure image watermarks, duplicate-title checks, and extension models. A section can override the corresponding site settings. |
| Content and extension models | Manage articles, galleries, audio, and video, with a recycle bin. Add structured fields to sites, sections, and content items, such as model numbers and specifications for product pages. |
| Templates and publishing | Edit FreeMarker templates and site text files in the admin console. Use custom template directives and functions. Set individual URLs, templates, and file extensions for publishing channels to generate formats such as HTML and JSON. Dynamic templates are also supported. |
| Media and page components | Manage images, audio, video, and other files using the local filesystem, Alibaba Cloud OSS, Tencent Cloud COS, MinIO, or Amazon S3. Add custom page blocks, link lists, and ads. Ads support weights, scheduled activation and deactivation, and click and impression reporting. |
| Content search and terminology | Index titles and body text with Elasticsearch and the IK Chinese analysis plugin. Manage custom search terms, stop words, and search logs. Maintain trending terms, tags, sensitive terms, and common misspellings, with detection and replacement for sensitive terms and misspellings in the article editor. |
| Membership and engagement | Configure member levels, experience points, and basic subscriptions. Support follows, check-ins, bookmarks, likes, comments, surveys, and custom forms. |
| Permissions, security, and localization | Manage admin users, departments, positions, and roles. Grant permissions by menu, action, site, and section. Configure password strength and expiry, mandatory password changes at first login, and rules for failed logins. The admin console supports Chinese and English, with localization settings for dynamic data such as menus. |
| Site operations and system administration | Access site traffic statistics, the Baidu Analytics API integration, system logs, audit logs, login logs, online-user management, and service and cache monitoring. Manage dictionaries, settings, and announcements, and use local scheduling, XXL-JOB, asynchronous task tracking, and Groovy script execution. Messaging supports email, SMS, DingTalk bots, and Feishu bots. |

## Live demos

| Demo | URL | Public demo credentials |
| --- | --- | --- |
| CMS admin console | [Admin console](https://admin.1000mz.com) | `demo` / `a123456` |
| Corporate website | [Swikoon website](https://swikoon.1000mz.com) | |
| News portal | [News site](https://news.1000mz.com) | Member `xxx333@126.com` / `a123456` |
| Image website | [Desktop](https://tpz.1000mz.com) · [Mobile](https://mtpz.1000mz.com) | |
| Gaming website | [Desktop](https://game.1000mz.com) · [Mobile](https://mgame.1000mz.com) | |
| Film and TV website | [Film and TV site (desktop and mobile)](https://movie.1000mz.com) | |
| Technology company website | [Technology company site](https://demo3.1000mz.com) | |

[More site and theme demos](https://www.1000mz.com/themes/)

## Run locally

### 1. Set up the development environment

Install OpenJDK 17, Maven, MySQL 8.0, Redis 7.x, Node.js 20.19.4, and npm.

Create an empty MySQL database, then set `spring.datasource.dynamic.datasource.master` and `spring.data.redis` in `application-dev.yml`. For a fresh database, set `spring.flyway.enabled=true` to run the MySQL migrations on backend startup. The development profile disables Flyway and XXL-JOB by default. [Development configuration](chestnut-admin/src/main/resources/application-dev.yml) · [MySQL migrations](chestnut-admin/src/main/resources/db/migration/mysql/)

```sql
CREATE DATABASE chestnut_cms CHARACTER SET utf8mb4;
```

### 2. Start the backend

Build and run the Spring Boot application from the repository root, or run `com.chestnut.ChestnutApplication` from your IDE. The development profile uses backend port `9080`. [Java application entry point](chestnut-admin/src/main/java/com/chestnut/ChestnutApplication.java)

```bash
mvn -pl chestnut-admin -am -Dmaven.test.skip=true package
java -jar chestnut-admin/target/chestnut-admin.jar --spring.profiles.active=dev
```

### 3. Start the admin frontend

In a separate terminal, run the following commands from the repository root. Then open [http://localhost:89/admin/](http://localhost:89/admin/). Vite proxies `/dev-api` requests to the backend. The default admin credentials for a freshly initialized database are `admin` / `admin`. Change the default password when deploying. [Vite configuration](chestnut-ui-vue3/vite.config.js)

```bash
cd chestnut-ui-vue3
npm install
npm run dev
```

To publish your first site, create a site, a section, and a publishing channel in the admin console. Set the channel URL and template, then publish the home page or a content item. [Site setup (Chinese)](https://www.1000mz.com/docs/manual/668421334257733.shtml) · [Site access configuration (Chinese)](https://www.1000mz.com/docs/deploy/668421333463109.shtml)

## Deployment and documentation

Run `npm run build:prod` to build the admin frontend into `chestnut-ui-vue3/dist/`. The deployment guides cover production backend configuration, static file directories, and Nginx routing.

| Task | Documentation |
| --- | --- |
| Set up a local environment and site parameters | [Quick start (Chinese)](https://www.1000mz.com/docs/quickstart/668421333332037.shtml) · [Common settings (Chinese)](https://www.1000mz.com/docs/quickstart/668421333397573.shtml) |
| Deploy the backend and admin frontend | [JAR deployment (Chinese)](https://www.1000mz.com/docs/deploy/668421334356037.shtml) · [Docker deployment (Chinese)](https://www.1000mz.com/docs/deploy/668421333434437.shtml) · [Nginx site access (Chinese)](https://www.1000mz.com/docs/deploy/668421333463109.shtml) |
| Set up a site and develop templates | [Site setup (Chinese)](https://www.1000mz.com/docs/manual/668421334257733.shtml) · [FreeMarker template reference (Chinese)](https://www.1000mz.com/docs/template/variables/668421333688389.shtml) |
| Troubleshoot and review updates | [FAQ (Chinese)](https://www.1000mz.com/docs/quickstart/668421333995589.shtml) · [Changelog (Chinese)](https://www.1000mz.com/changlog/) |

## Technology stack and repository layout

| Directory | Responsibilities and technologies |
| --- | --- |
| [chestnut-admin](chestnut-admin/) | Spring Boot 4.0.7 application entry point, runtime configuration, and Flyway 11.14.1 database migrations. |
| [chestnut-ui-vue3](chestnut-ui-vue3/) | Admin console built with Vue 3.5.16, Element Plus 2.11.4, and Vite 6.3.5. |
| [chestnut-cms](chestnut-cms/) | CMS modules for sites, sections, content, publishing channels, extension models, and search. The core implementation lives in [chestnut-cms-contentcore](chestnut-cms/chestnut-cms-contentcore/). |
| [chestnut-modules](chestnut-modules/) | Shared application modules for system administration, membership, metadata, messaging, and search. |
| [chestnut-common](chestnut-common/) | Shared components for data access, caching, object storage, and static rendering. Uses MyBatis-Plus 3.5.17, Redisson 4.6.1, and FreeMarker 2.3.34. |
| [docker](docker/) | Docker Compose configuration and deployment files. |

The backend uses Sa-Token 1.45.0 for authentication and Lombok 1.18.46 at compile time. It supports monitoring with Spring Boot Admin 4.0.4. See the project version and dependency declarations in the [root Maven POM](pom.xml) and [frontend package.json](chestnut-ui-vue3/package.json).

## FAQ

### How do sites, sections, and publishing channels fit together?

A site groups content and assets. Sections organize its navigation and content. A publishing channel determines which templates generate the files and which URL serves them. For example, a corporate site can maintain one article in its News section and publish it through separate desktop and mobile channels.

### How does a custom frontend retrieve published content?

A custom frontend can read JSON files generated by a publishing channel or call `GET /api/cms/content/list` with the site ID `sid` and publishing channel code `pp`. [Content API source](chestnut-cms/chestnut-cms-contentcore/src/main/java/com/chestnut/contentcore/controller/front/ContentApiController.java)

### Do I need Elasticsearch or XXL-JOB to run ChestnutCMS locally?

MySQL and Redis are the core local dependencies. Full-text search requires Elasticsearch, a matching version of the IK plugin, and `spring.elasticsearch` configuration. Distributed scheduling requires an XXL-JOB admin server and `xxl.job.enable=true`. [Build the Elasticsearch and IK image (Chinese)](chestnut-modules/chestnut-search/docker/elasticsearch/README.md)

## License and community

The ChestnutCMS software copyright registration number is `2023SR1343023`. [Open-source license](LICENSE) · [NOTICE](NOTICE) · [Copyright statement (Chinese)](https://www.1000mz.com/docs/others/668421333913669.shtml) · [Disclaimer (Chinese)](https://www.1000mz.com/docs/others/668421333942341.shtml)

- Report issues on [Gitee Issues](https://gitee.com/liweiyi/ChestnutCMS/issues) or [GitHub Issues](https://github.com/liweiyi/ChestnutCMS/issues).
- Use the passphrase `烤个栗子` to join either QQ group. [568506424](https://qm.qq.com/q/rOw3kwePg) · [1022939737](https://qm.qq.com/q/CDrFdmqa1G)