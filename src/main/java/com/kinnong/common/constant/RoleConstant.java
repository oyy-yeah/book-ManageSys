package com.kinnong.common.constant;

/**
 * 系统角色常量。
 *
 * <p>本系统至少包含三类角色：
 * <ul>
 *   <li>普通用户（读者）：使用小程序端，注册登录、检索图书、借阅、充值、申请退押金；</li>
 *   <li>审核人员（REVIEWER）：登录后台，仅可查看并审核押金退还申请；</li>
 *   <li>管理员（ADMIN）：拥有后台全部管理权限。</li>
 * </ul>
 * 普通用户对应 base_member 表，审核人员与管理员对应 sys_user 表的 role 字段。</p>
 *
 * @author ZJL
 */
public final class RoleConstant {

    /** 管理员：拥有后台全部权限 */
    public static final String ADMIN = "ADMIN";

    /** 审核人员：仅拥有审核类权限 */
    public static final String REVIEWER = "REVIEWER";

    /** 普通用户（读者）：使用读者端页面，账号存储在 base_member 表 */
    public static final String USER = "USER";

    private RoleConstant() {
    }

    /**
     * 判断角色是否合法（sys_user 表只允许管理员与审核人员）
     */
    public static boolean isValid(String role) {
        return ADMIN.equals(role) || REVIEWER.equals(role);
    }

    /**
     * 角色中文名称，用于日志与页面展示
     */
    public static String nameOf(String role) {
        if (ADMIN.equals(role)) {
            return "管理员";
        }
        if (REVIEWER.equals(role)) {
            return "审核人员";
        }
        if (USER.equals(role)) {
            return "普通用户";
        }
        return "普通用户";
    }
}
