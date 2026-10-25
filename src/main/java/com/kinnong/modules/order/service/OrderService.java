package com.kinnong.modules.order.service;

import java.util.List;
import java.util.Map;

import com.kinnong.modules.order.entity.OrderEntity;

/**
 * 借阅
 *
 * @author ZJL
 * @email 1044869436@qq.com
 */
public interface OrderService {
	
	OrderEntity queryObject(Integer id);
	
	List<OrderEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(OrderEntity order);
	
	void update(OrderEntity order);
	
	void delete(Integer id);
	
	void deleteBatch(Integer[] ids);

	void createOrder(OrderEntity orderEntity);

	void updateByOrderNumber(OrderEntity order);

	List<Map<String, String>> queryOrderCount();

	void returnBook(OrderEntity order);

	/**
	 * 借阅审核：通过（状态置为借阅中）或拒绝（状态置为已拒绝并回补库存）
	 *
	 * @param orderId 借阅单 ID
	 * @param approve 是否通过
	 * @param remark  审核备注
	 */
	void audit(Integer orderId, boolean approve, String remark);
}
