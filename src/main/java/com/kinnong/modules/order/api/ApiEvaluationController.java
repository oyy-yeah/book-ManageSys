package com.kinnong.modules.order.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kinnong.common.annotation.AuthIgnore;
import com.kinnong.common.utils.R;
import com.kinnong.modules.order.entity.OrderEntity;
import com.kinnong.modules.order.entity.OrderEvaluationEntity;
import com.kinnong.modules.order.service.OrderService;
import com.kinnong.modules.order.service.OrderEvaluationService;

/**
 * 评价留言
 * @author ZJL
 * @email 1044869436@qq.com
 */
@RestController
@RequestMapping("/api/evaluation")
public class ApiEvaluationController {
	
	@Autowired
	private OrderEvaluationService orderEvaluationService;

	@Autowired
	private OrderService orderService;
	
	@AuthIgnore
    @GetMapping("list")
	public R list(@RequestParam Map<String, Object> map) {
		List<OrderEvaluationEntity> evaluations = orderEvaluationService.queryList(map);
		return R.ok().put("evaluations", evaluations);
	}
	
	@PostMapping("save")
	public R save(@RequestAttribute("userId") Long userId, @RequestBody OrderEvaluationEntity orderEvaluation) {
		if (orderEvaluation.getOrderId() == null) {
			return R.error("借阅单不能为空");
		}
		OrderEntity order = orderService.queryObject(orderEvaluation.getOrderId());
		if (order == null || !userId.equals(order.getMemberId())) {
			return R.error("借阅单不存在");
		}
		if (order.getOrderStatus() == null || order.getOrderStatus() != 2) {
			return R.error("只有已归还的借阅单可以评价");
		}
		orderEvaluation.setCreateTime(new Date());
		orderEvaluation.setMemberId(userId);
		orderEvaluationService.save(orderEvaluation);
		// 评价后把借阅单状态置为已评价
		OrderEntity update = new OrderEntity();
		update.setId(order.getId());
		update.setOrderStatus(3);
		orderService.update(update);
		return R.ok();
	}
}
