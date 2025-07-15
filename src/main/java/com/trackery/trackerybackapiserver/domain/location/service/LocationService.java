package com.trackery.trackerybackapiserver.domain.location.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateRequestDto;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationNameResponseDto;
import com.trackery.trackerybackapiserver.domain.location.dto.MapResponseDto;
import com.trackery.trackerybackapiserver.domain.location.dto.UserStatsDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;
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
 * 25. 6. 13.		inari			시군구 ID로 시군구 정보를 조회 메서드 추가
 * 25. 6. 14.		inari			홈화면 전국지도용 통계 추가
 * 25. 6. 22.		inari			좌표 업데이트 메서드 추가
 * 25. 6. 27.		inari			코드 스멜 수정
 * 25. 7. 7.		inari			지역 태그 서비스 추가
 * 25. 7. 8.		inari			시군구 ID로 시도명과 시군구명을 분리한 메서드 추가
 * 25. 7. 10.		inari			태그를 태그 도메인으로 분리
 * 25. 7. 15.		inari			updateImageLocation을 이미지 서비스에서 이동
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

	private final LocationMapper locationMapper;
	private final GeometryFactory geometryFactory = new GeometryFactory();
	public static final String SEOUL =  "Asia/Seoul";
	private static final String SIDO_SIGUNGU_FORMAT = "%s %s";

	/**
	 * 좌표로 시도명과 시군구명을 분리하여 조회하는 메서드입니다.
	 * @param coordinateDto 좌표 DTO : latitude(위도), longitude(경도) 둘 다 double 타입입니다.
	 * @return : 시도명과 시군구명을 분리한 응답 DTO
	 */
	public LocationNameResponseDto getLocationNameByCoord(CoordinateDto coordinateDto) {
		JusoSigungu sigungu = locationMapper.findSigunguByCoordinate(coordinateDto).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND)
		);

		return LocationNameResponseDto.builder()
			.sdName(sigungu.getSido().getSidoName())
			.sggName(sigungu.getSigunguName())
			.build();
	}


	/**
	 * 좌표로 Point 타입 객체를 반환하는 메서드입니다.
	 * @param coordinateDto : 좌표 DTO
	 * @return : jts 라이브러리의 Point 객체
	 */
	private Point getPointByCoord(CoordinateDto coordinateDto) {
		Coordinate coordinate = new Coordinate(coordinateDto.longitude(), coordinateDto.latitude());
		return geometryFactory.createPoint(coordinate);
	}

	/**
	 * Point 객체로 시군구 엔티티를 반환합니다.
	 * @param point : Point 객체
	 * @return : JusoSigungu (시군구 ID, 이름, 시도 ID, 이름을 담고 있는 엔티티 객체)
	 */
	public JusoSigungu getSigunguByPoint(Point point) {
		return locationMapper.findSigunguByPoint(point).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND)

		);
	}

	/**
	 * 위도와 경도로 시군구 정보를 조회합니다.
	 * @param latitude 위도
	 * @param longitude 경도
	 * @return 시군구 정보 (null일 수 있음)
	 */
	public JusoSigungu findSigunguByCoordinate(double latitude, double longitude) {
		try {
			CoordinateDto coordinateDto = new CoordinateDto(latitude, longitude);
			Point point = getPointByCoord(coordinateDto);
			return getSigunguByPoint(point);
		} catch (ApiException e) {
			log.warn("Failed to find sigungu for coordinates ({}, {}): {}", latitude, longitude, e.getMessage());
			return null;
		}
	}

	/**
	 * 좌표 객체 CoordinatePoint 객체를 생성하고 DB에 삽입하는 메서드입니다.
	 * @param coordinateDto 좌표 DTO
	 * @return DB에서 자동으로 할당된 ID를 포함하는 CoordinatePoint 객체
	 */
	@Transactional
	public CoordinatePoint insertCoordinatePoint(CoordinateDto coordinateDto) {
		Point point = getPointByCoord(coordinateDto);
		JusoSigungu sigungu = getSigunguByPoint(point);
		String pointName = String.format(SIDO_SIGUNGU_FORMAT, sigungu.getSido().getSidoName(),
			sigungu.getSigunguName());

		CoordinatePoint coordinatePoint = CoordinatePoint.builder()
			.coordinatePointName(pointName)
			.coordinatePointPoint(point)
			.coordinatePointType((byte)1)
			.firstRegisteredDate(LocalDateTime.now(ZoneId.of(SEOUL)))
			.lastModifiedDate(LocalDateTime.now(ZoneId.of(SEOUL)))
			.sigungu(sigungu)
			.build();

		locationMapper.insertCoordinatePoint(coordinatePoint);

		return coordinatePoint;
	}

	/**
	 * 기존 CoordinatePoint 객체의 좌표 정보를 업데이트하는 메서드입니다.
	 * @param coordinatePointId 업데이트할 좌표 포인트 ID
	 * @param coordinateDto 새로운 좌표 DTO
	 * @return 업데이트된 행 수
	 */
	public int updateCoordinatePoint(Long coordinatePointId, CoordinateDto coordinateDto) {
		Point point = getPointByCoord(coordinateDto);
		JusoSigungu sigungu = getSigunguByPoint(point);
		String pointName = String.format(SIDO_SIGUNGU_FORMAT, sigungu.getSido().getSidoName(),
			sigungu.getSigunguName());

		return locationMapper.updateCoordinatePointById(coordinatePointId, pointName, point, sigungu.getSigunguId(),
			LocalDateTime.now(ZoneId.of(SEOUL)));
	}

	/**
	 * 대한민국의 모든 시도 목록을 조회합니다.
	 */
	public List<JusoSido> getAllSido() {
		log.debug("전체 시도 목록 조회");
		return locationMapper.findAllSido();
	}

	/**
	 * 특정 시도에 속한 모든 시군구 목록을 조회합니다.
	 */
	public List<JusoSigungu> getSigunguBySido(Long sidoId) {
		log.debug("시도 ID {}의 시군구 목록 조회", sidoId);
		List<JusoSigungu> sigunguList = locationMapper.findSigunguBySido(sidoId);

		if (sigunguList.isEmpty()) {
			log.warn("시도 ID {}에 해당하는 시군구가 없습니다.", sidoId);
		} else {
			log.debug("시도 ID {}의 시군구 목록 조회 결과: {} 개", sidoId, sigunguList.size());
			sigunguList.forEach(sigungu ->
				log.debug("- {} (ID: {})", sigungu.getSigunguName(), sigungu.getSigunguId())
			);
		}

		return sigunguList;
	}

	/**
	 * 지리 좌표를 기반으로 해당 위치의 시군구 정보를 DTO로 반환합니다.
	 */
	public MapResponseDto getSigunguByCoordinateDto(CoordinateRequestDto request) {
		log.debug("좌표로 시군구 조회 (DTO 반환): latitude={}, longitude={}",
			request.getLatitude(), request.getLongitude());

		JusoSigungu sigungu = locationMapper.findSigunguByCoordinateRequest(request);

		if (sigungu == null) {
			log.error("좌표에 해당하는 시군구를 찾을 수 없음: {}", request);
			throw new ApiException(ErrorCode.NOT_FOUND_SIGUNGU);
		}

		return MapResponseDto.from(sigungu);
	}

	/**
	 * 특정 시도의 경계선을 GeoJSON 형식으로 조회합니다.
	 */
	public String getSidoBorderAsGeoJson(Long sidoId) {
		log.debug("시도 ID {}의 경계선 GeoJSON 조회", sidoId);

		String geoJson = locationMapper.findSidoBorderAsGeoJson(sidoId);

		if (geoJson == null) {
			log.error("시도 ID {}를 찾을 수 없습니다.", sidoId);
			throw new ApiException(ErrorCode.NOT_FOUND_SIDO);
		}

		return geoJson;
	}

	/**
	 * 특정 시군구의 경계선을 GeoJSON 형식으로 조회합니다.
	 */
	public String getSigunguBorderAsGeoJson(Long sigunguId) {
		log.debug("시군구 ID {}의 경계선 GeoJSON 조회", sigunguId);

		String geoJson = locationMapper.findSigunguBorderAsGeoJson(sigunguId);

		if (geoJson == null) {
			log.error("시군구 ID {}를 찾을 수 없습니다.", sigunguId);
			throw new ApiException(ErrorCode.NOT_FOUND_SIGUNGU);
		}

		return geoJson;
	}

	/**
	 * 시군구 ID로 시군구 정보를 조회합니다.
	 */
	public JusoSigungu getSigunguById(Long sigunguId) {
		log.debug("시군구 ID {}로 시군구 정보 조회", sigunguId);
		JusoSigungu sigungu = locationMapper.findSigunguById(sigunguId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_SIGUNGU)
		);
		log.debug("시군구 정보 조회 결과: {} {} (ID: {})",
			sigungu.getSido().getSidoName(), sigungu.getSigunguName(), sigungu.getSigunguId());
		return sigungu;
	}

	/**
	 * 시군구 ID로 시도명과 시군구명을 분리하여 조회합니다.
	 */
	public LocationInfoDto getSigunguLocationInfoById(Long sigunguId) {
		log.debug("시군구 ID {}로 위치 정보 조회", sigunguId);
		JusoSigungu sigungu = locationMapper.findSigunguById(sigunguId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_SIGUNGU)
		);
		log.debug("위치 정보 조회 결과: {} {} (ID: {})",
			sigungu.getSido().getSidoName(), sigungu.getSigunguName(), sigungu.getSigunguId());
		return new LocationInfoDto(0.0, 0.0, sigungu.getSido().getSidoName(), sigungu.getSigunguName());
	}

	/**
	 * 사용자 통계 정보를 조회합니다.
	 */
	public UserStatsDto getUserStats(Long userId) {
		log.debug("사용자 통계 조회 - 사용자 ID: {}", userId);
		UserStatsDto stats = locationMapper.getUserStats(userId);
		log.debug("사용자 통계 조회 완료 - 이미지 {}개, 앨범 {}개, 시군구 {}개",
			stats.getImageCount(), stats.getAlbumCount(), stats.getSigunguCount());
		return stats;
	}

	/**
	 * 위치 정보가 제공된 경우 기존 CoordinatePoint의 위치 정보를 수정합니다.
	 * @param coordinatePointId 수정할 좌표 포인트 ID
	 * @param latitude 새로운 위도
	 * @param longitude 새로운 경도
	 * @param imageId 로그용 이미지 ID
	 * @throws ApiException 위치 정보 수정 실패 시
	 */
	@Transactional
	public void updateImageLocation(Long coordinatePointId, Double latitude, Double longitude, Long imageId) {
		if (latitude == null || longitude == null) {
			return;
		}

		try {
			CoordinateDto coordinateDto = new CoordinateDto(latitude, longitude);
			updateCoordinatePoint(coordinatePointId, coordinateDto);

			log.info("이미지 위치 정보 수정 완료 - imageId: {}, coord_point_id: {}, 새로운 위치: {}, {}",
				imageId, coordinatePointId, latitude, longitude);
		} catch (Exception e) {
			log.error("이미지 위치 정보 수정 실패 - imageId: {}, 에러: {}", imageId, e.getMessage());
			throw new ApiException(ErrorCode.UPDATE_FAILED_LOCATION);
		}
	}
}
