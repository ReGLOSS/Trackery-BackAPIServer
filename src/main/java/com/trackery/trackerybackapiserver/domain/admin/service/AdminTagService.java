package com.trackery.trackerybackapiserver.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.tag.entity.Tag;
import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;
import com.trackery.trackerybackapiserver.domain.tag.mapper.TagMapper;
import com.trackery.trackerybackapiserver.domain.user.enums.UserRole;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.service
 * fileName       : AdminTagService
 * author         : inari
 * date           : 25. 9. 27.
 * description    : 관리자용 태그 관리 비즈니스 로직을 처리하는 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 27.		inari		최초 생성
 * 25. 9. 30.		inari		UserRole enum 통합
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTagService {

	private final TagMapper tagMapper;

	/**
	 * 모든 태그 목록과 통계를 조회합니다 (ADMIN 전용)
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @return 태그 통계 목록
	 * @throws ApiException ADMIN 권한이 없는 경우
	 */
	public List<TagMapper.TagStatistics> getTagStatistics(Long adminRoleId) {
		// ADMIN 권한 확인
		UserRole adminRole = UserRole.fromRoleId(adminRoleId);
		if (!adminRole.isAdmin()) {
			throw new ApiException(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES);
		}

		return tagMapper.findTagStatistics();
	}

	/**
	 * 시스템 태그를 생성합니다 (ADMIN 전용)
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @param tagName 태그명
	 * @param tagType 태그 타입
	 * @throws ApiException ADMIN 권한이 없거나 이미 존재하는 태그인 경우
	 */
	@Transactional
	public Tag createSystemTag(Long adminRoleId, String tagName, TagType tagType) {
		// ADMIN 권한 확인
		UserRole adminRole = UserRole.fromRoleId(adminRoleId);
		if (!adminRole.isAdmin()) {
			throw new ApiException(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES);
		}

		// 사용자 정의 태그는 생성할 수 없음
		if (tagType == TagType.CUSTOM) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_TAG_TYPE);
		}

		// 이미 존재하는 태그인지 확인
		Tag existingTag = tagMapper.findTagByNameAndType(tagName, tagType);
		if (existingTag != null) {
			throw new ApiException(ErrorCode.BAD_REQUEST_TAG_ALREADY_EXISTS);
		}

		// 시스템 태그 생성
		Tag systemTag = Tag.builder()
			.tagName(tagName)
			.tagType(tagType)
			.tagUseCount(0L)
			.build();

		tagMapper.insertTag(systemTag);
		return systemTag;
	}

	/**
	 * 태그를 삭제합니다 (ADMIN 전용)
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @param tagId 삭제할 태그 ID
	 * @throws ApiException ADMIN 권한이 없거나 태그가 사용 중인 경우
	 */
	@Transactional
	public void deleteTag(Long adminRoleId, Long tagId) {
		// ADMIN 권한 확인
		UserRole adminRole = UserRole.fromRoleId(adminRoleId);
		if (!adminRole.isAdmin()) {
			throw new ApiException(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES);
		}

		// 태그 존재 여부 확인
		Tag tag = tagMapper.findTagById(tagId);
		if (tag == null) {
			throw new ApiException(ErrorCode.NOT_FOUND_TAG);
		}

		// 태그가 사용 중인지 확인
		if (tagMapper.isTagInUse(tagId)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_TAG_IN_USE);
		}

		// 사용되지 않는 태그만 삭제
		int deletedRows = tagMapper.deleteUnusedTag(tagId);
		if (deletedRows == 0) {
			throw new ApiException(ErrorCode.BAD_REQUEST_TAG_IN_USE);
		}
	}

	/**
	 * 태그 전체 통계를 조회합니다 (ADMIN 전용)
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @return 태그 통계 정보
	 * @throws ApiException ADMIN 권한이 없는 경우
	 */
	public TagOverallStatistics getTagOverallStatistics(Long adminRoleId) {
		// ADMIN 권한 확인
		UserRole adminRole = UserRole.fromRoleId(adminRoleId);
		if (!adminRole.isAdmin()) {
			throw new ApiException(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES);
		}

		Long totalTags = tagMapper.countAllTags();
		Long customTags = tagMapper.countTagsByType(TagType.CUSTOM);
		Long systemTags = totalTags - customTags;
		Long sidoTags = tagMapper.countTagsByType(TagType.SIDO);
		Long sigunguTags = tagMapper.countTagsByType(TagType.SIGUNGU);
		Long seasonTags = tagMapper.countTagsByType(TagType.SEASON);
		Long timeTags = tagMapper.countTagsByType(TagType.TIME);
		Long weatherTags = tagMapper.countTagsByType(TagType.WEATHER);

		return new TagOverallStatistics(
			totalTags,
			customTags,
			systemTags,
			sidoTags,
			sigunguTags,
			seasonTags,
			timeTags,
			weatherTags
		);
	}

	/**
	 * 태그 전체 통계 정보를 담는 레코드
	 */
	public record TagOverallStatistics(
		Long totalTags,
		Long customTags,
		Long systemTags,
		Long sidoTags,
		Long sigunguTags,
		Long seasonTags,
		Long timeTags,
		Long weatherTags
	) { }
}
