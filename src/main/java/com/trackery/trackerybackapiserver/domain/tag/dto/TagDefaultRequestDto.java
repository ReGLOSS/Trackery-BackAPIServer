package com.trackery.trackerybackapiserver.domain.tag.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.dto
 * fileName       : TagDefaultRequestDto
 * author         : inari
 * date           : 25. 7. 7.
 * description    : 날짜/시간 기반 기본 태그 생성 요청 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.        inari       최초 생성
 */
@Getter
@NoArgsConstructor
public class TagDefaultRequestDto {
	/**
	 * 날짜/시간 문자열 (기본 태그 생성용).
	 */
	private String dateTime;
}
