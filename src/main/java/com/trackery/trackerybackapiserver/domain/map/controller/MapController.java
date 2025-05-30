package com.trackery.trackerybackapiserver.domain.map.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.map.dto.CoordinateRequestDto;
import com.trackery.trackerybackapiserver.domain.map.dto.MapResponseDto;
import com.trackery.trackerybackapiserver.domain.map.entity.Sido;
import com.trackery.trackerybackapiserver.domain.map.entity.Sigungu;
import com.trackery.trackerybackapiserver.domain.map.service.MapService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.map.controller
 * fileName       : MapController
 * author         : inari
 * date           : 25. 5. 16.
 * description    : 대한민국 행정구역(시도, 시군구) 지도 데이터를 제공하는 REST API 컨트롤러입니다.
 *                  시도별 시군구 조회, 좌표 기반 행정구역 검색, GeoJSON 형식의 경계선 데이터를 제공합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 16.        inari       최초 생성
 * 25. 5. 30.        inari       ApiResponse 형식 통일 및 검증 추가
 */
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

	private final MapService mapService;

	/**
	 * 대한민국의 모든 시도 목록을 조회합니다.
	 *
	 * @return 시도 목록을 포함한 API 응답
	 *         - 성공: 200 OK, 시도 목록 (시도명 기준 정렬)
	 *         - 실패: 500 Internal Server Error
	 * @apiNote 이 API는 지도 초기 로딩시 전국 시도를 표시하는데 사용됩니다.
	 */
	@GetMapping("/sido")
	public ResponseEntity<ApiResponse<List<Sido>>> getAllSido() {
		List<Sido> sidoList = mapService.getAllSido();
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, sidoList));
	}

	/**
	 * 특정 시도에 속한 모든 시군구 목록을 조회합니다.
	 *
	 * @param sidoId 조회할 시도의 ID
	 * @return 시군구 목록을 포함한 API 응답
	 *         - 성공: 200 OK, 해당 시도의 시군구 목록 (시군구명 기준 정렬)
	 *         - 실패: 404 Not Found (존재하지 않는 시도 ID)
	 * @apiNote 사용자가 특정 시도를 클릭했을 때 해당 시도의 시군구를 표시하는데 사용됩니다.
	 * @example GET /api/map/sido/2/sigungu (경기도의 시군구 조회)
	 */
	@GetMapping("/sido/{sidoId}/sigungu")
	public ResponseEntity<ApiResponse<List<Sigungu>>> getSigunguBySido(@PathVariable Long sidoId) {
		List<Sigungu> sigunguList = mapService.getSigunguBySido(sidoId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, sigunguList));
	}

	/**
	 * 지리 좌표(위도, 경도)를 기반으로 해당 위치의 시군구 정보를 조회합니다.
	 *
	 * @param request 위도와 경도를 포함한 좌표 요청 DTO
	 * @return 해당 좌표가 속한 시군구 정보를 포함한 API 응답
	 *         - 성공: 200 OK, 시도 및 시군구 정보
	 *         - 실패: 400 Bad Request (유효하지 않은 좌표)
	 *         - 실패: 404 Not Found (해당 좌표에 시군구 없음)
	 * @apiNote GPS 기반 현재 위치의 행정구역을 조회하는데 사용됩니다.
	 *          한국 영토 범위(위도 33-38.7, 경도 124.5-131.1) 내의 좌표만 유효합니다.
	 */
	@PostMapping("/coordinate/sigungu")
	public ResponseEntity<ApiResponse<MapResponseDto>> getSigunguByCoordinate(
		@Valid @RequestBody CoordinateRequestDto request) {
		MapResponseDto response = mapService.getSigunguByCoordinateDto(request);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 특정 시도의 경계선을 GeoJSON 형식으로 조회합니다.
	 *
	 * @param sidoId 조회할 시도의 ID
	 * @return GeoJSON 형식의 시도 경계선 데이터를 포함한 API 응답
	 *         - 성공: 200 OK, GeoJSON 문자열
	 *         - 실패: 404 Not Found (존재하지 않는 시도 ID)
	 * @apiNote 지도에서 시도 경계선을 그리는데 사용됩니다.
	 *          반환된 GeoJSON은 바로 지도 라이브러리에서 사용 가능합니다.
	 */
	@GetMapping("/sido/{sidoId}/border")
	public ResponseEntity<ApiResponse<String>> getSidoBorder(@PathVariable Long sidoId) {
		String border = mapService.getSidoBorderAsGeoJson(sidoId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, border));
	}

	/**
	 * 특정 시군구의 경계선을 GeoJSON 형식으로 조회합니다.
	 *
	 * @param sigunguId 조회할 시군구의 ID
	 * @return GeoJSON 형식의 시군구 경계선 데이터를 포함한 API 응답
	 *         - 성공: 200 OK, GeoJSON 문자열
	 *         - 실패: 404 Not Found (존재하지 않는 시군구 ID)
	 * @apiNote 지도에서 시군구 경계선을 그리는데 사용됩니다.
	 *          반환된 GeoJSON은 바로 지도 라이브러리에서 사용 가능합니다.
	 */
	@GetMapping("/sigungu/{sigunguId}/border")
	public ResponseEntity<ApiResponse<String>> getSigunguBorder(@PathVariable Long sigunguId) {
		String border = mapService.getSigunguBorderAsGeoJson(sigunguId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, border));
	}
}
