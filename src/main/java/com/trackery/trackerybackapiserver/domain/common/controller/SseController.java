package com.trackery.trackerybackapiserver.domain.common.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.trackery.trackerybackapiserver.domain.common.service.SseService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.controller
 * fileName       : SseController
 * author         : durururuk
 * date           : 25. 8. 8.
 * description    : Server-Sent Events 연결 관리 컨트롤러
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 8.      durururuk       최초 생성
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

	private final SseService sseService;

	/**
	 * SSE 연결을 생성합니다.
	 * @param userDetails 인증된 사용자 정보
	 * @return SseEmitter 객체
	 */
	@GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter connect(@AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getUserId();
		log.info("SSE 연결 요청: userId={}", userId);

		return sseService.createEmitter(userId);
	}

	/**
	 * SSE 연결을 수동으로 해제합니다.
	 * @param userDetails 인증된 사용자 정보
	 */
	@DeleteMapping("/disconnect")
	public void disconnect(@AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getUserId();
		log.info("SSE 연결 해제 요청: userId={}", userId);

		sseService.removeEmitter(userId);
	}
}
