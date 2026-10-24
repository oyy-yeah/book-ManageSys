package com.kinnong.modules.member.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kinnong.common.annotation.RequireRole;
import com.kinnong.common.constant.RoleConstant;
import com.kinnong.common.utils.Query;
import com.kinnong.common.utils.R;
import com.kinnong.modules.member.entity.DepositRefundEntity;
import com.kinnong.modules.member.service.DepositRefundService;

/**
 * 押金退还申请管理
 *
 * <p>权限设置：管理员与审核人员均可查看待审核的押金退还申请；
 * 审核操作记录真实的审核人账号（从 token 中解析），不再写死为 1 号用户。</p>
 *
 * @author ZJL
 * @email 1044869436@qq.com
 */
@RestController
@RequestMapping("/depositRefund")
@RequireRole({ RoleConstant.ADMIN, RoleConstant.REVIEWER })
public class DepositRefundController {
    
    @Autowired
    private DepositRefundService depositRefundService;
    
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        //查询列表数据
        Query query = new Query(params);
        query.put("sidx", "id");
        query.put("order", "desc");
        return R.ok()
            .put("rows", depositRefundService.queryList(query))
            .put("total", depositRefundService.queryTotal(query));
    }
    
    /**
     * 详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("depositRefund", depositRefundService.queryObject(id));
    }
    
    /**
     * 审核
     */
    @RequestMapping("/audit")
    public R audit(@RequestBody DepositRefundEntity depositRefund, @RequestAttribute Long userId) {
        if (depositRefund.getId() == null || depositRefund.getStatus() == null) {
            return R.error("参数不完整");
        }
        
        if (depositRefund.getStatus() != 1 && depositRefund.getStatus() != 2) {
            return R.error("审核状态参数错误");
        }
        
        depositRefundService.audit(
            depositRefund.getId(), 
            depositRefund.getStatus(), 
            userId, 
            depositRefund.getRemark()
        );
        
        return R.ok();
    }
}
