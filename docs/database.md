# 数据库设计说明（a_book）

数据库：MySQL 8.0，字符集 utf8mb3，共 13 张表。

| 表名 | 说明 | 主要字段 |
| --- | --- | --- |
| base_member | 读者（普通用户） | nickname、avatar_url、gender、real_name、mobile、login_name、password(MD5)、deposit、create_time、id_card、payment_type、wechat_account、alipay_account、bank_card_number、bank_name、cardholder_name |
| base_member_auth | 微信授权信息 | member_id、openid、auth_type、create_time |
| sys_user | 后台账号（管理员/审核人员） | username、password(MD5)、email、mobile、status、role、create_time |
| book_category | 图书分类 | name、parent_id、sort、create_time |
| book_book | 图书 | book_name、category_id、author、press、price、stock、status、pic_url、describe、create_time |
| book_history | 借阅历史（读者-图书） | member_id、book_id、create_time |
| book_shelf | 书架（想读/在读） | member_id、book_id、type、create_time |
| book_order | 借阅单 | order_number、member_id、total_amount、order_status、start_date、end_date、remark、create_time |
| book_order_book | 借阅明细 | order_id、book_id、num、price |
| book_order_evaluation | 评价 | order_id、member_id、content、score、create_time |
| book_deposit_refund | 押金退还申请 | member_id、amount、status、apply_time、audit_time、audit_user_id、remark |
| book_advert | 广告 | pic_url、link、sort、enable、create_time |
| share_config | 分享配置 | config_key、config_name、config_type、config_value、description、enable |

## 关键约束与索引

* `base_member.login_name` 用于读者登录查询，建议唯一索引（当前数据量小，未强制）；
* `sys_user.username` 唯一索引，避免重复账号；
* `book_deposit_refund(member_id)`、`book_deposit_refund(status)` 建立普通索引，支撑“我的申请/待审核列表”查询；
* `book_order.order_number` 唯一编号，用于借阅单外部标识；
* `base_member_auth.openid` 唯一索引，保证微信授权一对一。

## 数据初始化

| 账号 | 密码 | 角色 | 表 |
| --- | --- | --- | --- |
| admin | 123456（MD5：e10adc3949ba59abbe56e057f20f883e） | ADMIN | sys_user |
| reviewer | reviewer123（MD5：86a7a97fe360ee0c51f2f7c2eb95e689） | REVIEWER | sys_user |
| test | 123456（MD5：e10adc3949ba59abbe56e057f20f883e） | 普通用户 | base_member |

## 升级脚本

1. `member_payment_update.sql`：为 `base_member` 增加收款信息字段；
2. `sql/2025-11-role-upgrade.sql`：为 `sys_user` 增加 `role` 字段，初始化审核账号 `reviewer`。

执行顺序：`a_book.sql` → `member_payment_update.sql` → `sql/2025-11-role-upgrade.sql`。
