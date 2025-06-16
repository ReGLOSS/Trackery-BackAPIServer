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
import com.trackery.trackerybackapiserver.domain.location.dto.HomeStatsResponseDto;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {
	private final LocationMapper locationMapper;
	private final GeometryFactory geometryFactory = new GeometryFactory();

	/**
	 * 좌표로 시도 + 시군구 주소를 조회하는 메서드입니다.
	 * @param coordinateDto 좌표 DTO : latitude(위도), longitude(경도) 둘 다 double 타입입니다.
	 * @return : 시도 + 시군구 주소를 String으로 반환합니다 (예시 : 부산광역시 수영구)
	 */
	public String getLocationNameByCoord(CoordinateDto coordinateDto) {
		JusoSigungu sigungu = locationMapper.findSigunguByCoordinate(coordinateDto).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND)
		);

		return String.format("%s %s", sigungu.getSido().getSidoName(), sigungu.getSigunguName());
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
	 * 좌표 객체 CoordinatePoint 객체를 생성하고 DB에 삽입하는 메서드입니다.
	 * @param coordinateDto 좌표 DTO
	 * @return DB에서 자동으로 할당된 ID를 포함하는 CoordinatePoint 객체
	 */
	@Transactional
	public CoordinatePoint insertCoordinatePoint(CoordinateDto coordinateDto) {
		Point point = getPointByCoord(coordinateDto);
		JusoSigungu sigungu = getSigunguByPoint(point);
		String pointName = String.format("%s %s", sigungu.getSido().getSidoName(), sigungu.getSigunguName());

		CoordinatePoint coordinatePoint = CoordinatePoint.builder()
			.coordinatePointName(pointName)
			.coordinatePointPoint(point)
			.coordinatePointType((byte)1)
			.firstRegisteredDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
			.lastModifiedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
			.sigungu(sigungu)
			.build();

		locationMapper.insertCoordinatePoint(coordinatePoint);

		return coordinatePoint;
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
	 * 홈 화면용 시도 목록과 사용자 통계를 함께 조회합니다.
	 */
	public HomeStatsResponseDto getHomeStatsData(Long userId) {
		log.debug("홈 화면 데이터 조회 - 사용자 ID: {}", userId);
		
		List<JusoSido> sidoList = getAllSido();
		UserStatsDto stats = locationMapper.getUserStats(userId);
		
		log.debug("홈 화면 데이터 조회 완료 - 시도 {}개, 이미지 {}개, 앨범 {}개, 시군구 {}개", 
			sidoList.size(), stats.getImageCount(), stats.getAlbumCount(), stats.getSigunguCount());
		
		return new HomeStatsResponseDto(sidoList, stats);
	}
}
