package com.trackery.trackerybackapiserver.domain.album.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumImageInsertRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageInsertResponseDto;
import com.trackery.trackerybackapiserver.domain.album.service.AlbumService;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.controller
 * fileName       : AlbumController
 * author         : durururuk
 * date           : 25. 5. 19.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 19.		durururuk		최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/albums")
public class AlbumController {
	private final AlbumService albumService;

	@PostMapping
	public ResponseEntity<ApiResponse<String>> createAlbum(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid AlbumCreateRequestDto albumCreateRequestDto) {

		albumService.insertAlbum(userDetails.getUserId(), albumCreateRequestDto);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	@PostMapping("/images")
	public ResponseEntity<ApiResponse<AlbumImageInsertResponseDto>> addImagesIntoAlbum(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid AlbumImageInsertRequestDto requestDto) {

		AlbumImageInsertResponseDto result = albumService.addImageIntoAlbum(userDetails.getUserId(),
			requestDto.getAlbumId(), requestDto.getImageIdList());

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}
}