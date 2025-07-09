package com.trackery.trackerybackapiserver.domain.tag.dto;

import java.time.LocalDateTime;

import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.dto
 * fileName       : TagResponseDto
 * author         : inari
 * date           : 25. 7. 2.
 * description    : 태그 응답 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 2.        inari        최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagResponseDto {
	/**
	 * 태그 ID.
	 */
	private Long tagId;
	/**
	 * 태그명.
	 */
	private String tagName;
	/**
	 * 태그 타입.
	 */
	private TagType tagType;
	/**
	 * 태그 사용 횟수.
	 */
	private Long tagUseCount;
	/**
	 * 생성일시.
	 */
	private LocalDateTime createdAt;
}
