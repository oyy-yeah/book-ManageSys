package com.kinnong.modules.order.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kinnong.common.annotation.RequireRole;
import com.kinnong.common.constant.RoleConstant;
import com.kinnong.common.utils.Query;
import com.kinnong.common.utils.R;
import com.kinnong.modules.order.entity.OrderEntity;
import com.kinnong.modules.order.service.OrderService;

/**
 * 借阅审核
 *
 * <p>读者提交借阅申请后进入“待审核”（orderStatus=4），审核人员在这里通过或拒绝：
 * 通过后借阅单变为“借阅中”；拒绝后变为“已拒绝”并回补库存。</p>
 *
 * @author ZJL
 * @email 1044869436@qq.com
 */
@RestController
@RequestMapping("/borrowAudit")
@RequireRole({ RoleConstant.ADMIN, RoleConstant.REVIEWER })
public class BorrowAuditController {

    @Autowired
    private OrderService orderService;

    /**
     * 待审核借阅申请列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        // 只查询待审核状态
        params.put("orderStatus", 4);
        Query query = new Query(params);
        List<OrderEntity> orderList = orderService.queryList(query);
        int total = orderService.queryTotal(query);
        return R.ok().put("rows", orderList).put("total", total);
    }

    /**
     * 借阅申请详情（含借阅明细）
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Integer id) {
        return R.ok().put("order", orderService.queryObject(id));
    }

    /**
     * 审核借阅申请
     *
     * @param body id(借阅单ID)、approve(是否通过)、remark(审核备注)
     */
    @RequestMapping("/audit")
    public R audit(@RequestBody Map<String, Object> body) {
        if (body.get("id") == null || body.get("approve") == null) {
            return R.error("参数不完整");
        }
        Integer id = Integer.valueOf(String.valueOf(body.get("id")));
        boolean approve = Boolean.valueOf(String.valueOf(body.get("approve")));
        String remark = body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        orderService.audit(id, approve, remark);
        return R.ok();
    }
}
