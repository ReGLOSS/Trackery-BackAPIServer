package com.trackery.trackerybackapiserver.domain.image.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.image.dto.LandingImagesResponse;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.controller
 * fileName       : LandingImagesController
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 랜딩 페이지에서 사용할 이미지 URL을 제공하는 컨트롤러입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.		inari		최초 생성
 * 25. 2. 14.		inari		랜덤이미지 가져오기 구현
 * 25. 2. 18.		inari		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 18.		inari		컨트롤러 ResponseEntity 반영
 * 25. 2. 24.		inari		자바독 주석 추가
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 4. 21.		durururuk		이미지 업로드 기능 구현
 * 25. 4. 21.		durururuk		더 이상 사용되지 않는 클래스 삭제
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class LandingImagesController {

	private final ImageService imageService;

	/**
	 * 랜딩 페이지에 표시할 공개 이미지 URL 목록을 반환합니다.
	 *
	 * @return 공개된 이미지 URL 목록, 없으면 빈 배열
	 */
	@GetMapping("/images")
	public ResponseEntity<ApiResponse<LandingImagesResponse>> getPublicImages() {
		LandingImagesResponse landingImagesResponse = new LandingImagesResponse(imageService.getPublicImageUrls());

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ApiResponse.success(SuccessCode.OK, landingImagesResponse));
	}
}
