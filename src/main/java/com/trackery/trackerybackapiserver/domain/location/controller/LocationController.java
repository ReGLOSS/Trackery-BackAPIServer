package com.trackery.trackerybackapiserver.domain.location.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.controller
 * fileName       : LocationController
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 */
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {
	private final LocationService locationService;

	/**
	 * 좌표로 시/도 + 시군구 주소명을 받을 수 있는 API입니다.
	 *
	 * @param coordinateDto 좌표 DTO : latitude(위도), longitude(경도)를 double 타입으로 받습니다.
	 * @return json의 data node에 주소명이 들어갑니다.
	 */
	@PostMapping("/name")
	public ResponseEntity<ApiResponse<String>> getLocationName(@RequestBody @Valid CoordinateDto coordinateDto) {
		String result = locationService.getLocationNameByCoord(coordinateDto);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}
}
