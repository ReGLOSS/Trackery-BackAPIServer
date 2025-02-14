package com.trackery.trackerybackapiserver.domain.home.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.home.dto.LandingImagesResponse;
import com.trackery.trackerybackapiserver.domain.home.service.ImageService;

import lombok.RequiredArgsConstructor;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.home.controller
 * fileName       : LandingImagesController
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 랜딩 페이지에서 사용할 이미지 URL을 제공하는 컨트롤러입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class LandingImagesController {

	private final ImageService imageService;

	/**
	 * 랜딩 페이지에 표시할 공개 이미지 URL 목록을 반환합니다.
	 * @return 공개된 이미지 URL 목록
	 */
	@GetMapping
	public ApiResponse<LandingImagesResponse> getPublicImages() {
		return ApiResponse.success(
			SuccessCode.OK,
			new LandingImagesResponse(imageService.getPublicImageUrls())
		);
	}
}
