package com.trackery.trackerybackapiserver.domain.tag.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagDefaultRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagNameResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.service.TagService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.controller
 * fileName       : TagController
 * author         : inari
 * date           : 25. 7. 2.
 * description    : 태그 관련 API를 처리하는 컨트롤러 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 2.        inari       	최초 생성
 * 25. 7. 9.	 	inari		 	TagController로 일부 이동
 * 25. 7. 10.		inari			기본 태그 api 추가
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

	private final TagService tagService;

	/**
	 * 새로운 태그를 생성합니다.
	 * @param request 태그 생성 요청 데이터
	 * @return 생성된 태그 정보
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<TagResponseDto>> createTag(
			@RequestBody TagCreateRequestDto request) {

		TagResponseDto response = tagService.createTag(request);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 모든 태그 목록을 조회합니다.
	 * @return 모든 태그 목록
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<TagResponseDto>>> getAllTags() {
		List<TagResponseDto> response = tagService.getAllTags();
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 시스템 태그 목록을 조회합니다.
	 * @return 시스템 태그 목록
	 */
	@GetMapping("/system")
	public ResponseEntity<ApiResponse<List<TagResponseDto>>> getSystemTags() {
		List<TagResponseDto> response = tagService.getSystemTags();
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 태그를 안전하게 삭제합니다. (관리자 전용)
	 * - 사용되지 않는 태그만 삭제 가능
	 * - 시스템 태그 (CUSTOM 제외)는 삭제 불가
	 * @param tagId 삭제할 태그 ID
	 * @return 성공 응답
	 */
	@DeleteMapping("/admin/{tagId}")
	public ResponseEntity<ApiResponse<Void>> deleteTagByAdmin(@PathVariable Long tagId) {
		boolean deleted = tagService.deleteTagByAdmin(tagId);

		if (deleted) {
			return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
		} else {
			return ResponseEntity.badRequest()
					.body(ApiResponse.error(ErrorCode.BAD_REQUEST));
		}
	}

	/**
	 * 날짜 정보와 좌표 정보를 기반으로 기본 태그명 목록을 반환합니다.
	 * @param request 날짜 및 좌표 정보 요청 데이터
	 * @return 계절 태그명 목록 + 지역 태그명 목록 (좌표 제공시)
	 */
	@PostMapping("/default")
	public ResponseEntity<ApiResponse<List<TagNameResponseDto>>> getDefaultTags(
			@RequestBody @Valid TagDefaultRequestDto request) {

		List<TagNameResponseDto> response = tagService.createDefaultTags(
				request.getDate(), request.getCoordinate().latitude(), request.getCoordinate().longitude());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}
}
