package com.kinnong.common.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 角色常量与角色合法性测试。
 */
public class RoleConstantTest {

    @Test
    @DisplayName("管理员与审核人员均为合法角色")
    public void testValidRoles() {
        assertTrue(RoleConstant.isValid(RoleConstant.ADMIN));
        assertTrue(RoleConstant.isValid(RoleConstant.REVIEWER));
    }

    @Test
    @DisplayName("未知角色不合法，防止越权账号入库")
    public void testInvalidRole() {
        assertFalse(RoleConstant.isValid("SUPER_ADMIN"));
        assertFalse(RoleConstant.isValid(""));
        assertFalse(RoleConstant.isValid(null));
    }

    @Test
    @DisplayName("角色中文名称映射正确")
    public void testRoleName() {
        assertEquals("管理员", RoleConstant.nameOf(RoleConstant.ADMIN));
        assertEquals("审核人员", RoleConstant.nameOf(RoleConstant.REVIEWER));
        assertEquals("普通用户", RoleConstant.nameOf("MEMBER"));
    }
}
