package com.trackery.trackerybackapiserver.domain.location.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : LocationNameResponseDto
 * author         : inari
 * date           : 25. 7. 7.
 * description    : 위치명 응답 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.        inari       최초 생성
 * 25. 7. 11.       inari       태그 리스트 삭제
 * 25. 7. 14.       inari       locationName을 sdName과 sggName으로 분리
 */
@Getter
@Builder
public class LocationNameResponseDto {
	/**
	 * 시도명.
	 */
	private String sdName;

	/**
	 * 시군구명.
	 */
	private String sggName;
}
