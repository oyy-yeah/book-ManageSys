package com.kinnong.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 密码摘要工具测试。
 *
 * <p>验证登录时使用的 MD5 摘要算法与数据库中的密文口径一致，
 * 并验证相同明文多次摘要结果稳定、不同明文摘要不同。</p>
 */
public class MD5UtilTest {

    @Test
    @DisplayName("常用密码 123456 的 MD5 应为 e10adc3949ba59abbe56e057f20f883e")
    public void testMd5OfCommonPassword() {
        assertEquals("e10adc3949ba59abbe56e057f20f883e", MD5Util.MD5Encode("123456"));
    }

    @Test
    @DisplayName("管理员默认密码 admin 的 MD5 应为 21232f297a57a5a743894a0e4a801fc3")
    public void testMd5OfAdmin() {
        assertEquals("21232f297a57a5a743894a0e4a801fc3", MD5Util.MD5Encode("admin"));
    }

    @Test
    @DisplayName("同样的明文多次摘要结果相同，不同明文摘要不同")
    public void testMd5StableAndUnique() {
        String first = MD5Util.MD5Encode("reader123");
        String second = MD5Util.MD5Encode("reader123");
        assertEquals(first, second);
        assertNotEquals(first, MD5Util.MD5Encode("reader124"));
        assertEquals(32, first.length());
    }

    @Test
    @DisplayName("中文密码使用 UTF-8 编码摘要，结果长度仍为 32 位")
    public void testMd5WithChinese() {
        String encode = MD5Util.MD5Encode("图书借阅");
        assertEquals(32, encode.length());
        assertTrue(encode.matches("[0-9a-f]{32}"));
    }
}
