
package com.trackery.trackerybackapiserver.domain.album.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumImageEditRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumCreateResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumDetailedResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.MyAlbumResponseDto;
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
 * description    : 앨범 기능 컨트롤러
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

	/**
	 * 앨범 생성 API
	 * @param userDetails 유저 인증 정보
	 * @param albumCreateRequestDto 앨범 생성 정보 DTO
	 * @return 앨범 ID, 제목, 설명을 담은 DTO
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<AlbumCreateResponseDto>> createAlbum(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid AlbumCreateRequestDto albumCreateRequestDto) {

		AlbumCreateResponseDto result = albumService.insertAlbum(userDetails.getUserId(), albumCreateRequestDto);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	/**
	 * 앨범에 이미지 추가 API
	 * @param userDetails 유저 인증 정보
	 * @param requestDto 앨범 ID, 이미지 ID 리스트를 담은 DTO
	 * @return 결과 정보를 담은 DTO
	 */
	@PostMapping("/images")
	public ResponseEntity<ApiResponse<AlbumImageEditResponseDto>> addImagesIntoAlbum(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid AlbumImageEditRequestDto requestDto) {

		AlbumImageEditResponseDto result = albumService.addImageIntoAlbum(userDetails.getUserId(),
			requestDto.getAlbumId(), requestDto.getImageIdList());

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	/**
	 * 앨범 이미지 삭제
	 * @param userDetails 인증된 사용자 정보
	 * @param requestDto 앨범 ID와 삭제할 이미지 리스트를 담은 DTO
	 * @return 삭제된 이미지 정보, 실패한 이미지 정보를 담은 DTO
	 */
	@DeleteMapping("/images")
	public ResponseEntity<ApiResponse<AlbumImageEditResponseDto>> deleteImagesFromAlbum(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid AlbumImageEditRequestDto requestDto
	) {
		AlbumImageEditResponseDto result = albumService.deleteImageFromAlbum(userDetails.getUserId(),
			requestDto.getAlbumId(), requestDto.getImageIdList());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	/**
	 * 앨범 상세 정보 조회 API
	 * @param albumId 앨범 ID
	 * @param userDetails 인증된 유저 정보
	 * @return 앨범 정보, 앨범에 있는 이미지를 담은 상세 정보 DTO
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<AlbumDetailedResponseDto>> getAlbumDetailedInfo(@RequestParam Long albumId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		AlbumDetailedResponseDto result = albumService.getAlbumDetailedInfo(userDetails.getUserId(), albumId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	/**
	 * 앨범 정보 수정 API
	 * @param requestDto 수정할 앨범 정보 Request DTO
	 * @param userDetails 인증된 사용자 정보
	 * @return 성공 시 200, 그 외 상황에 맞는 에러코드
	 */
	@PatchMapping
	public ResponseEntity<ApiResponse<String>> updateAlbumInfo(@RequestBody AlbumUpdateRequestDto requestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		albumService.updateAlbumInfo(userDetails.getUserId(), requestDto);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 내 앨범 정보 간단 조회
	 * @param userDetails 인증된 사용자 정보
	 * @return 유저 ID, 앨범 갯수, 앨범 제목, 앨범 이미지 수, 공개 정보를 담은 DTO
	 */
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<MyAlbumResponseDto>> getMyAlbumSimpleInfo(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		MyAlbumResponseDto result = albumService.getMyAlbumSimpleInfo(userDetails.getUserId());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	/**
	 * 앨범을 삭제(비활성화)하는 API
	 * @param userDetails 인증된 사용자 정보
	 * @param albumId 앨범 ID
	 * @return 추가로 반환되는 데이터는 없습니다.
	 */
	@DeleteMapping
	public ResponseEntity<ApiResponse<String>> deleteAlbum(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam Long albumId) {
		albumService.deleteAlbum(userDetails.getUserId(), albumId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}
}
