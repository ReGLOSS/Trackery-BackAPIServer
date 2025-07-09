package com.trackery.trackerybackapiserver.domain.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.dto
 * fileName       : TagTimeRequestDto
 * author         : inari
 * date           : 25. 7. 7.
 * description    : 날짜/시간 기반 기본 태그 생성 요청 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.        inari       최초 생성
 * 25. 7. 8.        inari       AllArgsConstructor 추가
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TagTimeRequestDto {
	/**
	 * 날짜/시간 문자열 (기본 태그 생성용).
	 */
	private String dateTime;
}
