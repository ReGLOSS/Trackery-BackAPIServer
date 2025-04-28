package com.trackery.trackerybackapiserver.domain.location.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.locationtech.jts.geom.Point;

import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.mapper
 * fileName       : LocationMapper
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 */
@Mapper
public interface LocationMapper{
	/**
	 * 좌표로 시군구 엔티티를 DB에서 조회하고 반환하는 메서드입니다.
	 * @param coordinateDto 좌표 DTO
	 * @return 시군구 엔티티를 Optional로 반환합니다.
	 */
	Optional<JusoSigungu> findSigunguByCoordinate(CoordinateDto coordinateDto);

	/**
	 * Point 객체로 시군구 엔티티를 DB에서 조회하고 반환하는 메서드입니다.
	 * @param point Point 객체
	 * @return 시군구 엔티티를 Optional로 반환합니다.
	 */
	Optional<JusoSigungu> findSigunguByPoint(Point point);

	/**
	 * CoordinatePoint 정보를 DB에 삽입합니다.
	 * @param coordinatePoint CoordinatePoint 객체
	 */
	void insertCoordinatePoint(CoordinatePoint coordinatePoint);
}
