package com.trackery.trackerybackapiserver.domain.location.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateRequestDto;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationNameResponseDto;
import com.trackery.trackerybackapiserver.domain.location.dto.MapResponseDto;
import com.trackery.trackerybackapiserver.domain.location.dto.SigunguResponseDto;
import com.trackery.trackerybackapiserver.domain.location.dto.UserStatsDto;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.controller
 * fileName       : LocationController
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 위치 서비스 관련 API를 담당하는 컨트롤러 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 * 25. 4. 15.		durururuk		좌표로 지역 찾아오는 기능 구현
 * 25. 4. 17.		durururuk		좌표로 이름을 받아오는 api url 수정
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 4. 21.		durururuk		이미지 업로드 DTO 작성
 * 25. 4. 21.		durururuk		개발 중 테스트용 코드 남아있던 것 삭제
 * 25. 4. 23.		durururuk		LocationController JavaDoc 주석 작성
 * 25. 4. 24.		durururuk		클래스 설명 주석 작성
 * 25. 6. 10.		Nari-Lee		location과 통합
 * 25. 6. 13.		Nari-Lee		시군구 조회 기능 추가
 * 25. 6. 16.		Nari-Lee		전국지도용 통계 추가
 * 25. 6. 16.		Nari-Lee		location 도메인과 image 도메인 리팩토링 및 지도 이미지 조회기능 추가
 * 25. 6. 16.		Nari-Lee		체크스타일 적용
 * 25. 6. 16.		Nari-Lee		state에서 시도 제거
 * 25. 7. 1.		durururuk		다른 서비스 클래스에서도 변경된 로직에 맞게끔 수정
 * 25. 7. 7.		Nari-Lee		이미지 업로드시 시도,시군구 태그 추가 기능 구현
 * 25. 7. 8.		Nari-Lee		이미지 업로드시 지역기반으로 자동으로 시도, 시군구 태그 생
 * 25. 7. 11.		Nari-Lee		기본 태그기능 로케이션에서 태그 도메인으로 이동
 */
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {
	private final LocationService locationService;
	private final ImageService imageService;

	/**
	 * 좌표로 시/도 + 시군구 주소명을 받을 수 있는 API입니다.
	 *
	 * @param coordinateDto 좌표 DTO : latitude(위도), longitude(경도)를 double 타입으로 받습니다.
	 * @return json의 data node에 주소명이 들어갑니다.
	 */
	@PostMapping("/name")
	public ResponseEntity<ApiResponse<LocationNameResponseDto>> getLocationName(
		@RequestBody @Valid CoordinateDto coordinateDto) {
		LocationNameResponseDto result = locationService.getLocationNameByCoord(coordinateDto);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	/**
	 * 대한민국의 모든 시도 목록을 조회합니다.
	 *
	 * @return 시도 목록을 포함한 API 응답
	 */
	@GetMapping("/sido")
	public ResponseEntity<ApiResponse<List<JusoSido>>> getAllSido() {
		List<JusoSido> sidoList = locationService.getAllSido();
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, sidoList));
	}

	/**
	 * 특정 시도에 속한 모든 시군구 목록을 조회합니다.
	 *
	 * @param sidoId 조회할 시도의 ID
	 * @return 시군구 목록을 포함한 API 응답
	 */
	@GetMapping("/sido/{sidoId}/sigungu")
	public ResponseEntity<ApiResponse<List<JusoSigungu>>> getSigunguBySido(@PathVariable Long sidoId) {
		List<JusoSigungu> sigunguList = locationService.getSigunguBySido(sidoId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, sigunguList));
	}

	/**
	 * 지리 좌표(위도, 경도)를 기반으로 해당 위치의 시군구 정보를 조회합니다.
	 *
	 * @param request 위도와 경도를 포함한 좌표 요청 DTO
	 * @return 해당 좌표가 속한 시군구 정보를 포함한 API 응답
	 */
	@PostMapping("/coordinate/sigungu")
	public ResponseEntity<ApiResponse<MapResponseDto>> getSigunguByCoordinate(
		@Valid @RequestBody CoordinateRequestDto request) {
		MapResponseDto response = locationService.getSigunguByCoordinateDto(request);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 특정 시도의 경계선을 GeoJSON 형식으로 조회합니다.
	 *
	 * @param sidoId 조회할 시도의 ID
	 * @return GeoJSON 형식의 시도 경계선 데이터를 포함한 API 응답
	 */
	@GetMapping("/sido/{sidoId}/border")
	public ResponseEntity<ApiResponse<String>> getSidoBorder(@PathVariable Long sidoId) {
		String border = locationService.getSidoBorderAsGeoJson(sidoId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, border));
	}

	/**
	 * 특정 시군구의 경계선을 GeoJSON 형식으로 조회합니다.
	 *
	 * @param sigunguId 조회할 시군구의 ID
	 * @return GeoJSON 형식의 시군구 경계선 데이터를 포함한 API 응답
	 */
	@GetMapping("/sigungu/{sigunguId}/border")
	public ResponseEntity<ApiResponse<String>> getSigunguBorder(@PathVariable Long sigunguId) {
		String border = locationService.getSigunguBorderAsGeoJson(sigunguId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, border));
	}

	/**
	 * 시군구 ID로 시군구 정보를 조회합니다.
	 *
	 * @param sigunguId 조회할 시군구의 ID
	 * @return 시군구 정보를 포함한 API 응답
	 */
	@GetMapping("/sigungu/{sigunguId}")
	public ResponseEntity<ApiResponse<SigunguResponseDto>> getSigungu(@PathVariable Long sigunguId) {
		JusoSigungu sigungu = locationService.getSigunguById(sigunguId);
		SigunguResponseDto response = SigunguResponseDto.from(sigungu);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 시군구 ID로 시도명과 시군구명을 분리하여 조회합니다.
	 *
	 * @param sigunguId 조회할 시군구의 ID
	 * @return 시도명과 시군구명을 분리한 위치 정보를 포함한 API 응답
	 */
	@GetMapping("/sigungu/{sigunguId}/location-info")
	public ResponseEntity<ApiResponse<LocationInfoDto>> getSigunguLocationInfo(@PathVariable Long sigunguId) {
		LocationInfoDto response = locationService.getSigunguLocationInfoById(sigunguId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 홈 화면용 사용자 통계 정보를 조회합니다.
	 *
	 * @param userDetails 인증된 사용자 정보
	 * @return 사용자 통계를 포함한 API 응답
	 */
	@GetMapping("/home/stats")
	public ResponseEntity<ApiResponse<UserStatsDto>> getHomeStats(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		UserStatsDto response = locationService.getUserStats(userDetails.getUserId());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 특정 시도에 등록된 사용자의 이미지 목록을 조회합니다.
	 *
	 * @param sidoId 조회할 시도의 ID
	 * @param userDetails 인증된 사용자 정보
	 * @return 해당 시도의 사용자 이미지 목록을 포함한 API 응답
	 */
	@GetMapping("/sido/{sidoId}/images")
	public ResponseEntity<ApiResponse<List<ImageThumbnailDto>>> getImagesBySido(
		@PathVariable Long sidoId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		List<ImageThumbnailDto> response = imageService.getImagesBySido(sidoId, userDetails.getUserId());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 특정 시군구에 등록된 사용자의 이미지 목록을 조회합니다.
	 *
	 * @param sigunguId 조회할 시군구의 ID
	 * @param userDetails 인증된 사용자 정보
	 * @return 해당 시군구의 사용자 이미지 목록을 포함한 API 응답
	 */
	@GetMapping("/sigungu/{sigunguId}/images")
	public ResponseEntity<ApiResponse<List<ImageThumbnailDto>>> getImagesBySigungu(
		@PathVariable Long sigunguId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		List<ImageThumbnailDto> response = imageService.getImagesBySigungu(sigunguId, userDetails.getUserId());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}
}
