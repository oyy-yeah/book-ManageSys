package com.kinnong.modules.order.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kinnong.common.exception.RException;
import com.kinnong.common.utils.NumberUtil;
import com.kinnong.modules.book.dao.BookDao;
import com.kinnong.modules.book.dao.HistoryDao;
import com.kinnong.modules.book.entity.HistoryEntity;
import com.kinnong.modules.order.dao.OrderDao;
import com.kinnong.modules.order.dao.OrderBookDao;
import com.kinnong.modules.order.entity.OrderEntity;
import com.kinnong.modules.order.entity.OrderBookEntity;
import com.kinnong.modules.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl implements OrderService {
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private OrderBookDao orderBookDao;
	@Autowired
	private BookDao bookDao;
	@Autowired
	private HistoryDao historyDao;
	
	@Override
	public OrderEntity queryObject(Integer id){
		OrderEntity order = orderDao.queryObject(id);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", order.getId());
		List<OrderBookEntity> orderBookList = orderBookDao.queryList(params);
		order.setOrderBookList(orderBookList);
		return order;
	}
	
	@Override
	public List<OrderEntity> queryList(Map<String, Object> map){
		List<OrderEntity> orderList = orderDao.queryList(map);
		for (OrderEntity orderEntity : orderList) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", orderEntity.getId());
			List<OrderBookEntity> orderBookList = orderBookDao.queryList(params);
			orderEntity.setOrderBookList(orderBookList);
		}
		return orderList;
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return orderDao.queryTotal(map);
	}
	
	@Override
	public void save(OrderEntity order){
		orderDao.save(order);
	}
	
	@Override
	public void update(OrderEntity order){
		orderDao.update(order);
	}
	
	@Override
	public void delete(Integer orderId){
		orderDao.delete(orderId);
	}
	
	@Override
	public void deleteBatch(Integer[] orderIds){
		orderDao.deleteBatch(orderIds);
	}

	@Override
	@Transactional
	public void createOrder(OrderEntity order) {
		order.setOrderNumber(NumberUtil.getOrderNumber());
		orderDao.save(order);
		List<OrderBookEntity> orderBookList = order.getOrderBookList();
		for (OrderBookEntity orderBook : orderBookList) {
			orderBook.setOrderId(order.getId());
			orderBookDao.save(orderBook);
			bookDao.delStock(orderBook.getBookId(), orderBook.getNum());
			Map<String, Object> map = new HashMap<String, Object>();
	    	map.put("memberId", order.getMemberId());
	    	map.put("bookId", orderBook.getBookId());
			Integer total = historyDao.queryTotal(map);
	    	if(total < 1) {
	    		HistoryEntity history = new HistoryEntity();
	    		history.setCreateTime(new Date());
	        	history.setMemberId(order.getMemberId());
	        	history.setBookId(orderBook.getBookId());
	    		historyDao.save(history);
	    	}
		}
	}

	@Override
	public void updateByOrderNumber(OrderEntity order) {
		orderDao.updateByOrderNumber(order);
	}

	@Override
	public List<Map<String, String>> queryOrderCount() {
		return orderDao.queryOrderCount();
	}

	@Override
	@Transactional
	public void audit(Integer orderId, boolean approve, String remark) {
		OrderEntity order = orderDao.queryObject(orderId);
		if (order == null) {
			throw new RException("借阅单不存在");
		}
		if (order.getOrderStatus() == null || order.getOrderStatus() != 4) {
			throw new RException("该借阅单已审核，不能重复审核");
		}
		OrderEntity update = new OrderEntity();
		update.setId(orderId);
		update.setRemark(remark);
		if (approve) {
			// 通过：借阅单进入借阅中（库存已在下单时扣减）
			update.setOrderStatus(1);
			orderDao.update(update);
		} else {
			// 拒绝：状态置为已拒绝，并回补下单时扣减的库存
			update.setOrderStatus(5);
			orderDao.update(update);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("orderId", orderId);
			List<OrderBookEntity> orderBookList = orderBookDao.queryList(map);
			for (OrderBookEntity orderBook : orderBookList) {
				bookDao.addStock(orderBook.getBookId(), orderBook.getNum());
			}
		}
	}

	@Override
	@Transactional
	public void returnBook(OrderEntity order) {
		orderDao.update(order);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("orderId", order.getId());
		List<OrderBookEntity> orderBookList = orderBookDao.queryList(map);
		for (OrderBookEntity orderBook : orderBookList) {
			bookDao.addStock(orderBook.getBookId(), orderBook.getNum());
		}
	}
	
}
