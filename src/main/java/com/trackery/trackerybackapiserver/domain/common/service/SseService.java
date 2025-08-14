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
 * 25. 8. 14.     durururuk       javadoc 주석 추가
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {
	
	/**
	 * SSE 연결 기본 타임아웃 (60분)
	 */
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
	
	/**
	 * 사용자 ID별 SSE Emitter를 저장하는 맵
	 */
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	/**
	 * 사용자를 위한 SSE Emitter를 생성합니다.
	 * 연결 완료, 타임아웃, 오류 상황에 대한 콜백을 설정하고 내부 맵에 저장합니다.
	 * 
	 * @param userId 사용자 ID
	 * @return 생성된 SSE Emitter
	 */
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

	/**
	 * 사용자에게 이미지 처리 완료 이벤트를 전송합니다.
	 * 해당 사용자의 SSE 연결이 존재하지 않으면 경고 로그를 남기고 종료합니다.
	 * 
	 * @param userId 이벤트를 받을 사용자 ID
	 * @param data 전송할 이미지 처리 데이터
	 */
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

	/**
	 * 사용자의 SSE Emitter를 수동으로 제거합니다.
	 * Emitter가 존재하면 연결을 완료 처리하고 맵에서 제거합니다.
	 * 
	 * @param userId 제거할 사용자 ID
	 */
	public void removeEmitter(Long userId) {
		SseEmitter emitter = emitters.remove(userId);
		if (emitter != null) {
			emitter.complete();
			log.info("SSE 연결 수동 제거: userId={}", userId);
		}
	}

	/**
	 * 사용자의 SSE Emitter 존재 여부를 확인합니다.
	 * 
	 * @param userId 확인할 사용자 ID
	 * @return SSE 연결 존재 여부
	 */
	public boolean hasEmitter(Long userId) {
		return emitters.containsKey(userId);
	}
}
