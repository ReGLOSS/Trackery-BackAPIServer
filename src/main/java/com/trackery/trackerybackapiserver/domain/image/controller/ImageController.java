package com.trackery.trackerybackapiserver.domain.image.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.controller
 * fileName       : ImageController
 * author         : durururuk
 * date           : 25. 5. 15.
 * description    : 이미지 관련 기능 컨트롤러입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 15.		durururuk		최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {
	private final ImageService imageService;

	/**
	 * 이미지 단건 조회 API
	 * @param imageId 이미지 ID
	 * @return 이미지 정보 DTO
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<ImageDto>> getImageDto(@RequestParam Long imageId) {
		ImageDto imageDto = imageService.getImageByImageId(imageId);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, imageDto));
	}

	/**
	 * 현재 인증된 유저가 업로드한 이미지 다건 조회 API
	 * @deprecated 페이지네이션 사용 버전으로 프론트 수정 후 삭제 예정 {@link #getMyImagesV2(CustomUserDetails, int, int)}
	 * @param userDetails 인증 유저 정보
	 * @return 이미지 정보 DTO
	 */
	@Deprecated(forRemoval = true)
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<List<ImageDto>>> getMyImages(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		List<ImageDto> imageDtoList = imageService.getImageListByUserId(userDetails.getUserId());

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, imageDtoList));
	}

	/**
	 * 현재 인증된 유저가 업로드한 이미지 조회 API 페이지네이션 버전
	 * @param userDetails 인증 유저 정보
	 * @param pageNum 페이지 번호 (기본값 : 1)
	 * @param pageSize 페이지 크기 (기본값 : 10)
	 * @return 페이지네이션된 이미지 DTO 리스트
	 */
	@GetMapping("/v2/me")
	public ResponseEntity<ApiResponse<PageInfo<ImageDto>>> getMyImagesV2(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(defaultValue = "1") int pageNum,
		@RequestParam(defaultValue = "10") int pageSize
	) {
		PageInfo<ImageDto> imageDtoList = imageService.getImageListByUserIdV2(userDetails.getUserId(), pageNum,
			pageSize);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, imageDtoList));
	}
}
