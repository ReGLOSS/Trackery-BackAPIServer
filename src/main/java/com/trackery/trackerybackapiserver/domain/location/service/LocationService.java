package com.trackery.trackerybackapiserver.domain.location.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;
import com.trackery.trackerybackapiserver.domain.location.mapper.LocationMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.service
 * fileName       : LocationService
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 위치 관련 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {
	private final LocationMapper locationMapper;
	private GeometryFactory geometryFactory = new GeometryFactory();

	public String getLocationNameByCoord(CoordinateDto coordinateDto) {
		JusoSigungu sigungu = locationMapper.findByCoord(coordinateDto).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND)
		);

		return String.format("%s %s", sigungu.getSido().getSidoName(), sigungu.getSigunguName());
	}

	public Point getPointByCoord(CoordinateDto coordinateDto) {
		Coordinate coordinate = new Coordinate(coordinateDto.longitude(), coordinateDto.latitude());
		return geometryFactory.createPoint(coordinate);
	}

	public JusoSigungu getSigunguByPoint(Point point) {
		return locationMapper.findByPoint(point).orElseThrow();
	}

	public List<String> test(CoordinateDto coordinateDto) {
		Point point = getPointByCoord(coordinateDto);

		JusoSigungu sigungu = getSigunguByPoint(point);

		List<String> result = new ArrayList<>();

		result.add(sigungu.getSigunguName());
		result.add(sigungu.getSido().getSidoName());

		return result;
	}

	@Transactional
	public CoordinatePoint insertCoordinatePoint(CoordinateDto coordinateDto) {
		Point point = getPointByCoord(coordinateDto);
		JusoSigungu sigungu = getSigunguByPoint(point);
		String pointName = String.format("%s, %s",sigungu.getSido().getSidoName(), sigungu.getSigunguName());

		CoordinatePoint coordinatePoint =  CoordinatePoint.builder()
			.coordinatePointName(pointName)
			.coordinatePointPoint(point)
			.coordinatePointType((byte) 1)
			.firstRegisteredDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
			.lastModifiedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
			.sigungu(sigungu)
			.build();

		return locationMapper.insertCoordinatePoint(coordinatePoint);
	}
}
