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
 * 25. 8. 6.      durururuk       최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqsMessageConsumer {

	private final SqsClient sqsClient;
	private final ObjectMapper objectMapper;
	private final ImageService imageService;
	private final SseService sseService;

	@Value("${aws.sqs.queue-url}")
	private String queueUrl;

	@Async
	@EventListener(ApplicationReadyEvent.class)
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

			//DB 업데이트
			imageService.changeImageProcessingStatus(processedMessage.getImageName(), 1);

			//SSE로 클라이언트에 실시간 알림 전송
			sseService.sendImageProcessedEvent(processedMessage.getUserId(), processedMessage);

		} catch (Exception e) {
			log.error("SQS 메시지 처리 실패: {}", message.messageId(), e);
		}
	}

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
