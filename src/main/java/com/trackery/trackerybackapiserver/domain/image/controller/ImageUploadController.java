package com.trackery.trackerybackapiserver.domain.image.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.image.dto.upload.ImageUploadDto;
import com.trackery.trackerybackapiserver.domain.image.service.ImageUploadService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.controller
 * fileName       : ImageUploadController
 * author         : durururuk
 * date           : 25. 4. 21.
 * description    : 이미지 업로드 관련 API를 담고 있는 RestController 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 21.		durururuk		최초 생성
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {
	private final ImageUploadService imageUploadService;

	/**
	 * S3 Object Put Presigned URL을 요청하는 API입니다.
	 * @param imageFileName Object Key가 될 이미지 파일명
	 * @return 성공 시 data node에 S3 PresignedPutUrl을 반환합니다.
	 */
	@GetMapping("/presigned-url/put")
	public ResponseEntity<ApiResponse<String>> requestPreSignedPutUrl(@RequestParam String imageFileName, @AuthenticationPrincipal CustomUserDetails userDetails) {
		String result = imageUploadService.getPresignedPutUrl(imageFileName, userDetails.getUserId());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	/**
	 * 이미지 메타데이터를 DB에 삽입 요청하는 API입니다.
	 * @param userDetails 업로드를 하는 유저 인증 정보
	 * @param imageUploadDto 이미지 메타데이터 DTO
	 * @return 성공 시 추가로 반환되는 데이터는 없습니다.
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> saveImageMetaData(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody ImageUploadDto imageUploadDto) {
		imageUploadService.saveImage(userDetails.getUserId(), imageUploadDto);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}
}
