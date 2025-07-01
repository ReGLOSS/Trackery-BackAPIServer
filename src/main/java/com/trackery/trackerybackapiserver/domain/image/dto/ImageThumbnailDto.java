package com.trackery.trackerybackapiserver.domain.image.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto
 * fileName       : ImageThumbnailDto
 * author         : durururuk
 * date           : 25. 7. 1.
 * description    : 이미지 ID와 썸네일 이미지 주소를 담는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 1.		durururuk		최초 생성
 */
@Getter
@Builder
public class ImageThumbnailDto {
	private Long imageId;
	private String thumbnailUrl;
}
