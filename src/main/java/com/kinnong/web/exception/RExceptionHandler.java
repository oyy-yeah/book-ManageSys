package com.kinnong.web.exception;

import com.kinnong.common.exception.RException;
import com.kinnong.common.utils.R;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理器
 *
 * <p>对于 {@link RException}，除在响应体中返回业务错误码外，同时设置对应的 HTTP 状态码
 * （401 未登录、403 无权限），便于前端统一拦截处理。</p>
 *
 * @author ZJL
 * @email 1044869436@qq.com
 */
@RestControllerAdvice
public class RExceptionHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 自定义异常：HTTP 状态码与业务码保持一致（错误码须在 400~599 之间）
	 */
	@ExceptionHandler(RException.class)
	public ResponseEntity<R> handleRRException(RException e){
		R r = new R();
		r.put("code", e.getCode());
		r.put("msg", e.getMessage());

		HttpStatus status = HttpStatus.OK;
		if (e.getCode() >= 400 && e.getCode() < 600) {
			status = HttpStatus.valueOf(e.getCode());
		}
		return ResponseEntity.status(status).body(r);
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public R handleDuplicateKeyException(DuplicateKeyException e){
		logger.error(e.getMessage(), e);
		return R.error("数据库中已存在该记录");
	}

	@ExceptionHandler(AuthorizationException.class)
	public R handleAuthorizationException(AuthorizationException e){
		logger.error(e.getMessage(), e);
		return R.error("没有权限，请联系管理员授权");
	}

	@ExceptionHandler(Exception.class)
	public R handleException(Exception e){
		logger.error(e.getMessage(), e);
		return R.error();
	}
}
