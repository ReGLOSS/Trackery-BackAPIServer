package com.trackery.trackerybackapiserver.domain.image.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageSearchByUserIdDto;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.service.TagService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import jakarta.validation.Valid;
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
 * 25. 5. 15.		durururuk		이미지 조회 기능 폴더 지정 오류로 안 되던 문제 수정
 * 25. 5. 15.		durururuk		서식 수정
 * 25. 5. 19.		durururuk		ImageController JavaDoc 주석 작성
 * 25. 6. 17.		durururuk		images/me 페이지네이션 버전 임시 작성
 * 25. 6. 17.		durururuk		getImageListByUserIdV2 서비스 테스트 코드 작성
 * 25. 6. 18.		durururuk		javadoc 주석 작성
 * 25. 6. 20.		Nari-Lee		이미지 수정 및 삭제기능 추가
 * 25. 6. 20.		Nari-Lee		pr 코멘트받은 내용 수정
 * 25. 6. 23.		durururuk		imageController에서도 deprecated된 API 삭제
 * 25. 7. 1.		durururuk		이미지 썸네일을 조회하는 메서드에서 썸네일 DTO를 반환하게 변경
 * 25. 7. 8.		durururuk		내 이미지 조회 시 조회 결과에서 제외될 앨범 ID 파라미터 추가
 * 25. 7. 8.		durururuk		개발 도중 흔적 제거
 * 25. 7. 8.		durururuk		이미지 단건 조회 api 엔드포인트 pathVariable 방식으로 수정
 * 25. 7. 9.		Nari-Lee		태그 컨트롤러에 있던 일부 매핑을 이미지로 이전함으로써 RESTful한 URL 구조로 변경
 * 25. 7. 10.		durururuk		PageSize 파라미터 검증 추가
 * 25. 7. 10.		Nari-Lee		이미지 단건 조회시 태그 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {
	private final ImageService imageService;
	private final TagService tagService;

	/**
	 * 원본 이미지 단건 조회 API
	 * @param imageId 이미지 ID
	 * @param userDetails 인증된 사용자 정보
	 * @return 이미지 정보 DTO
	 */
	@GetMapping("/{imageId}")
	public ResponseEntity<ApiResponse<ImageDto>> getImageDto(@PathVariable Long imageId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		ImageDto imageDto = imageService.getOriginalImageByImageId(userDetails.getUserId(), imageId);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, imageDto));
	}

	/**
	 * 현재 인증된 유저가 업로드한 이미지 조회 API 페이지네이션 버전
	 * @param userDetails 인증 유저 정보
	 * @param pageNum 페이지 번호 (기본값 : 1)
	 * @param pageSize 페이지 크기 (기본값 : 10)
	 * @return 페이지네이션된 이미지 DTO 리스트
	 */
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<PageInfo<ImageThumbnailDto>>> getMyImagesV2(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(defaultValue = "1") int pageNum,
		@RequestParam(defaultValue = "10") int pageSize,
		@RequestParam(required = false) Long excludeAlbumId
	) {
		pageSize = Math.max(1, Math.min(100, pageSize));

		ImageSearchByUserIdDto searchByUserIdDto = ImageSearchByUserIdDto.builder()
			.userId(userDetails.getUserId())
			.excludeAlbumId(excludeAlbumId)
			.pageNum(pageNum)
			.pageSize(pageSize)
			.build();

		PageInfo<ImageThumbnailDto> imageDtoList = imageService.getImageListByUserId(searchByUserIdDto);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, imageDtoList));
	}

	/**
	 * 이미지 메타데이터 수정 API
	 * @param imageId 수정할 이미지 ID
	 * @param updateRequest 수정할 메타데이터
	 * @param userDetails 인증된 사용자 정보
	 * @return 수정된 이미지 정보
	 */
	@PatchMapping("/{imageId}")
	public ResponseEntity<ApiResponse<ImageDto>> updateImageMetadata(
		@PathVariable Long imageId,
		@Valid @RequestBody ImageUpdateRequestDto updateRequest,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		ImageDto updatedImage = imageService.updateImageMetadata(imageId, userDetails.getUserId(), updateRequest);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, updatedImage));
	}

	/**
	 * 이미지 삭제 API (논리적 삭제)
	 * @param imageId 삭제할 이미지 ID
	 * @param userDetails 인증된 사용자 정보
	 * @return 삭제 성공 응답
	 */
	@DeleteMapping("/{imageId}")
	public ResponseEntity<ApiResponse<Void>> deleteImage(
		@PathVariable Long imageId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		imageService.deleteImage(imageId, userDetails.getUserId());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, null));
	}

	/**
	 * 특정 이미지에 연결된 태그 목록만을 조회합니다.
	 * @param imageId 이미지 ID
	 * @param userDetails 인증된 사용자 정보
	 * @return 이미지에 연결된 태그 목록
	 */
	@GetMapping("/{imageId}/tags")
	public ResponseEntity<ApiResponse<List<TagResponseDto>>> getTagsByImageId(
		@PathVariable Long imageId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		// 이미지 소유자 확인
		imageService.getOriginalImageByImageId(userDetails.getUserId(), imageId);
		List<TagResponseDto> response = tagService.getTagsByImageId(imageId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 이미지에 태그를 연결합니다.
	 * @param imageId 이미지 ID
	 * @param tagId 태그 ID
	 * @param userDetails 인증된 사용자 정보
	 * @return 성공 응답
	 */
	@PostMapping("/{imageId}/tags/{tagId}")
	public ResponseEntity<ApiResponse<Void>> addTagToImage(
		@PathVariable Long imageId,
		@PathVariable Long tagId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		// 이미지 소유자 확인
		imageService.getOriginalImageByImageId(userDetails.getUserId(), imageId);
		tagService.addTagToImage(imageId, tagId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 이미지에서 태그 연결을 제거합니다.
	 * @param imageId 이미지 ID
	 * @param tagId 태그 ID
	 * @param userDetails 인증된 사용자 정보
	 * @return 성공 응답
	 */
	@DeleteMapping("/{imageId}/tags/{tagId}")
	public ResponseEntity<ApiResponse<Void>> removeTagFromImage(
		@PathVariable Long imageId,
		@PathVariable Long tagId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		// 이미지 소유자 확인
		imageService.getOriginalImageByImageId(userDetails.getUserId(), imageId);
		tagService.removeTagFromImage(imageId, tagId);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 특정 이미지의 태그를 수정합니다.
	 * @param imageId 이미지 ID
	 * @param tagId 수정할 태그 ID
	 * @param request 태그 수정 요청 데이터
	 * @param userDetails 인증된 사용자 정보
	 * @return 수정된 태그 정보
	 */
	@PutMapping("/{imageId}/tags/{tagId}")
	public ResponseEntity<ApiResponse<TagResponseDto>> updateImageTag(
		@PathVariable Long imageId,
		@PathVariable Long tagId,
		@RequestBody TagUpdateRequestDto request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		// 이미지 소유자 확인
		imageService.getOriginalImageByImageId(userDetails.getUserId(), imageId);
		TagResponseDto response = tagService.updateImageTag(imageId, tagId, request.getNewTagName());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}
}
