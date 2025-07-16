package com.trackery.trackerybackapiserver.domain.location.service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.service
 * fileName       : LocationUtil
 * author         : durururuk
 * date           : 25. 5. 15.
 * description    : 위치 관련 유틸 메서드를 담은 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 15.		durururuk		최초 생성
 * 25. 5. 15.		durururuk		이미지 조회 기능 구현
 */
public class LocationUtil {
	private LocationUtil() {
		throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_UTIL_CLASS_INSTANTIATED);
	}

	/**
	 * CoordinatePoint 객체에서 정보를 추출해서 DTO를
	 * 반환하는 메서드입니다.
	 * @param coordPoint CoordinatePoint 객체
	 * @return LocationInfoDto : 위도, 경도, 시/도 명, 시/군/구 명을 담고있는 DTO
	 */
	public static LocationInfoDto getLocationInfoByCoordinatePoint(CoordinatePoint coordPoint) {
		double latitude = coordPoint.getCoordinatePointPoint().getX();
		double longitude = coordPoint.getCoordinatePointPoint().getY();

		String sidoName = coordPoint.getSigungu().getSido().getSidoName();
		String sigunguName = coordPoint.getSigungu().getSigunguName();

		return new LocationInfoDto(latitude, longitude, sidoName, sigunguName);
	}
}
