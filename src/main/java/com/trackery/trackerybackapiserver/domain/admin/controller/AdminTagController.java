package com.trackery.trackerybackapiserver.domain.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.admin.dto.response.TagStatisticsResponseDto;
import com.trackery.trackerybackapiserver.domain.admin.service.AdminTagService;
import com.trackery.trackerybackapiserver.domain.admin.util.AdminAuthorizationUtil;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.entity.Tag;
import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;
import com.trackery.trackerybackapiserver.domain.tag.mapper.TagMapper;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.controller
 * fileName       : AdminTagController
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 관리자용 태그 관리 HTTP 요청을 처리하는 컨트롤러 클래스입니다.
 * 					태그 목록 조회, 시스템 태그 생성, 태그 삭제 기능을 제공합니다. (ADMIN 전용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 * 25. 9. 25.		inari		태그 목록 조회 API 구현
 * 25. 9. 26.		inari		시스템 태그 생성 API 구현
 * 25. 9. 27.		inari		태그 삭제 API 구현
 */
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

	private final AdminTagService adminTagService;

	/**
	 * 태그 목록 및 통계를 조회하는 API (ADMIN 전용)
	 * 모든 시스템 태그와 사용자 생성 태그를 타입별로 분류하여 표시합니다.
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @return 태그 통계 목록
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<TagStatisticsResponseDto>>> getTags(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		// ADMIN 권한 확인
		AdminAuthorizationUtil.requireAdmin();

		// 태그 통계 조회
		List<TagMapper.TagStatistics> tagStatistics = adminTagService.getTagStatistics(userDetails.getRoleId());

		// DTO 변환
		List<TagStatisticsResponseDto> responseList = tagStatistics.stream()
			.map(this::convertToTagStatisticsDto)
			.toList();

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, responseList));
	}

	/**
	 * 시스템 태그를 생성하는 API (ADMIN 전용)
	 * 새로운 시스템 태그를 생성하고 모든 사용자가 사용할 수 있도록 합니다.
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @param request 태그 생성 요청 정보
	 * @return 생성된 태그 정보
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<TagResponseDto>> createTag(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody TagCreateRequestDto request) {

		// ADMIN 권한 확인
		AdminAuthorizationUtil.requireAdmin();

		// 시스템 태그 생성
		TagType tagType = TagType.fromCode(request.getTagType());
		Tag createdTag = adminTagService.createSystemTag(
			userDetails.getRoleId(),
			request.getTagName(),
			tagType
		);

		// DTO 변환
		TagResponseDto responseDto = convertToTagResponseDto(createdTag);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.CREATED, responseDto));
	}

	/**
	 * 태그를 삭제하는 API (ADMIN 전용)
	 * 사용되지 않는 태그를 안전하게 제거하고 관련 연결을 정리합니다.
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @param tagId 삭제할 태그 ID
	 * @return 삭제 처리 결과
	 */
	@DeleteMapping("/{tagId}")
	public ResponseEntity<ApiResponse<Void>> deleteTag(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long tagId) {

		// ADMIN 권한 확인
		AdminAuthorizationUtil.requireAdmin();

		// 태그 삭제
		adminTagService.deleteTag(userDetails.getRoleId(), tagId);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * TagMapper.TagStatistics를 TagStatisticsResponseDto로 변환합니다.
	 *
	 * @param tagStatistics 태그 통계 정보
	 * @return 태그 통계 응답 DTO
	 */
	private TagStatisticsResponseDto convertToTagStatisticsDto(TagMapper.TagStatistics tagStatistics) {
		return TagStatisticsResponseDto.builder()
			.tagId(tagStatistics.tagId())
			.tagName(tagStatistics.tagName())
			.tagType(tagStatistics.tagType())
			.usageCount(tagStatistics.connectedImageCount())
			.createdAt(tagStatistics.createdAt())
			.build();
	}

	/**
	 * Tag 엔티티를 TagResponseDto로 변환합니다.
	 *
	 * @param tag 태그 엔티티
	 * @return 태그 응답 DTO
	 */
	private TagResponseDto convertToTagResponseDto(Tag tag) {
		return TagResponseDto.builder()
			.tagId(tag.getTagId())
			.tagName(tag.getTagName())
			.tagType(tag.getTagType())
			.tagUseCount(tag.getTagUseCount())
			.createdAt(tag.getCreatedAt())
			.build();
	}
}
