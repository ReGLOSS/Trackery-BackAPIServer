package com.trackery.trackerybackapiserver.domain.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.dto
 * fileName       : TagForImageResponseDto
 * author         : inari
 * date           : 25. 7. 12.
 * description    : 이미지 조회 시 태그 정보를 위한 간소화된 응답 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 12.        inari        최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagForImageResponseDto {
	/**
	 * 태그 ID.
	 */
	private Long tagId;
	
	/**
	 * 태그명.
	 */
	private String tagName;
}
