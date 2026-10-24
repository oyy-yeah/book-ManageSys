package com.kinnong.common.annotation;

import java.lang.annotation.*;

import com.kinnong.common.constant.RoleConstant;

/**
 * 角色权限校验注解。
 *
 * <p>标注在 Controller 类或方法上，表示该接口只允许指定角色访问。
 * 校验逻辑由 {@link com.kinnong.web.interceptor.AuthorizationInterceptor} 完成：
 * 先校验 token 是否有效，再根据 token 中的用户 ID 查询数据库中的角色，
 * 判断角色是否在允许列表内，不满足时返回 403。</p>
 *
 * <p>示例：</p>
 * <pre>
 * // 仅管理员可访问
 * &#64;RequireRole(RoleConstant.ADMIN)
 * // 管理员与审核人员均可访问
 * &#64;RequireRole({RoleConstant.ADMIN, RoleConstant.REVIEWER})
 * </pre>
 *
 * @author ZJL
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允许访问的角色列表，满足其中之一即可通过
     */
    String[] value() default { RoleConstant.ADMIN };
}
