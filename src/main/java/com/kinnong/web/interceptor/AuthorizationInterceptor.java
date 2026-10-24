package com.kinnong.web.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.kinnong.common.annotation.AuthIgnore;
import com.kinnong.common.annotation.RequireRole;
import com.kinnong.common.exception.RException;
import com.kinnong.modules.sys.entity.SysUserEntity;
import com.kinnong.modules.sys.service.SysUserService;
import com.kinnong.web.utils.JwtUtils;

/**
 * 权限(Token)验证
 *
 * <p>处理流程：
 * <ol>
 *   <li>如果接口标注了 {@link AuthIgnore}，直接放行（登录、注册、验证码等）；</li>
 *   <li>校验请求头中的 token 是否有效，解析出用户 ID 放入 request；</li>
 *   <li>如果接口（或所在类）标注了 {@link RequireRole}，查询该用户当前角色，
 *       判断是否在允许角色列表中，不在则抛出 403 异常。</li>
 * </ol>
 * 角色从数据库实时读取，因此管理员修改某账号的角色后无需重新登录即可生效。</p>
 *
 * @author ZJL
 * @email 1044869436@qq.com
 */
@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SysUserService sysUserService;

    /** 用户ID，控制器通过 @RequestAttribute Long userId 获取 */
    public static final String USER_KEY = "userId";

    /** 角色，控制器通过 @RequestAttribute String role 获取 */
    public static final String ROLE_KEY = "role";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	AuthIgnore annotation;
        if(handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(AuthIgnore.class);
        }else{
            return true;
        }

        //如果有@IgnoreAuth注解，则不验证token
        if(annotation != null){
            return true;
        }

        //获取用户凭证
        String token = request.getHeader(jwtUtils.getHeader());
        if(StringUtils.isBlank(token)){
            token = request.getParameter(jwtUtils.getHeader());
        }

        //凭证为空
        if(StringUtils.isBlank(token)){
            throw new RException(jwtUtils.getHeader() + "不能为空", HttpStatus.UNAUTHORIZED.value());
        }

        DecodedJWT decoded = jwtUtils.getDecodedByToken(token);
        if(decoded == null || jwtUtils.isTokenExpired(decoded.getExpiresAt())){
            throw new RException(jwtUtils.getHeader() + "失效，请重新登录", HttpStatus.UNAUTHORIZED.value());
        }

        //设置userId到request里，后续根据userId，获取用户信息
        Long userId = Long.parseLong(decoded.getAudience().get(0));
        request.setAttribute(USER_KEY, userId);

        //角色权限校验（方法级注解优先于类级注解）
        RequireRole requireRole = getRequireRole(handler);
        if (requireRole != null && !hasRole(userId, requireRole.value())) {
            throw new RException("没有访问该功能的权限", HttpStatus.FORBIDDEN.value());
        }

        return true;
    }

    /**
     * 获取接口或所在类上的角色注解
     */
    private RequireRole getRequireRole(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return null;
        }
        HandlerMethod method = (HandlerMethod) handler;
        RequireRole requireRole = method.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = method.getBeanType().getAnnotation(RequireRole.class);
        }
        return requireRole;
    }

    /**
     * 判断用户当前角色是否在允许列表中，未登录用户、被禁用账号均无权限
     */
    private boolean hasRole(Long userId, String[] allowRoles) {
        SysUserEntity user = sysUserService.queryObject(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return false;
        }
        String role = user.getRole();
        if (role == null) {
            return false;
        }
        for (String allowRole : allowRoles) {
            if (allowRole.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
