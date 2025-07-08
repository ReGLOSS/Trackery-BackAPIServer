package com.trackery.trackerybackapiserver.domain.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.dto
 * fileName       : TagCreateRequestDto
 * author         : inari
 * date           : 25. 7. 2.
 * description    : 태그 생성 요청 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 2.        inari       최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TagCreateRequestDto {
	/**
	 * 태그명.
	 */
	private String tagName;
	/**
	 * 태그 타입 (정수값).
	 */
	private Integer tagType;

}
