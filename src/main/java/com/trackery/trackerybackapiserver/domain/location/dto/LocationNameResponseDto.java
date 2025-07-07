package com.trackery.trackerybackapiserver.domain.location.dto;

import java.util.List;

import com.trackery.trackerybackapiserver.domain.tag.dto.TagNameResponseDto;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : LocationNameResponseDto
 * author         : inari
 * date           : 25. 7. 7.
 * description    : 위치명과 지역 태그를 포함한 응답 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.        inari       최초 생성
 */
@Getter
@Builder
public class LocationNameResponseDto {
	/**
	 * 위치명.
	 */
	private String locationName;

	/**
	 * 지역 태그 목록.
	 */
	private List<TagNameResponseDto> regionalTags;
}
