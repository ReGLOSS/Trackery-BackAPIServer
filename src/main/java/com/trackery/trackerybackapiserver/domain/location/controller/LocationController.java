package com.trackery.trackerybackapiserver.domain.location.controller;

import java.util.List;

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

	@PostMapping("/name")
	public ResponseEntity<ApiResponse<String>> getLocationName(@RequestBody @Valid CoordinateDto coordinateDto) {
		String result = locationService.getLocationNameByCoord(coordinateDto);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	@PostMapping("/test")
	public ResponseEntity<ApiResponse<List<String>>> testPoint(@RequestBody @Valid CoordinateDto coordinateDto) {
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, locationService.test(coordinateDto)));
	}
}
