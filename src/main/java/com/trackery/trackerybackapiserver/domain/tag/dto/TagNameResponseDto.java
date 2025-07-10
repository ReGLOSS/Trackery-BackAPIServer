package com.trackery.trackerybackapiserver.domain.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.dto
 * fileName       : TagNameResponseDto
 * author         : inari
 * date           : 25. 7. 7.
 * description    : 태그명만 포함한 간단한 응답 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.        inari        최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagNameResponseDto {
	/**
	 * 태그명.
	 */
	private String tagName;
}
