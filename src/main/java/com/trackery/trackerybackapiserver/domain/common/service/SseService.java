package com.trackery.trackerybackapiserver.domain.common.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.service
 * fileName       : SseService
 * author         : durururuk
 * date           : 25. 8. 8.
 * description    : Server-Sent Events 관리 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 8.      durururuk       최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	public SseEmitter createEmitter(Long userId) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

		emitters.put(userId, emitter);
		log.info("SSE 연결 생성: userId={}", userId);

		emitter.onCompletion(() -> {
			log.info("SSE 연결 완료: userId={}", userId);
			emitters.remove(userId);
		});

		emitter.onTimeout(() -> {
			log.info("SSE 연결 타임아웃: userId={}", userId);
			emitters.remove(userId);
		});

		emitter.onError((e) -> {
			log.error("SSE 연결 오류: userId={}", userId, e);
			emitters.remove(userId);
		});

		return emitter;
	}

	public void sendImageProcessedEvent(Long userId, Object data) {
		SseEmitter emitter = emitters.get(userId);
		if (emitter == null) {
			log.warn("SSE 연결을 찾을 수 없음: userId={}", userId);
			return;
		}

		try {
			emitter.send(SseEmitter.event()
				.name("image-processed")
				.data(data));
			log.info("이미지 처리 완료 이벤트 전송: userId={}", userId);
		} catch (IOException e) {
			log.error("SSE 이벤트 전송 실패: userId={}", userId, e);
			emitters.remove(userId);
		}
	}

	public void removeEmitter(Long userId) {
		SseEmitter emitter = emitters.remove(userId);
		if (emitter != null) {
			emitter.complete();
			log.info("SSE 연결 수동 제거: userId={}", userId);
		}
	}

	public boolean hasEmitter(Long userId) {
		return emitters.containsKey(userId);
	}
}
