package com.trackery.trackerybackapiserver.domain.image.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto
 * fileName       : ImageDto
 * author         : durururuk
 * date           : 25. 5. 14.
 * description    : 이미지 조회할 때 사용되는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.		durururuk		최초 생성
 */
@Getter
@Builder
public class ImageDto {
	private Long imageId;
	private Long userId;
	private LocalDateTime imageRegDate;
	private String sdName;
	private String sggName;
	private double latitude;
	private double longitude;
	private String imageName;
	private String imageContent;
	private LocalDateTime imageDate;
	private Integer isPublic;
	private String imageUrl;
}
