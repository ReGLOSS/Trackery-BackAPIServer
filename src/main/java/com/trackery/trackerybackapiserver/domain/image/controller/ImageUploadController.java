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
 * description    : 
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

	@GetMapping("/presigned-url/put")
	public ResponseEntity<ApiResponse<String>> requestPreSignedPutUrl(@RequestParam String imageFileName) {
		String result = imageUploadService.getPresignedPutUrl(imageFileName);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<String>> saveImageMetaData(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ImageUploadDto imageUploadDto) {
		imageUploadService.saveImage(userDetails.getUserId(), imageUploadDto);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}
}
