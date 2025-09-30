package com.trackery.trackerybackapiserver.domain.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.tag.entity.Tag;
import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;
import com.trackery.trackerybackapiserver.domain.tag.mapper.TagMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.service
 * fileName       : AdminTagServiceTest
 * author         : inari
 * date           : 25. 9. 28.
 * description    : 관리자 태그 서비스 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 28.		inari		최초 생성
 * 25. 9. 29.		inari		테스트 코드 추가
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminTagService 테스트")
class AdminTagServiceTest {

	@Mock
	private TagMapper tagMapper;

	@InjectMocks
	private AdminTagService adminTagService;

	private TagMapper.TagStatistics testTagStatistics;
	private Tag testTag;

	@BeforeEach
	void setUp() {
		testTagStatistics = createTestTagStatistics();
		testTag = createTestTag();
	}

	@Nested
	@DisplayName("태그 통계 조회")
	class GetTagStatisticsTest {

		@Test
		@DisplayName("ADMIN이 태그 통계를 성공적으로 조회한다")
		void getTagStatisticsSuccess() {
			// Given
			Long adminRoleId = 3L;
			List<TagMapper.TagStatistics> statistics = List.of(testTagStatistics);
			when(tagMapper.findTagStatistics()).thenReturn(statistics);

			// When
			List<TagMapper.TagStatistics> result = adminTagService.getTagStatistics(adminRoleId);

			// Then
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(testTagStatistics.tagId(), result.get(0).tagId());
			assertEquals(testTagStatistics.tagName(), result.get(0).tagName());
			verify(tagMapper).findTagStatistics();
		}

		@Test
		@DisplayName("MANAGER가 태그 통계를 조회하려 하면 예외가 발생한다")
		void getTagStatisticsWithManagerRoleFails() {
			// Given
			Long managerRoleId = 2L;

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.getTagStatistics(managerRoleId));

			assertEquals(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES, exception.getErrorCode());
			verify(tagMapper, never()).findTagStatistics();
		}

		@Test
		@DisplayName("일반 사용자가 태그 통계를 조회하려 하면 예외가 발생한다")
		void getTagStatisticsWithUserRoleFails() {
			// Given
			Long userRoleId = 1L;

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.getTagStatistics(userRoleId));

			assertNotNull(exception.getMessage());
			assertEquals(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES, exception.getErrorCode());
			verify(tagMapper, never()).findTagStatistics();
		}
	}

	@Nested
	@DisplayName("시스템 태그 생성")
	class CreateSystemTagTest {

		@Test
		@DisplayName("ADMIN이 시스템 태그를 성공적으로 생성한다")
		void createSystemTagSuccess() {
			// Given
			Long adminRoleId = 3L;
			String tagName = "새로운 시스템 태그";
			TagType tagType = TagType.SIDO;

			when(tagMapper.findTagByNameAndType(eq(tagName), eq(tagType))).thenReturn(null);
			doNothing().when(tagMapper).insertTag(any(Tag.class));

			// When
			Tag result = adminTagService.createSystemTag(adminRoleId, tagName, tagType);

			// Then
			assertNotNull(result);
			assertEquals(tagName, result.getTagName());
			assertEquals(tagType, result.getTagType());
			assertEquals(0L, result.getTagUseCount());

			ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
			verify(tagMapper).insertTag(tagCaptor.capture());

			Tag capturedTag = tagCaptor.getValue();
			assertEquals(tagName, capturedTag.getTagName());
			assertEquals(tagType, capturedTag.getTagType());
			assertEquals(0L, capturedTag.getTagUseCount());
		}

		@Test
		@DisplayName("CUSTOM 태그 타입으로 생성하려 하면 예외가 발생한다")
		void createCustomTagFails() {
			// Given
			Long adminRoleId = 3L;
			String tagName = "커스텀 태그";
			TagType tagType = TagType.CUSTOM;

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.createSystemTag(adminRoleId, tagName, tagType));

			assertEquals(ErrorCode.BAD_REQUEST_INVALID_TAG_TYPE, exception.getErrorCode());
			verify(tagMapper, never()).insertTag(any(Tag.class));
		}

		@Test
		@DisplayName("이미 존재하는 태그명으로 생성하려 하면 예외가 발생한다")
		void createExistingTagFails() {
			// Given
			Long adminRoleId = 3L;
			String tagName = "기존 태그";
			TagType tagType = TagType.SIDO;

			when(tagMapper.findTagByNameAndType(eq(tagName), eq(tagType))).thenReturn(testTag);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.createSystemTag(adminRoleId, tagName, tagType));

			assertEquals(ErrorCode.BAD_REQUEST_TAG_ALREADY_EXISTS, exception.getErrorCode());
			verify(tagMapper, never()).insertTag(any(Tag.class));
		}

		@Test
		@DisplayName("MANAGER가 시스템 태그를 생성하려 하면 예외가 발생한다")
		void createSystemTagWithManagerRoleFails() {
			// Given
			Long managerRoleId = 2L;
			String tagName = "새로운 태그";
			TagType tagType = TagType.SIDO;

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.createSystemTag(managerRoleId, tagName, tagType));

			assertEquals(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("태그 삭제")
	class DeleteTagTest {

		@Test
		@DisplayName("사용되지 않는 태그를 성공적으로 삭제한다")
		void deleteUnusedTagSuccess() {
			// Given
			Long adminRoleId = 3L;
			Long tagId = 1L;

			when(tagMapper.findTagById(eq(tagId))).thenReturn(testTag);
			when(tagMapper.isTagInUse(eq(tagId))).thenReturn(false);
			when(tagMapper.deleteUnusedTag(eq(tagId))).thenReturn(1);

			// When
			assertDoesNotThrow(() -> adminTagService.deleteTag(adminRoleId, tagId));

			// Then
			verify(tagMapper).findTagById(tagId);
			verify(tagMapper).isTagInUse(tagId);
			verify(tagMapper).deleteUnusedTag(tagId);
		}

		@Test
		@DisplayName("존재하지 않는 태그를 삭제하려 하면 예외가 발생한다")
		void deleteNonexistentTagFails() {
			// Given
			Long adminRoleId = 3L;
			Long tagId = 999L;

			when(tagMapper.findTagById(eq(tagId))).thenReturn(null);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.deleteTag(adminRoleId, tagId));

			assertEquals(ErrorCode.NOT_FOUND_TAG, exception.getErrorCode());
			verify(tagMapper, never()).isTagInUse(anyLong());
			verify(tagMapper, never()).deleteUnusedTag(anyLong());
		}

		@Test
		@DisplayName("사용 중인 태그를 삭제하려 하면 예외가 발생한다")
		void deleteTagInUseFails() {
			// Given
			Long adminRoleId = 3L;
			Long tagId = 1L;

			when(tagMapper.findTagById(eq(tagId))).thenReturn(testTag);
			when(tagMapper.isTagInUse(eq(tagId))).thenReturn(true);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.deleteTag(adminRoleId, tagId));

			assertEquals(ErrorCode.BAD_REQUEST_TAG_IN_USE, exception.getErrorCode());
			verify(tagMapper, never()).deleteUnusedTag(anyLong());
		}

		@Test
		@DisplayName("삭제가 실제로 수행되지 않으면 예외가 발생한다")
		void deleteTagNotExecutedFails() {
			// Given
			Long adminRoleId = 3L;
			Long tagId = 1L;

			when(tagMapper.findTagById(eq(tagId))).thenReturn(testTag);
			when(tagMapper.isTagInUse(eq(tagId))).thenReturn(false);
			when(tagMapper.deleteUnusedTag(eq(tagId))).thenReturn(0); // 삭제되지 않음

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.deleteTag(adminRoleId, tagId));

			assertEquals(ErrorCode.BAD_REQUEST_TAG_IN_USE, exception.getErrorCode());
		}

		@Test
		@DisplayName("MANAGER가 태그를 삭제하려 하면 예외가 발생한다")
		void deleteTagWithManagerRoleFails() {
			// Given
			Long managerRoleId = 2L;
			Long tagId = 1L;

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.deleteTag(managerRoleId, tagId));

			assertEquals(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("태그 전체 통계 조회")
	class GetTagOverallStatisticsTest {

		@Test
		@DisplayName("ADMIN이 태그 전체 통계를 성공적으로 조회한다")
		void getTagOverallStatisticsSuccess() {
			// Given
			Long adminRoleId = 3L;

			when(tagMapper.countAllTags()).thenReturn(1000L);
			when(tagMapper.countTagsByType(TagType.CUSTOM)).thenReturn(700L);
			when(tagMapper.countTagsByType(TagType.SIDO)).thenReturn(17L);
			when(tagMapper.countTagsByType(TagType.SIGUNGU)).thenReturn(250L);
			when(tagMapper.countTagsByType(TagType.SEASON)).thenReturn(4L);
			when(tagMapper.countTagsByType(TagType.TIME)).thenReturn(24L);
			when(tagMapper.countTagsByType(TagType.WEATHER)).thenReturn(5L);

			// When
			AdminTagService.TagOverallStatistics result =
				adminTagService.getTagOverallStatistics(adminRoleId);

			// Then
			assertNotNull(result);
			assertEquals(1000L, result.totalTags());
			assertEquals(700L, result.customTags());
			assertEquals(300L, result.systemTags()); // 1000 - 700
			assertEquals(17L, result.sidoTags());
			assertEquals(250L, result.sigunguTags());
			assertEquals(4L, result.seasonTags());
			assertEquals(24L, result.timeTags());
			assertEquals(5L, result.weatherTags());

			verify(tagMapper).countAllTags();
			verify(tagMapper, times(6)).countTagsByType(any(TagType.class));
		}

		@Test
		@DisplayName("MANAGER가 태그 전체 통계를 조회하려 하면 예외가 발생한다")
		void getTagOverallStatisticsWithManagerRoleFails() {
			// Given
			Long managerRoleId = 2L;

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminTagService.getTagOverallStatistics(managerRoleId));

			assertEquals(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES, exception.getErrorCode());
			verify(tagMapper, never()).countAllTags();
		}

		@Test
		@DisplayName("태그가 없는 경우에도 정상적으로 통계를 조회한다")
		void getTagOverallStatisticsWithNoTags() {
			// Given
			Long adminRoleId = 3L;

			when(tagMapper.countAllTags()).thenReturn(0L);
			when(tagMapper.countTagsByType(any(TagType.class))).thenReturn(0L);

			// When
			AdminTagService.TagOverallStatistics result =
				adminTagService.getTagOverallStatistics(adminRoleId);

			// Then
			assertNotNull(result);
			assertEquals(0L, result.totalTags());
			assertEquals(0L, result.customTags());
			assertEquals(0L, result.systemTags());
			assertEquals(0L, result.sidoTags());
			assertEquals(0L, result.sigunguTags());
			assertEquals(0L, result.seasonTags());
			assertEquals(0L, result.timeTags());
			assertEquals(0L, result.weatherTags());
		}
	}

	private TagMapper.TagStatistics createTestTagStatistics() {
		return new TagMapper.TagStatistics(
			1L,
			"풍경",
			TagType.SIDO,
			150L,
			50L, // connectedImageCount
			LocalDateTime.now()
		);
	}

	private Tag createTestTag() {
		return Tag.builder()
			.tagName("풍경")
			.tagType(TagType.SIDO)
			.tagUseCount(0L)
			.createdAt(LocalDateTime.now())
			.build();
	}
}
