package com.trackery.trackerybackapiserver.domain.map.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.map.dto.CoordinateRequestDto;
import com.trackery.trackerybackapiserver.domain.map.dto.MapResponseDto;
import com.trackery.trackerybackapiserver.domain.map.entity.Sido;
import com.trackery.trackerybackapiserver.domain.map.entity.Sigungu;
import com.trackery.trackerybackapiserver.domain.map.mapper.MapMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.map.service
 * fileName       : MapService
 * author         : inari
 * date           : 25. 5. 16.
 * description    : 대한민국 행정구역 지도 데이터 처리를 담당하는 서비스 클래스입니다.
 *                  시도/시군구 조회, 좌표 기반 검색, GeoJSON 경계선 데이터 제공 등의 비즈니스 로직을 처리합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 16.        inari       최초 생성
 * 25. 5. 30.        inari       예외 처리 및 로깅 추가
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MapService {

	private final MapMapper mapMapper;

	/**
	 * 대한민국의 모든 시도 목록을 조회합니다.
	 *
	 * @return 시도 목록 (시도명 기준 오름차순 정렬)
	 * @apiNote 캐싱 적용 대상 메서드입니다. 시도 데이터는 변경이 거의 없으므로 캐싱을 고려하세요.
	 */
	public List<Sido> getAllSido() {
		log.debug("전체 시도 목록 조회");
		return mapMapper.findAllSido();
	}

	/**
	 * 특정 시도에 속한 모든 시군구 목록을 조회합니다.
	 *
	 * @param sidoId 조회할 시도의 ID
	 * @return 해당 시도의 시군구 목록 (시군구명 기준 오름차순 정렬)
	 * @apiNote 빈 목록이 반환될 수 있습니다 (예: 잘못된 시도 ID).
	 *          캐싱 적용 대상 메서드입니다.
	 */
	public List<Sigungu> getSigunguBySido(Long sidoId) {
		log.debug("시도 ID {}의 시군구 목록 조회", sidoId);
		List<Sigungu> sigunguList = mapMapper.findSigunguBySido(sidoId);

		if (sigunguList.isEmpty()) {
			log.warn("시도 ID {}에 해당하는 시군구가 없습니다.", sidoId);
		}

		return sigunguList;
	}

	/**
	 * 지리 좌표를 기반으로 해당 위치의 시군구 정보를 조회합니다.
	 *
	 * @param request 위도와 경도를 포함한 좌표 요청 DTO
	 * @return 해당 좌표가 속한 시군구 정보 (시도 정보 포함)
	 * @apiNote PostGIS의 ST_Contains 함수를 사용하여 좌표가 어느 시군구 경계 내에 있는지 판단합니다.
	 */
	public Sigungu getSigunguByCoordinate(CoordinateRequestDto request) {
		log.debug("좌표로 시군구 조회: latitude={}, longitude={}",
			request.getLatitude(), request.getLongitude());

		Sigungu result = mapMapper.findSigunguByCoordinate(request);

		if (result == null) {
			log.warn("좌표에 해당하는 시군구를 찾을 수 없음: {}", request);
		}

		return result;
	}

	/**
	 * 지리 좌표를 기반으로 해당 위치의 시군구 정보를 DTO로 반환합니다.
	 *
	 * @param request 위도와 경도를 포함한 좌표 요청 DTO
	 * @return 시도명, 시군구명, ID를 포함한 응답 DTO
	 * @throws ApiException 해당 좌표에 시군구가 없는 경우 (ErrorCode.NOT_FOUND_SIGUNGU)
	 * @apiNote 좌표가 한국 영토 밖이거나 해상인 경우 예외가 발생합니다.
	 */
	public MapResponseDto getSigunguByCoordinateDto(CoordinateRequestDto request) {
		log.debug("좌표로 시군구 조회 (DTO 반환): latitude={}, longitude={}",
			request.getLatitude(), request.getLongitude());

		Sigungu sigungu = mapMapper.findSigunguByCoordinate(request);

		if (sigungu == null) {
			log.error("좌표에 해당하는 시군구를 찾을 수 없음: {}", request);
			throw new ApiException(ErrorCode.NOT_FOUND_SIGUNGU);
		}

		// 정적 팩토리 메서드 사용
		return MapResponseDto.from(sigungu);
	}

	/**
	 * 특정 시도의 경계선을 GeoJSON 형식으로 조회합니다.
	 *
	 * @param sidoId 조회할 시도의 ID
	 * @return GeoJSON 형식의 시도 경계선 데이터
	 * @throws ApiException 존재하지 않는 시도 ID인 경우 (ErrorCode.NOT_FOUND_SIDO)
	 * @apiNote PostGIS의 ST_AsGeoJSON 함수를 사용하여 공간 데이터를 GeoJSON으로 변환합니다.
	 *          반환된 GeoJSON은 Leaflet, Mapbox 등 주요 지도 라이브러리에서 바로 사용 가능합니다.
	 */
	public String getSidoBorderAsGeoJson(Long sidoId) {
		log.debug("시도 ID {}의 경계선 GeoJSON 조회", sidoId);

		String geoJson = mapMapper.findSidoBorderAsGeoJson(sidoId);

		if (geoJson == null) {
			log.error("시도 ID {}를 찾을 수 없습니다.", sidoId);
			throw new ApiException(ErrorCode.NOT_FOUND_SIDO);
		}

		return geoJson;
	}

	/**
	 * 특정 시군구의 경계선을 GeoJSON 형식으로 조회합니다.
	 *
	 * @param sigunguId 조회할 시군구의 ID
	 * @return GeoJSON 형식의 시군구 경계선 데이터
	 * @throws ApiException 존재하지 않는 시군구 ID인 경우 (ErrorCode.NOT_FOUND_SIGUNGU)
	 * @apiNote PostGIS의 ST_AsGeoJSON 함수를 사용하여 공간 데이터를 GeoJSON으로 변환합니다.
	 *          대용량 경계선 데이터의 경우 단순화(simplification) 적용을 고려하세요.
	 */
	public String getSigunguBorderAsGeoJson(Long sigunguId) {
		log.debug("시군구 ID {}의 경계선 GeoJSON 조회", sigunguId);

		String geoJson = mapMapper.findSigunguBorderAsGeoJson(sigunguId);

		if (geoJson == null) {
			log.error("시군구 ID {}를 찾을 수 없습니다.", sigunguId);
			throw new ApiException(ErrorCode.NOT_FOUND_SIGUNGU);
		}

		return geoJson;
	}
}
