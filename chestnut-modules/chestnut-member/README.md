# 会员模块

本模块提供会员管理与基础订阅服务。订阅类位于 `com.chestnut.member` 下的 controller、domain、service、mapper 等共用包。

## 2026-10-29 基础订阅功能开源

现有HTTP路径、权限键、表名和会员Hash缓存键保持一致；定义对象缓存改用 `cc:member:subscribe:definition:` 前缀，迁移后按新Java包名回源。

## 2026-10-27 订阅结束时间改为秒时间戳

当前 `MemberSubscribeRecord.expireTime`、审计日志 `beforeExpireTime` / `afterExpireTime` 和会员订阅缓存中的结束时间均为UTC纪元秒。首次关联、已过期订阅按操作时间的整数秒计算；未过期订阅沿用原结束秒值，再加减 `deltaSeconds`。结束时间不晚于当前秒即为过期。

`MemberSubscribeRecord.createTime` 与 `MemberSubscribeLog.operationTime` 继续使用UTC纪元毫秒。前端仅对结束时间按秒转换展示，操作时间仍按毫秒展示。会员结束时间缓存使用 `cc:member:subscribe:record:seconds:` 前缀，避免复用旧毫秒缓存。SQL建表注释和三语接口字段说明已同步。

同源回执查询和结束时间调整在同一独立写事务内执行。会员Hash缓存仅在未命中时使用独立只读事务回源，隔离调用方事务的旧快照和MyBatis一级缓存；缓存命中时不创建回源事务。Hash读取为空按未命中重新加载，有数据时按原TTL回填。

既有毫秒数据不能直接按秒解释；若保留旧订阅记录，需要一次性转换记录结束时间和审计前后结束时间，创建时间与操作时间保持原值。历史实现及验收记录中的时间单位以上述约定为准。

## 2026-10-23 简化后的前端适配

本轮对用户简化后的接口进行审查并适配前端。会员订阅查询、新增关联和时长调整统一使用 `subscriptionId`，通过可搜索的订阅定义选择器按名称选择，不再手工输入订阅key。记录页按ID批量回显定义名称，选中项不受默认前100条选项限制。

用户本轮确认采用新的时间规则：未过期订阅沿用原结束时间，过期订阅从当前操作时间计算，即 `base = max(oldExpiry, now)`，再加减 `deltaSeconds`。前端提示按该规则更新。会员ID和秒数保留十进制字符串；增加秒数按新请求的 `@Max(316224000)` 校验，会员ID的Long范围不受该时长上限影响。

### 当前HTTP契约

| 方法与路径 | 前端适配 |
| --- | --- |
| GET /member/subscription/definition/list | 名称筛选；展示definitionId及status |
| GET /member/subscription/definition/detail/{subscriptionId} | UUID字符串，修正错误的Long参数校验 |
| GET /member/subscription/definition/serviceItems | 返回id/name/description，前端映射服务项ID |
| GET /member/subscription/definition/options | 返回value=定义ID、label=名称；keyword搜索，ids逗号分隔批量回显，每批最多100个 |
| POST /member/subscription/definition/create、/update | 提交名称、说明、serviceIds、remark；编辑以subscriptionId提交定义ID，主键由系统生成 |
| POST /member/subscription/definition/changeStatus | 提交subscriptionId及status：0启用、1禁用 |
| POST /member/subscription/definition/delete | 查询参数definitionId传定义ID |
| GET /member/subscription/record/list | memberId、subscriptionId及valid=Y/N；按UTC纪元秒比较，结束时间相等时为过期 |
| POST /member/subscription/record/adjust | 传requestId/memberId/subscriptionId/deltaSeconds/reason；手动来源由控制器覆盖，sourceId使用requestId |
| GET /member/subscription/log/list | memberId、subscriptionId筛选；显示秒数、前后时间、操作人ID及类型、来源和原因 |

后端已移除撤销接口，前端移除相应按钮。当前审计实体不再提供requestId、action、operatorName，界面不再提交或展示这些旧字段。定义的服务范围从已创建的订阅服务项中选择，使用支持ID和名称搜索的下拉多选；已选项通过ids批量回显，不受默认前100条限制。

请求超时后，同内容重试复用requestId。Controller、服务接口和实现统一返回MemberSubscribeLog；同一会员相同sourceType/sourceId返回首次审计回执，不比较重试参数内容，也不会再次调整时长；不同会员使用相同来源互不冲突。

### 统一迁移及定义选项

`V1.6.4__update.sql` 定义了四张基础订阅表：`cc_member_subscribe_service`（service_id）、`cc_member_subscribe_definition`（definition_id）、`cc_member_subscribe`（record_id）、`cc_member_subscribe_log`（log_id）。定义使用JSON数组保存service_ids，包含status/remark，审计时间与BaseEntity一致使用日期时间类型；会员结束点及审计前后结束时间使用UTC纪元秒，创建与操作时间继续使用UTC毫秒。迁移未创建非主键唯一约束和外键约束，业务层需要负责唯一性、幂等性及关联校验。旧范围关系表、版本和旧审计字段已从未发布迁移中移除。

### 订阅服务项管理

管理入口为会员管理下的“订阅配置”，通过“订阅定义”和“订阅服务项”两个标签页分别管理。配置菜单使用 `member:subscription:config:view` 作为统一查询权限，两个标签页共用该入口；服务项新增、编辑和删除沿用订阅定义的对应权限，不再单独配置服务项CRUD或订阅定义查看权限。菜单提供简体、英文、繁体名称。接口前缀为 `/member-subscribe-service`：`GET /list`、`GET /detail/{id}`、`GET /options`、`POST /add`、`POST /update`、`POST /delete`（ID数组）。列表和选项支持keyword按ID或名称模糊查询，选项还支持ids批量回显。

新增时选择“系统内置”或“自定义”。内置项从 `/member/subscription/definition/serviceItems` 选择，名称由后端获取；自定义项填写1～100位大小写字母、数字、下划线组成的serviceId和不超过100字的必填名称。serviceId创建后固定，内置项只能修改remark，自定义项可以修改name和remark，remark最多400字。已被订阅定义引用的服务项不能删除。定义保存校验已创建的服务项，允许自定义项，无需注册Spring Bean。

### 业务服务项注册

当前接口为 `IMemberSubscribeServiceItem`。实现提供服务ID、名称、描述；定义服务通过 `IMemberSubscribeServiceItem.BEAN_PREFIX + serviceId` 查找Spring Bean，业务模块需按此前缀命名Bean。
