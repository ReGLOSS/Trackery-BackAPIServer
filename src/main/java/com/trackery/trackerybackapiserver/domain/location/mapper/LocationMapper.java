package com.trackery.trackerybackapiserver.domain.location.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.locationtech.jts.geom.Point;

import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateRequestDto;
import com.trackery.trackerybackapiserver.domain.location.dto.UserStatsDto;
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
 * 25. 4. 15.		durururuk		좌표로 지역 찾아오는 기능 구현
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 4. 21.		durururuk		insert CoordinatePoint 기능 작성
 * 25. 4. 23.		durururuk		LocationMapper JavaDoc 주석 추가
 * 25. 6. 10.		inari		location과 통합
 * 25. 6. 13.		inari		시군구 조회 기능 추가
 * 25. 6. 15.		inari		체크스타일 수정
 * 25. 6. 16.		inari		전국지도용 통계 추가
 * 25. 6. 22.		inari		이미지 메타데이터 수정시 좌표 인서트가 아닌 업데이트로 변경
 * 25. 6. 23.		inari		코드스멜 수정
 * 25. 6. 24.		inari		코드스멜 해결
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
	 * ID로 CoordinatePoint 정보를 수정합니다.
	 * @param coordinatePointId 좌표 포인트 ID
	 * @param coordinatePointName 좌표 포인트 이름
	 * @param coordinatePointPoint 좌표 포인트
	 * @param sigunguId 시군구 ID
	 * @param lastModifiedDate 수정 시간
	 * @return 수정된 행 수
	 */
	int updateCoordinatePointById(Long coordinatePointId, String coordinatePointName, Point coordinatePointPoint,
		Long sigunguId, LocalDateTime lastModifiedDate);

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

	/**
	 * 특정 사용자의 통계 정보를 조회합니다.
	 * @param userId 사용자 ID
	 * @return 사용자 통계 정보
	 */
	UserStatsDto getUserStats(Long userId);
}
