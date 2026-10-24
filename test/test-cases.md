# 测试用例（test/test-cases.md）

| 编号 | 对应需求 | 前置条件 | 测试步骤 | 预期结果 | 结论 |
| --- | --- | --- | --- | --- | --- |
| TC-01 | FR-01 | 已存在读者 test | POST `/api/login`，loginName=test，password=123456 | code=0，返回 token 与 member，password 字段为 null | 通过 |
| TC-02 | FR-01 | 已存在读者 test | POST `/api/login`，password=wrong | code=500，提示“账号或密码不正确” | 通过 |
| TC-03 | FR-01 | 无 | POST `/api/register`，新登录名+密码 | code=0，库中 password 为 32 位 MD5 | 通过 |
| TC-04 | FR-01 | 后台账号 admin | POST `/sys/login`，admin/123456 | code=0，user.role=ADMIN，password 为 null | 通过 |
| TC-05 | FR-07 | 后台账号 reviewer | POST `/sys/login`，reviewer/reviewer123 | code=0，user.role=REVIEWER | 通过 |
| TC-06 | NFR-04 | reviewer 的 token | GET `/sys/user/list` | HTTP 403，msg=没有访问该功能的权限 | 通过 |
| TC-07 | NFR-04 | reviewer 的 token | GET `/book/list` | HTTP 403 | 通过 |
| TC-08 | NFR-04 | admin 的 token | GET `/sys/user/list` | HTTP 200，返回账号列表 | 通过 |
| TC-09 | NFR-04 | 无 token | GET `/sys/user/list` | HTTP 401 | 通过 |
| TC-10 | FR-07 | reviewer token、存在待审核申请 | POST `/depositRefund/audit`，status=1，remark | code=0，库中 status=1、audit_user_id=审核人 ID、audit_time 非空 | 通过 |
| TC-11 | FR-07 | 已审核的申请 | 再次 POST `/depositRefund/audit` | code=500，提示“该申请已审核，不能重复审核” | 通过 |
| TC-12 | FR-06 | 读者押金>0、无在途申请 | POST `/api/member/delDeposit` | code=0，生成 status=0 的申请 | 通过 |
| TC-13 | FR-06 | 读者已有在途申请 | 再次 POST `/api/member/delDeposit` | code=500，提示“已有一笔退押金申请正在处理中” | 通过 |
| TC-14 | FR-05 | 已登录读者 | POST `/api/member/addDeposit`，deposit=50 | code=0，押金累加 50 | 通过 |
| TC-15 | FR-05 | 未登录 | POST `/api/member/addDeposit` | HTTP 401 | 通过 |
| TC-16 | FR-11 | admin token | POST `/sys/user/save`，role=REVIEWER | code=0，新账号角色为 REVIEWER | 通过 |
| TC-17 | FR-11 | admin token | POST `/sys/user/save`，role=HACKER | code=500，提示“角色参数不正确” | 通过 |
| TC-18 | FR-03 | 读者押金=0 | POST `/api/order/create` | code=500，提示“您还未交押金” | 通过 |
| TC-19 | FR-03 | 读者押金>0、库存充足 | POST `/api/order/create` | code=0，借阅单 orderStatus=1，图书库存减 1 | 通过 |
| TC-20 | FR-03 | 库存=0 | POST `/api/order/create` | code=500，提示“xxx 库存不足” | 通过 |
| TC-21 | FR-04 | 存在借阅中订单 | POST `/order/returnBook/{id}`（admin） | code=0，orderStatus=2，库存回补 | 通过 |
| TC-22 | NFR-01 | 数据库有 100 本图书 | 连续 10 次 GET `/book/list` | 平均响应时间 < 2s | 通过 |
| TC-23 | NFR-03 | 任意账号 | 登录响应与列表接口检查 password 字段 | 均为 null，不返回摘要 | 通过 |
| TC-24 | FR-02 | 已登录读者 | GET `/api/book/list`，关键字“三体” | 返回匹配图书列表 | 通过 |
| TC-25 | FR-10 | admin token | GET `/statistics/query` | 返回用户数、图书数、借阅数、分类分布 | 通过 |
| TC-26 | FR-08 | admin token | POST `/book/save` 新增图书 | code=0，列表可见新书 | 通过 |
| TC-27 | FR-08 | admin token | POST `/book/delete` 删除图书 | code=0，列表不再显示 | 通过 |
| TC-28 | FR-12 | admin token | POST `/advert/save` 新增广告 | code=0，广告列表可见 | 通过 |
