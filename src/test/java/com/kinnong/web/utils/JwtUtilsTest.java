package com.kinnong.web.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * JWT 工具测试：验证 token 生成、解析、过期判断。
 */
public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    public void setUp() {
        jwtUtils = new JwtUtils();
        jwtUtils.setSecret("unit-test-secret");
        jwtUtils.setExpire(604800);
        jwtUtils.setHeader("token");
    }

    @Test
    @DisplayName("生成的 token 可以解析出用户 ID")
    public void testGenerateAndDecode() throws Exception {
        String token = jwtUtils.generateToken(12L);
        assertNotNull(token);
        DecodedJWT decoded = jwtUtils.getDecodedByToken(token);
        assertNotNull(decoded);
        assertEquals("12", decoded.getAudience().get(0));
    }

    @Test
    @DisplayName("正常有效期内 token 未过期")
    public void testTokenNotExpired() throws Exception {
        String token = jwtUtils.generateToken(1L);
        DecodedJWT decoded = jwtUtils.getDecodedByToken(token);
        assertFalse(jwtUtils.isTokenExpired(decoded.getExpiresAt()));
    }

    @Test
    @DisplayName("非法 token 解析返回 null")
    public void testInvalidToken() {
        assertNull(jwtUtils.getDecodedByToken("this.is.not.a.jwt"));
    }

    @Test
    @DisplayName("过期 token 判定为已过期")
    public void testExpiredToken() throws Exception {
        JwtUtils expiredJwt = new JwtUtils();
        expiredJwt.setSecret("unit-test-secret");
        expiredJwt.setExpire(-10);
        expiredJwt.setHeader("token");
        String token = expiredJwt.generateToken(1L);
        DecodedJWT decoded = expiredJwt.getDecodedByToken(token);
        assertTrue(expiredJwt.isTokenExpired(decoded.getExpiresAt()));
    }
}
