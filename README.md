# 图书借阅管理系统（Books-Management）

《软件工程》课程期末综合实践项目 —— 2024 软件工程 4 班 / 第九组。

系统由 **读者端接口（小程序/Web API）** 与 **后台管理端** 两部分组成，基于 Spring Boot + MyBatis + Vue/AdminLTE 实现，
覆盖「图书管理 → 读者借阅 → 归还 → 押金管理 → 押金退还审核 → 统计分析」的完整业务闭环。

## 一、角色与权限

系统包含 3 类角色，后台通过 `sys_user.role` + `@RequireRole` 注解 + 拦截器实现接口级权限控制，
前端按角色过滤导航菜单，二者共同保证“看不到的页面也调不通接口”。

| 角色 | 账号示例 | 权限范围 |
| --- | --- | --- |
| 普通用户（读者） | `test / 123456`（`base_member` 表） | 注册登录、图书检索与分类浏览、借阅下单、取消借阅、押金充值、申请退押金、我的借阅/评价 |
| 审核人员（REVIEWER） | `reviewer / reviewer123` | 登录后台，查看待审核押金退还申请，执行「通过 / 拒绝」审核并填写备注；可修改本人密码 |
| 管理员（ADMIN） | `admin / 123456` | 后台全部功能：统计分析、用户管理、广告管理、分类管理、图书管理、借阅管理、评价管理、管理员管理（含账号与角色维护） |

权限实现要点：

1. `com.kinnong.common.annotation.RequireRole`：可标注在 Controller 类或方法上；
2. `com.kinnong.web.interceptor.AuthorizationInterceptor`：先校验 JWT，再按注解从数据库实时读取角色判断是否放行，不通过返回 HTTP 403；
3. `com.kinnong.common.constant.RoleConstant`：统一维护 `ADMIN` / `REVIEWER` 常量与角色合法性校验；
4. 前端 `components.js` 按 `localStorage.role` 过滤菜单，`common.js` 统一处理 401/403 跳转。

## 二、核心功能

| 编号 | 功能模块 | 功能项 | 说明 |
| --- | --- | --- | --- |
| FR-01 | 读者账户 | 注册 / 登录 | 密码 MD5 摘要存储，登录返回 JWT |
| FR-02 | 图书检索 | 分类浏览 / 关键字查询 / 详情 | 首页广告位、分类树、图书详情与库存 |
| FR-03 | 借阅管理 | 借阅下单 | 校验押金、库存，生成借阅单（14 天），扣减库存，写入借阅历史 |
| FR-04 | 借阅管理 | 取消 / 归还 | 借阅中可取消；管理员办理归还并回补库存 |
| FR-05 | 押金管理 | 押金充值 | 读者自助充值，金额累加 |
| FR-06 | 押金管理 | 退押金申请 | 校验无在途申请，生成待审核申请 |
| FR-07 | 押金审核 | 押金退还审核 | 审核人员通过/拒绝，通过后押金清零，记录审核人与审核时间，禁止重复审核 |
| FR-08 | 图书管理 | 图书 / 分类维护 | 上下架、库存、图片、价格维护 |
| FR-09 | 借阅管理 | 借阅单 / 评价管理 | 后台查询借阅单、办理归还、管理评价 |
| FR-10 | 统计报表 | 统计分析 | 用户数、图书数、借阅量、分类借阅分布图 |
| FR-11 | 账户管理 | 管理员与角色维护 | 新增后台账号并指定角色（ADMIN/REVIEWER） |
| FR-12 | 内容管理 | 广告管理 | 轮播广告启停、排序 |

## 三、技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 / 运行时 | Java 8、JavaScript（Vue 2） |
| 后端框架 | Spring Boot 2.3.4、MyBatis 3.4、Apache Shiro（会话/验证码）、JWT（java-jwt 3.3.0） |
| 数据库 | MySQL 8.0（Druid 连接池） |
| 前端 | AdminLTE 2.3.8、Bootstrap 3.3.7、Vue 2.2、jQuery、bootstrap-table |
| 构建 | Maven 3.9 |
| 测试 | JUnit 5、Postman |
| 建模 | PlantUML（用例图、类图、顺序图、状态图、部署图、E-R 图） |
| 版本控制 | Git（头歌平台仓库） |

## 四、目录结构

```
Books-Management
├── a_book.sql                     # 数据库初始化脚本（建库建表 + 初始数据）
├── member_payment_update.sql      # 会员收款信息字段升级脚本
├── sql/
│   └── 2025-11-role-upgrade.sql   # 角色权限升级脚本（新增 role 字段与审核账号）
├── docs/                          # 需求、设计、数据库、会议记录等文档
├── test/                          # 测试用例、测试报告、Postman 集合
├── scripts/
│   └── code_stat.py               # 代码规模统计脚本（报告 4.3 节口径）
├── src/main/java/com/kinnong/
│   ├── common/                    # 通用工具、注解、常量、异常
│   ├── modules/
│   │   ├── sys/                   # 后台账号、登录、角色权限
│   │   ├── member/                # 读者、押金、押金退还审核
│   │   ├── book/                  # 图书、分类、借阅历史、书架
│   │   ├── order/                 # 借阅单、评价、统计
│   │   ├── advert/                # 广告
│   │   └── share/                 # 分享配置
│   └── web/                       # 拦截器、JWT、全局异常、MVC 配置
├── src/main/resources/
│   ├── mapper/                    # MyBatis 映射
│   ├── static/                    # 后台与前台静态资源
│   └── views/admin/               # 后台页面
└── src/test/java/                 # 单元测试
```

## 五、快速开始

### 1. 初始化数据库

```bash
mysql -uroot -p < a_book.sql
mysql -uroot -p a_book < member_payment_update.sql        # 若库中没有收款字段
mysql -uroot -p a_book < sql/2025-11-role-upgrade.sql     # 角色字段与审核账号
```

### 2. 配置数据源

将 `src/main/resources/application-dev.example.yml` 复制为 `application-dev.yml`，填写本机数据库密码；
或设置环境变量 `DB_PASSWORD`、`JWT_SECRET`、`WECHAT_APPID`、`WECHAT_APP_SECRET`。

> `application-dev.yml` 已被 `.gitignore` 忽略，不会提交到仓库。

### 3. 启动

```bash
mvn spring-boot:run
# 或
mvn clean package -DskipTests
java -jar target/book-server-0.0.1-SNAPSHOT.jar
```

启动后访问：

* 后台登录：<http://localhost:8080/admin/login.html>
* 管理员：`admin / 123456`
* 审核人员：`reviewer / reviewer123`
* 读者（小程序/接口，**不能登录后台页面**）：`test / 123456`，调用 `POST /api/login`，参数为 `loginName` 与 `password`

### 4. 测试

```bash
mvn test                 # 单元测试（MD5、JWT、角色常量）
# 接口测试：导入 test/postman/book-manageSys.postman_collection.json
```

## 六、代码规模

统计口径见 `scripts/code_stat.py`（剔除第三方插件、构建产物与纯注释空行）：

```bash
python scripts/code_stat.py
```

## 七、版本控制与分支规范

* 主干：`main`（受保护的稳定分支）；
* 功能分支：`feature/<功能>`、`fix/<缺陷>`；
* 提交信息格式：`类型: 摘要`，类型包括 `feat`、`fix`、`test`、`docs`、`chore`；
* 仓库地址：<https://bdgit.educoder.net/pvelqaibr/book-ManageSys.git>

## 八、许可证与致谢

本项目为课程实践作品，仅用于教学与学习交流。后端思路与基础代码来自课程实验项目，角色权限、押金审核、
密码加密、测试与文档为本次综合实践补充完善。
