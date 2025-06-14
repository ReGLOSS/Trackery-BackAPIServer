package com.trackery.trackerybackapiserver.domain.location.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.locationtech.jts.geom.Point;

import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateRequestDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.mapper
 * fileName       : LocationMapper
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 지도 데이터 처리를 위한 MyBatis Mapper 인터페이스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 * 25. 6. 13.		inari			시군구 ID로 시군구 정보를 조회 매퍼 추가
 */
@Mapper
public interface LocationMapper {
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

	/**
	 * 대한민국의 모든 시도 목록을 조회합니다.
	 */
	List<JusoSido> findAllSido();

	/**
	 * 특정 시도에 속한 모든 시군구 목록을 조회합니다.
	 */
	List<JusoSigungu> findSigunguBySido(Long sidoId);

	/**
	 * 지리 좌표를 기반으로 해당 위치의 시군구 정보를 조회합니다.
	 */
	JusoSigungu findSigunguByCoordinateRequest(CoordinateRequestDto coordinate);

	/**
	 * 특정 시도의 경계선을 GeoJSON 형식으로 조회합니다.
	 */
	String findSidoBorderAsGeoJson(Long sidoId);

	/**
	 * 특정 시군구의 경계선을 GeoJSON 형식으로 조회합니다.
	 */
	String findSigunguBorderAsGeoJson(Long sigunguId);

	/**
	 * 시군구 ID로 시군구 정보를 조회합니다.
	 */
	Optional<JusoSigungu> findSigunguById(Long sigunguId);
}
