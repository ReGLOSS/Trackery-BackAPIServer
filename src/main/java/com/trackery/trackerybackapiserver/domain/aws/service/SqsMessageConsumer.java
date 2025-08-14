package com.trackery.trackerybackapiserver.domain.aws.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.domain.aws.dto.ImageProcessedMessage;
import com.trackery.trackerybackapiserver.domain.common.service.SseService;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.aws.service
 * fileName       : SqsMessageConsumer
 * author         : durururuk
 * date           : 25. 8. 6.
 * description    : SQS 메시지 컨슈머
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 6.		durururuk       최초 생성
 * 25. 8. 7.		durururuk		sqs 이벤트 리스너 기능 구현
 * 25. 8. 14.		durururuk		javadoc 주석 추가
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqsMessageConsumer {

	/**
	 * AWS SQS 클라이언트
	 */
	private final SqsClient sqsClient;

	/**
	 * JSON 메시지 파싱을 위한 ObjectMapper
	 */
	private final ObjectMapper objectMapper;

	/**
	 * 이미지 처리 상태 업데이트를 위한 서비스
	 */
	private final ImageService imageService;

	/**
	 * 클라이언트에게 실시간 이벤트 전송을 위한 SSE 서비스
	 */
	private final SseService sseService;

	/**
	 * SQS 큐 URL
	 */
	@Value("${aws.sqs.queue-url}")
	private String queueUrl;

	/**
	 * SQS 메시지를 소비하는 메인 메서드입니다.
	 * 애플리케이션 시작 후 자동으로 실행되며, Long Polling을 사용하여 메시지를 수신합니다.
	 * 최대 20초간 대기하며, 한 번에 최대 10개의 메시지를 처리할 수 있습니다.
	 *
	 * @throws InterruptedException 스레드가 중단된 경우
	 */
	@Async
	@EventListener(ApplicationReadyEvent.class)
	@SuppressWarnings("BusyWait")
	public void consumeMessage() throws InterruptedException {
		log.info("SQS 메시지 컨슈머 시작");

		while (!Thread.currentThread().isInterrupted()) {
			try {
				// Long polling (최대 20초 대기)
				ReceiveMessageRequest request = ReceiveMessageRequest.builder()
					.queueUrl(queueUrl)
					.waitTimeSeconds(20)
					.maxNumberOfMessages(10)
					.build();

				List<Message> messages = sqsClient.receiveMessage(request).messages();

				for (Message message : messages) {
					processMessage(message);
					deleteMessage(message);
				}
			} catch (Exception e) {
				log.error("SQS 메시지 수신 실패", e);
				Thread.sleep(5000);
			}
		}
	}

	/**
	 * SQS 메시지를 처리합니다.
	 * 메시지 본문을 ImageProcessedMessage로 파싱하고, 이미지 처리 상태를 업데이트한 후
	 * SSE를 통해 클라이언트에게 처리 완료 이벤트를 전송합니다.
	 *
	 * @param message 처리할 SQS 메시지
	 */
	private void processMessage(Message message) {
		try {
			log.info("Processing message: {}", message.messageId());

			String body = message.body();
			ImageProcessedMessage processedMessage = objectMapper.readValue(body, ImageProcessedMessage.class);

			log.info("SQS 수신 메시지 - Type: {}, BatchId: {}, UserId: {}", processedMessage.getType(),
				processedMessage.getBatchId(), processedMessage.getUserId());
			log.info("SQS 수신 메시지 - 이미지 키 - Original: {}, Processed: {}, Thumbnail: {}, ImageName : {}",
				processedMessage.getOriginalKey(), processedMessage.getProcessedKey(),
				processedMessage.getThumbnailKey(), processedMessage.getImageName());

			imageService.changeImageProcessingStatus(processedMessage.getImageName(), 1);

			sseService.sendImageProcessedEvent(processedMessage.getUserId(), processedMessage);

		} catch (Exception e) {
			log.error("SQS 메시지 처리 실패: {}", message.messageId(), e);
		}
	}

	/**
	 * 처리 완료된 SQS 메시지를 큐에서 삭제합니다.
	 * 메시지 삭제에 실패하면 로그를 남기지만 예외를 던지지 않습니다.
	 *
	 * @param message 삭제할 SQS 메시지
	 */
	private void deleteMessage(Message message) {
		try {
			DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
				.queueUrl(queueUrl)
				.receiptHandle(message.receiptHandle())
				.build();

			sqsClient.deleteMessage(deleteRequest);
			log.info("SQS 메시지 삭제 완료: {}", message.messageId());

		} catch (Exception e) {
			log.error("SQS 메시지 삭제 실패: {}", message.messageId(), e);
		}
	}
}
