# 图书借阅管理系统 设计说明书

## 1. 体系结构设计

### 1.1 体系结构风格

系统采用 **分层架构 + MVC**：

* 表现层（View）：后台与读者端均使用 AdminLTE + Vue 2 的页面，读者端页面位于 `views/reader/`；
* 控制层（Controller）：`com.kinnong.modules.*.controller` 负责参数接收、权限注解、调用服务、封装 `R` 统一响应；
* 业务层（Service）：`service/impl` 承担事务、业务规则（库存校验、押金校验、审核状态机）；
* 数据访问层（Dao/Mapper）：MyBatis 映射 XML 与 `BaseDao` 通用 CRUD；
* 横切层：`web.interceptor.AuthorizationInterceptor`（JWT + 角色）、`RExceptionHandler`（统一异常）、`JwtUtils`、`MD5Util`、`Query` 分页工具。

选择理由：项目规模中等，团队对 Spring Boot 分层模式熟悉；分层后新增“角色权限”和“审核”功能只需在
横切层与 Service 层扩展，符合开闭原则，也便于单元测试与后续维护。

### 1.2 逻辑视图（模块划分）

```
┌───────────────────────────── 客户端 ─────────────────────────────┐
│  读者端（浏览器 AdminLTE+Vue）    后台管理端（浏览器 AdminLTE+Vue） │
└───────────────┬──────────────────────────────┬──────────────────┘
                │ HTTP/JSON + JWT              │
┌───────────────▼──────────────────────────────▼──────────────────┐
│                     表现层 / 控制层 Controller                    │
│  /api/**（读者接口，@AuthIgnore 控制）  /sys /book /order ...      │
├─────────────────────────────────────────────────────────────────┤
│ 横切：AuthorizationInterceptor（Token + @RequireRole 角色校验）    │
│       RExceptionHandler（401/403/500 统一响应）                   │
├─────────────────────────────────────────────────────────────────┤
│                       业务层 Service                             │
│  MemberService  OrderService  BookService  DepositRefundService  │
│  SysUserService  StatisticsService ...                           │
├─────────────────────────────────────────────────────────────────┤
│                       数据访问层 Dao/Mapper                      │
│  MyBatis Mapper XML + BaseDao<T> 通用分页/CRUD                    │
├─────────────────────────────────────────────────────────────────┤
│                     MySQL 8.0（a_book 库，13 张表）               │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 部署视图

* 应用服务器：单机 Spring Boot 内嵌 Tomcat（默认 8080 端口），可打为可执行 JAR；
* 数据库服务器：MySQL 8.0（本机 3306），Druid 连接池（初始 10、最大 100）；
* 静态资源：由 Spring Boot 直接提供（`classpath:/static/`、`classpath:/views/`）与文件上传目录 `c:/file`；
* 客户端：浏览器访问后台页面与读者端页面；读者端页面通过 `/api/**` 接口读写数据。

部署关系：客户端 --HTTP--> 应用（Tomcat）--JDBC--> MySQL；上传文件 --> 本地磁盘 `c:/file`。

## 2. 用户界面设计

### 2.1 界面清单

| 界面 | 参与者 | 主要元素 |
| --- | --- | --- |
| 后台登录页 | 管理员、审核人员 | 账号、密码、验证码（可配置）、登录按钮、错误提示 |
| 统计分析首页 | 管理员 | 用户数/图书数/借阅数卡片、分类借阅柱状图 |
| 押金退还审核页 | 审核人员、管理员 | 申请列表（读者、金额、状态、时间）、通过/拒绝按钮、审核备注框 |
| 图书管理页 | 管理员 | 查询条件、图书表格、新增/编辑弹窗、上下架 |
| 借阅管理页 | 管理员 | 借阅单列表、详情、办理归还 |
| 管理员管理页 | 管理员 | 后台账号列表、角色列、新增/编辑（用户名、密码、角色、状态） |

### 2.2 界面跳转

```
登录页 --管理员--> 统计分析首页 --菜单--> 各管理页
  └──审核人员--> 押金退还审核页（菜单仅此一项）
```

### 2.3 设计原则

* 角色可见性：审核人员看不到管理员菜单，登录后直接进入审核工作台，减少误操作；
* 一致性：沿用 AdminLTE 的配色、卡片、表格与弹窗组件；删除/审核等敏感操作二次确认；
* 反馈：所有操作成功/失败通过统一 `alert`/`confirm` 组件提示；401 自动跳转登录、403 提示无权限。

## 3. 详细设计

### 3.1 角色权限设计（本次新增）

```
@RequireRole(ADMIN)                     类/方法级注解
        │
AuthorizationInterceptor.preHandle
        │ 1) @AuthIgnore ? 放行
        │ 2) 解析 JWT -> userId
        │ 3) 读取 @RequireRole
        │ 4) SysUserService.queryObject(userId) 取 role
        │ 5) 角色匹配 ? 放行 : 抛 RException(403)
        ▼
RExceptionHandler -> HTTP 403 {"code":403,"msg":"没有访问该功能的权限"}
```

* 角色实时读库：管理员修改账号角色后立即生效，无需重新登录；
* 账号被禁用（status=0）时即使 token 未过期也无法访问受控接口。

### 3.2 关键类职责

| 类名称 | 所属模块 | 主要职责 | 关键方法 |
| --- | --- | --- | --- |
| `AuthorizationInterceptor` | web | 校验 token、解析 userId、按注解校验角色 | `preHandle`、`hasRole` |
| `RequireRole` | common.annotation | 声明接口所需角色 | `value()` |
| `RoleConstant` | common.constant | 角色常量与合法性校验 | `isValid`、`nameOf` |
| `SysUserController` | sys | 后台账号管理，角色维护 | `list`、`save`、`update`、`roles` |
| `SysLoginController` | sys | 后台登录，签发 JWT | `login` |
| `DepositRefundController` | member | 押金退还申请列表/详情/审核 | `list`、`audit` |
| `DepositRefundServiceImpl` | member | 审核事务：状态机校验、写审核人、通过后清零押金 | `audit` |
| `ApiLoginController` | member | 读者注册/登录，密码摘要 | `login`、`register` |
| `OrderServiceImpl` | order | 借阅下单（扣库存、写历史）、归还（回补库存） | `createOrder`、`returnBook` |
| `MD5Util` | common.utils | 口令摘要 | `MD5Encode` |

### 3.3 审核用例顺序设计

读者先提交申请（写 `book_deposit_refund`，status=0）→ 审核人员查询列表 → 提交审核
→ `DepositRefundServiceImpl.audit` 校验状态为 0 → 更新 status/audit_user_id/audit_time/remark
→ 通过时 `member.deposit=0` → 返回结果。整个过程在 `@Transactional` 内，任一步失败回滚。

### 3.4 借阅业务规则

* 借期固定 14 天（`DateUtils.add(now, 14)`），借阅单状态初始为 1；
* 借阅前校验押金 > 0，逐本校验 `stock >= num`；
* 下单时扣减库存并写入/更新借阅历史（同一读者同一本书只写一条历史）；
* 归还在事务中先更新借阅单状态为 2，再按借阅明细回补库存。

## 4. 数据库设计

### 4.1 设计说明

* 共 13 张表：读者 2 张（`base_member`、`base_member_auth`）、图书 5 张（`book_book`、`book_category`、`book_history`、`book_shelf`、`book_advert`）、
  借阅 3 张（`book_order`、`book_order_book`、`book_order_evaluation`）、押金 1 张（`book_deposit_refund`）、
  后台账号 1 张（`sys_user`）、分享配置 1 张（`share_config`）；
* 主键统一为自增整型；`sys_user.username` 唯一索引；`book_deposit_refund` 对 `member_id`、`status` 建立查询索引；
* 金额使用 `decimal(10,2)`；时间使用 `datetime`；角色使用 `varchar(20)` 存枚举值，便于扩展新角色；
* 冗余设计：审核人用户名通过关联 `sys_user` 查询，不在退款表冗余存储；借阅单金额在生成时固化，避免商品改价影响历史。

### 4.2 核心表结构

**sys_user（后台账号）**

| 字段名 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | 主键、自增 | 账号 ID |
| username | varchar(50) | 非空、唯一 | 登录名 |
| password | varchar(100) | 可空 | MD5 摘要 |
| email / mobile | varchar(100) | 可空 | 联系方式 |
| status | tinyint | 默认 1 | 0 禁用 / 1 正常 |
| role | varchar(20) | 非空、默认 ADMIN | ADMIN 管理员 / REVIEWER 审核人员 |
| create_time | datetime | 可空 | 创建时间 |

**book_deposit_refund（押金退还申请）**

| 字段名 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | int | 主键、自增 | 申请 ID |
| member_id | int | 外键关联读者 | 申请读者 |
| amount | decimal(10,2) | 可空 | 退还金额 |
| status | tinyint | 默认 0 | 0 待审核 / 1 已通过 / 2 已拒绝 |
| apply_time | datetime | 可空 | 申请时间 |
| audit_time | datetime | 可空 | 审核时间 |
| audit_user_id | int | 可空 | 审核人（sys_user.id） |
| remark | varchar(255) | 可空 | 审核备注 |

**book_order（借阅单）**：order_number（借阅编号）、member_id、total_amount、order_status（0 取消/1 借阅中/2 已归还/3 已评价）、start_date、end_date、create_time。

**book_book（图书）**：book_name、category_id、author、press、price、stock、status、pic_url、describe。

其余表结构见 `a_book.sql` 与 `docs/database.md`。

### 4.3 E-R 关系（文字描述）

* 读者（1）—（n）借阅单；借阅单（n）—（n）图书，通过 `book_order_book` 关联；
* 读者（1）—（n）押金退还申请；后台账号（1）—（n）押金退还申请（审核人）；
* 分类（1）—（n）图书；读者（1）—（n）借阅历史；借阅单（1）—（1）评价。

## 5. 关键接口设计

| 接口 | 方法 | 权限 | 说明 |
| --- | --- | --- | --- |
| `/sys/login` | POST | 匿名 | 后台登录，返回 token 与 user（含 role） |
| `/sys/user/list` | POST/GET | ADMIN | 后台账号列表 |
| `/sys/user/save`、`/update`、`/delete` | POST | ADMIN | 账号维护（含角色） |
| `/sys/user/getInfo`、`/password` | POST | 任意登录账号 | 当前账号信息、修改本人密码 |
| `/depositRefund/list`、`/info/{id}` | GET | ADMIN、REVIEWER | 押金退还申请查询 |
| `/depositRefund/audit` | POST | ADMIN、REVIEWER | 审核（记录真实审核人） |
| `/api/login`、`/api/register` | POST | 匿名 | 读者登录/注册（密码 MD5） |
| `/api/order/create` | POST | 读者 | 借阅下单 |
| `/api/member/delDeposit` | POST | 读者 | 申请退还押金 |
| `/book/**`、`/order/**`、`/statistics/**` 等 | - | ADMIN | 后台管理接口 |

统一响应：`{code:0,msg:...,data...}`；401 未登录、403 无权限、500 业务/系统异常。
