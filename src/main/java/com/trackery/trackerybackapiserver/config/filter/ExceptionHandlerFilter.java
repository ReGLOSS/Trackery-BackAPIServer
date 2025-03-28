package com.trackery.trackerybackapiserver.config.filter;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : ExceptionHandlerFilter
 * author         : durururuk
 * date           : 25. 3. 14.
 * description    : 필터단에서 발생하는 예외를 처리하는 필터입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 14.        durururuk      최초 생성
 */
public class ExceptionHandlerFilter extends OncePerRequestFilter {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (ApiException e) {
			setErrorResponse(e.getErrorCode().getStatus(), response, e.getErrorCode());
		}
	}

	public void setErrorResponse(HttpStatus status, HttpServletResponse response,
		ErrorCode errorCode) throws IOException {
		response.setStatus(status.value());
		response.setContentType("application/json; charset=UTF-8");

		String json = objectMapper.writeValueAsString(ApiResponse.error(errorCode));

		response.getWriter().write(json);
	}
}
