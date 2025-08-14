package com.trackery.trackerybackapiserver.domain.aws.service;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.domain.aws.dto.ImageProcessedMessage;
import com.trackery.trackerybackapiserver.domain.aws.enums.MessageType;
import com.trackery.trackerybackapiserver.domain.common.service.SseService;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.aws.service
 * fileName       : SqsMessageConsumerTest
 * author         : durururuk
 * date           : 25. 8. 14.
 * description    : SqsMessageConsumer 단위 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 14.      durururuk       최초 생성
 * 25. 8. 14.		durururuk		단위 테스트 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SqsMessageConsumer 테스트")
class SqsMessageConsumerTest {

	@Mock
	private SqsClient sqsClient;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private ImageService imageService;

	@Mock
	private SseService sseService;

	@InjectMocks
	private SqsMessageConsumer sqsMessageConsumer;

	private static final String TEST_QUEUE_URL = "https://sqs.ap-northeast-2.amazonaws.com/123456789012/test-queue";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(sqsMessageConsumer, "queueUrl", TEST_QUEUE_URL);
	}

	@Nested
	@DisplayName("consumeMessage 메서드 테스트")
	class ConsumeMessageTest {

		@Test
		@DisplayName("메시지가 없을 때 정상적으로 처리된다")
		void consumeMessage_NoMessages_Success() throws Exception {
			AtomicBoolean methodCalled = new AtomicBoolean(false);

			ReceiveMessageResponse emptyResponse = ReceiveMessageResponse.builder()
				.messages(Collections.emptyList())
				.build();
			when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
				.thenAnswer(invocation -> {
					methodCalled.set(true);
					return emptyResponse;
				});

			Thread testThread = new Thread(() -> {
				try {
					sqsMessageConsumer.consumeMessage();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});

			testThread.start();

			await().atMost(Duration.ofSeconds(2))
				.until(methodCalled::get);

			testThread.interrupt();
			testThread.join(1000);

			verify(sqsClient, atLeastOnce()).receiveMessage(any(ReceiveMessageRequest.class));
		}

		@Test
		@DisplayName("메시지가 있을 때 정상적으로 처리하고 삭제한다")
		void consumeMessage_WithMessages_ProcessesAndDeletes() throws Exception {
			Message testMessage = createTestMessage();
			ImageProcessedMessage processedMessage = createTestImageProcessedMessage();
			AtomicBoolean messageProcessed = new AtomicBoolean(false);

			ReceiveMessageResponse messageResponse = ReceiveMessageResponse.builder()
				.messages(Collections.singletonList(testMessage))
				.build();
			
			ReceiveMessageResponse emptyResponse = ReceiveMessageResponse.builder()
				.messages(Collections.emptyList())
				.build();

			when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
				.thenReturn(messageResponse)
				.thenReturn(emptyResponse);

			when(objectMapper.readValue(anyString(), eq(ImageProcessedMessage.class)))
				.thenReturn(processedMessage);

			when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
				.thenAnswer(invocation -> {
					messageProcessed.set(true);
					return DeleteMessageResponse.builder().build();
				});

			Thread testThread = new Thread(() -> {
				try {
					sqsMessageConsumer.consumeMessage();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});

			testThread.start();

			await().atMost(Duration.ofSeconds(2))
				.until(messageProcessed::get);

			testThread.interrupt();
			testThread.join(1000);

			verify(imageService, times(1)).changeImageProcessingStatus("test-image.jpg", 1);
			verify(sseService, times(1)).sendImageProcessedEvent(1L, processedMessage);
			verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
		}

		@Test
		@DisplayName("SQS 예외 발생 시 5초 대기 후 재시도한다")
		void consumeMessage_SqsException_RetriesAfterDelay() throws Exception {
			AtomicBoolean retryAttempted = new AtomicBoolean(false);

			when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
				.thenThrow(SqsException.builder().message("SQS Error").build())
				.thenAnswer(invocation -> {
					retryAttempted.set(true);
					throw SqsException.builder().message("Second error").build();
				});

			Thread testThread = new Thread(() -> {
				try {
					sqsMessageConsumer.consumeMessage();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});

			testThread.start();

			await().atMost(Duration.ofSeconds(7))
				.pollDelay(Duration.ofSeconds(5))
				.until(retryAttempted::get);

			testThread.interrupt();
			testThread.join(1000);

			verify(sqsClient, atLeast(2)).receiveMessage(any(ReceiveMessageRequest.class));
		}
	}

	@Nested
	@DisplayName("processMessage 메서드 테스트")
	class ProcessMessageTest {

		@Test
		@DisplayName("정상 메시지 처리 시 이미지 상태 업데이트와 SSE 이벤트 전송을 수행한다")
		void processMessage_ValidMessage_ProcessesSuccessfully() throws Exception {
			Message testMessage = createTestMessage();
			ImageProcessedMessage processedMessage = createTestImageProcessedMessage();

			when(objectMapper.readValue(anyString(), eq(ImageProcessedMessage.class)))
				.thenReturn(processedMessage);

			ReflectionTestUtils.invokeMethod(sqsMessageConsumer, "processMessage", testMessage);

			verify(objectMapper).readValue(testMessage.body(), ImageProcessedMessage.class);
			verify(imageService).changeImageProcessingStatus("test-image.jpg", 1);
			verify(sseService).sendImageProcessedEvent(1L, processedMessage);
		}

		@Test
		@DisplayName("JSON 파싱 실패 시 예외를 로깅하고 처리를 계속한다")
		void processMessage_JsonParsingError_LogsErrorAndContinues() throws Exception {
			Message testMessage = createTestMessage();

			when(objectMapper.readValue(anyString(), eq(ImageProcessedMessage.class)))
				.thenThrow(new JsonProcessingException("JSON parsing error") {
				});

			assertDoesNotThrow(() ->
				ReflectionTestUtils.invokeMethod(sqsMessageConsumer, "processMessage", testMessage)
			);

			verify(imageService, never()).changeImageProcessingStatus(anyString(), anyInt());
			verify(sseService, never()).sendImageProcessedEvent(anyLong(), any());
		}

		@Test
		@DisplayName("이미지 서비스 예외 발생 시 예외를 로깅하고 처리를 계속한다")
		void processMessage_ImageServiceError_LogsErrorAndContinues() throws Exception {
			Message testMessage = createTestMessage();
			ImageProcessedMessage processedMessage = createTestImageProcessedMessage();

			when(objectMapper.readValue(anyString(), eq(ImageProcessedMessage.class)))
				.thenReturn(processedMessage);
			doThrow(new RuntimeException("Image service error"))
				.when(imageService).changeImageProcessingStatus(anyString(), anyInt());

			assertDoesNotThrow(() ->
				ReflectionTestUtils.invokeMethod(sqsMessageConsumer, "processMessage", testMessage)
			);

			verify(sseService, never()).sendImageProcessedEvent(anyLong(), any());
		}

		@Test
		@DisplayName("SSE 서비스 예외 발생 시 예외를 로깅하고 처리를 계속한다")
		void processMessage_SseServiceError_LogsErrorAndContinues() throws Exception {
			Message testMessage = createTestMessage();
			ImageProcessedMessage processedMessage = createTestImageProcessedMessage();

			when(objectMapper.readValue(anyString(), eq(ImageProcessedMessage.class)))
				.thenReturn(processedMessage);
			doThrow(new RuntimeException("SSE service error"))
				.when(sseService).sendImageProcessedEvent(anyLong(), any());

			assertDoesNotThrow(() ->
				ReflectionTestUtils.invokeMethod(sqsMessageConsumer, "processMessage", testMessage)
			);

			verify(imageService).changeImageProcessingStatus("test-image.jpg", 1);
		}
	}

	@Nested
	@DisplayName("deleteMessage 메서드 테스트")
	class DeleteMessageTest {

		@Test
		@DisplayName("메시지 삭제가 정상적으로 수행된다")
		void deleteMessage_Success() {
			Message testMessage = createTestMessage();

			when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
				.thenReturn(DeleteMessageResponse.builder().build());

			assertDoesNotThrow(() ->
				ReflectionTestUtils.invokeMethod(sqsMessageConsumer, "deleteMessage", testMessage)
			);

			verify(sqsClient).deleteMessage(argThat((DeleteMessageRequest request) ->
				request.queueUrl().equals(TEST_QUEUE_URL) &&
					request.receiptHandle().equals("test-receipt-handle")
			));
		}

		@Test
		@DisplayName("메시지 삭제 실패 시 예외를 로깅하고 처리를 계속한다")
		void deleteMessage_Error_LogsErrorAndContinues() {
			Message testMessage = createTestMessage();

			when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
				.thenThrow(SqsException.builder().message("Delete failed").build());

			assertDoesNotThrow(() ->
				ReflectionTestUtils.invokeMethod(sqsMessageConsumer, "deleteMessage", testMessage)
			);

			verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
		}

		@Test
		@DisplayName("올바른 큐 URL과 receipt handle로 삭제 요청을 생성한다")
		void deleteMessage_CreatesCorrectDeleteRequest() {
			Message testMessage = createTestMessage();

			when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
				.thenReturn(DeleteMessageResponse.builder().build());

			ReflectionTestUtils.invokeMethod(sqsMessageConsumer, "deleteMessage", testMessage);

			verify(sqsClient).deleteMessage(argThat((DeleteMessageRequest request) -> {
				assertThat(request.queueUrl()).isEqualTo(TEST_QUEUE_URL);
				assertThat(request.receiptHandle()).isEqualTo("test-receipt-handle");
				return true;
			}));
		}
	}

	private Message createTestMessage() {
		return Message.builder()
			.messageId("test-message-id")
			.receiptHandle("test-receipt-handle")
			.body(
				"{\"type\":\"IMAGE_PROCESSED\",\"batchId\":\"batch-123\",\"userId\":1,\"originalKey\":\"original/test-image.jpg\",\"processedKey\":\"processed/test-image.jpg\",\"thumbnailKey\":\"thumbnail/test-image.jpg\",\"imageName\":\"test-image.jpg\",\"processedAt\":\"2025-08-14T10:00:00Z\"}")
			.build();
	}

	private ImageProcessedMessage createTestImageProcessedMessage() {
		return new ImageProcessedMessage(
			MessageType.SINGLE_IMAGE,
			"batch-123",
			1L,
			"original/test-image.jpg",
			"processed/test-image.jpg",
			"thumbnail/test-image.jpg",
			"test-image.jpg",
			"2025-08-14T10:00:00Z"
		);
	}
}