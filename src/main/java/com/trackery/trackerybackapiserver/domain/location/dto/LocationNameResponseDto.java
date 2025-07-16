package com.trackery.trackerybackapiserver.domain.location.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : LocationNameResponseDto
 * author         : Nari-Lee
 * date           : 25. 7. 7.
 * description    : 위치명 응답 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.		Nari-Lee		최초 생성
 * 25. 7. 7.		Nari-Lee		이미지 업로드시 시도,시군구 태그 추가 기능 구현
 * 25. 7. 11.		Nari-Lee		태그 리스트 삭제
 * 25. 7. 14.		Nari-Lee		locationName을 sdName + sggName으로 수정
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
