package com.kinnong.modules.sys.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kinnong.common.annotation.RequireRole;
import com.kinnong.common.constant.RoleConstant;
import com.kinnong.common.utils.MD5Util;
import com.kinnong.common.utils.Query;
import com.kinnong.common.utils.R;
import com.kinnong.modules.sys.entity.SysUserEntity;
import com.kinnong.modules.sys.service.SysUserService;

/**
 * 系统用户
 *
 * <p>权限设置：管理员管理相关接口（列表、详情、新增、修改、删除）仅管理员可访问；
 * 获取当前登录人信息、修改本人密码两个接口面向所有已登录的后台账号（包含审核人员）。</p>
 *
 * @author ZJL
 * @email 1044869436@qq.com
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController {
	@Autowired
	private SysUserService sysUserService;

	/**
	 * 所有用户列表（仅管理员）
	 */
	@RequireRole(RoleConstant.ADMIN)
	@RequestMapping("/list")
	public R list(@RequestParam Map<String, Object> params){

		//查询列表数据
		Query query = new Query(params);
		List<SysUserEntity> userList = sysUserService.queryList(query);
		int total = sysUserService.queryTotal(query);

		return R.ok().put("rows", userList).put("total", total);
	}

	/**
	 * 角色下拉列表（仅管理员）
	 */
	@RequireRole(RoleConstant.ADMIN)
	@RequestMapping("/roles")
	public R roles(){
		return R.ok().put("roles", new String[]{ RoleConstant.ADMIN, RoleConstant.REVIEWER });
	}

	/**
	 * 获取登录的用户信息（所有后台账号）
	 */
	@RequestMapping("/getInfo")
	public R getInfo(@RequestAttribute Long userId){
		return R.ok().put("user", sysUserService.queryObject(userId));
	}

	/**
	 * 修改登录用户密码（所有后台账号）
	 */
	@RequestMapping("/password")
	public R password(String password, String newPassword, @RequestAttribute Long userId){

		//sha256加密
		password = MD5Util.MD5Encode(password);
		//sha256加密
		newPassword = MD5Util.MD5Encode(newPassword);

		//更新密码
		int count = sysUserService.updatePassword(userId, password, newPassword);
		if(count == 0){
			return R.error("原密码不正确");
		}

		return R.ok();
	}

	/**
	 * 用户信息（仅管理员）
	 */
	@RequireRole(RoleConstant.ADMIN)
	@RequestMapping("/info/{id}")
	public R info(@PathVariable("id") Long id){
		SysUserEntity user = sysUserService.queryObject(id);
		return R.ok().put("user", user);
	}

	/**
	 * 保存用户（仅管理员）
	 */
	@RequireRole(RoleConstant.ADMIN)
	@RequestMapping("/save")
	public R save(@RequestBody SysUserEntity user){
		//角色为空时默认管理员，非法角色直接拒绝，避免出现越权账号
		if (StringUtils.isBlank(user.getRole())) {
			user.setRole(RoleConstant.ADMIN);
		}
		if (!RoleConstant.isValid(user.getRole())) {
			return R.error("角色参数不正确");
		}
		sysUserService.save(user);

		return R.ok();
	}

	/**
	 * 修改用户（仅管理员）
	 */
	@RequireRole(RoleConstant.ADMIN)
	@RequestMapping("/update")
	public R update(@RequestBody SysUserEntity user){
		if (user.getRole() != null && !RoleConstant.isValid(user.getRole())) {
			return R.error("角色参数不正确");
		}
		sysUserService.update(user);
		return R.ok();
	}

	/**
	 * 删除用户（仅管理员）
	 */
	@RequireRole(RoleConstant.ADMIN)
	@RequestMapping("/delete")
	public R delete(@RequestBody Long[] ids){
		sysUserService.deleteBatch(ids);
		return R.ok();
	}
}
