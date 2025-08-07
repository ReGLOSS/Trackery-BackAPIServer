package com.trackery.trackerybackapiserver.domain.aws.dto;

import com.trackery.trackerybackapiserver.domain.aws.enums.MessageType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.bokkurin.trackery.dto
 * fileName       : ImageProcessedMessage
 * author         : durururuk
 * date           : 25. 8. 6.
 * description    : SQS로 전달될 이벤트 메시지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 8. 6.      durururuk       최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageProcessedMessage {
	private MessageType type;
	private String batchId;
	private String userId;
	private String originalKey;
	private String processedKey;
	private String thumbnailKey;
	private String imageName;
	private String processedAt;
}