package com.trackery.trackerybackapiserver.domain.image.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.trackery.trackerybackapiserver.domain.tag.dto.TagForImageResponseDto;

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
 * 25. 5. 15.		durururuk		이미지 조회 기능 구현
 * 25. 7. 10.		inari		이미지 단건 조회시 태그 추가
 * 25. 7. 12.       inari       	태그 id 추가
 * 25. 7. 13.		inari		이미지 수정시 태그 삭제 기능 구현
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
	private List<TagForImageResponseDto> tags;
}
