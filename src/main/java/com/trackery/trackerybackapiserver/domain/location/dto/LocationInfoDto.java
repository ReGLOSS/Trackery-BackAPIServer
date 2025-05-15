package com.trackery.trackerybackapiserver.domain.location.dto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : LocationInfoDto
 * author         : durururuk
 * date           : 25. 5. 15.
 * description    : 위치 정보를 담는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 15.		durururuk		최초 생성
 */
public record LocationInfoDto(
	/*
	  latitude : 위도
	  longitude : 경도
	  sidoName : 시/도 명
	  sigunguName : 시/군/구 명
	 */
	double latitude,
	double longitude,
	String sidoName,
	String sigunguName
) {
}
