-- =====================================================================
-- 角色权限升级脚本（2025-11）
-- 为 sys_user 增加 role 字段，引入“审核人员”角色，并初始化审核账号。
-- 原有 admin 账号默认保持管理员角色。
-- 执行方式：mysql -uroot -p a_book < sql/2025-11-role-upgrade.sql
-- =====================================================================

USE `a_book`;

-- 1. 增加角色字段：ADMIN 管理员 / REVIEWER 审核人员
ALTER TABLE `sys_user`
  ADD COLUMN `role` varchar(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色：ADMIN 管理员，REVIEWER 审核人员' AFTER `status`;

-- 2. 历史账号统一设为管理员
UPDATE `sys_user` SET `role` = 'ADMIN' WHERE `role` IS NULL OR `role` = '';

-- 3. 初始化审核人员账号：reviewer / reviewer123
INSERT INTO `sys_user` (`username`, `password`, `email`, `mobile`, `status`, `create_time`, `role`)
SELECT 'reviewer', MD5('reviewer123'), 'reviewer@kinnong.com', NULL, 1, NOW(), 'REVIEWER'
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'reviewer');
