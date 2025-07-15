package com.trackery.trackerybackapiserver.domain.tag.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagNameResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.entity.Tag;
import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;
import com.trackery.trackerybackapiserver.domain.tag.mapper.TagMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.service
 * fileName       : TagServiceTest
 * author         : inari
 * date           : 25. 7. 8.
 * description    : TagService 테스트코드
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 8.         inari       최초 생성
 * 25. 7. 11.        inari       최초 생성
 * 25. 7. 14.        inari       테스트 코드 수정
 * 25. 7. 15.        inari       코드 스멜 수정
 */
@ExtendWith(MockitoExtension.class)
class TagServiceTest {

	@InjectMocks
	private TagService tagService;

	@Mock
	private TagMapper tagMapper;

	@Mock
	private LocationService locationService;

	private Tag testTag;
	private TagCreateRequestDto createRequestDto;

	@BeforeEach
	void setUp() {
		testTag = Tag.builder()
			.tagName("테스트태그")
			.tagType(TagType.CUSTOM)
			.tagUseCount(0L)
			.createdAt(LocalDateTime.now())
			.build();
		// Set tagId using reflection since it's not included in the builder
		org.springframework.test.util.ReflectionTestUtils.setField(testTag, "tagId", 1L);

		createRequestDto = new TagCreateRequestDto("새태그", null);
	}

	@Nested
	@DisplayName("태그 생성 테스트")
	class CreateTagTest {

		@Test
		@DisplayName("새로운 태그 생성 성공")
		void createTag_Success() {
			// given
			when(tagMapper.findExistingTagByName("새태그")).thenReturn(null);
			doNothing().when(tagMapper).insertTag(any(Tag.class));

			// when
			TagResponseDto result = tagService.createTag(createRequestDto);

			// then
			assertNotNull(result);
			assertEquals("새태그", result.getTagName());
			assertEquals(TagType.CUSTOM, result.getTagType());
			assertEquals(0L, result.getTagUseCount());
			verify(tagMapper).findExistingTagByName("새태그");
			verify(tagMapper).insertTag(any(Tag.class));
		}

		@Test
		@DisplayName("기존 태그가 있으면 기존 태그 반환")
		void createTag_ExistingTag() {
			// given
			when(tagMapper.findExistingTagByName("새태그")).thenReturn(testTag);

			// when
			TagResponseDto result = tagService.createTag(createRequestDto);

			// then
			assertNotNull(result);
			assertEquals(testTag.getTagName(), result.getTagName());
			assertEquals(testTag.getTagType(), result.getTagType());
			verify(tagMapper).findExistingTagByName("새태그");
			verify(tagMapper, never()).insertTag(any(Tag.class));
		}
	}

	@Nested
	@DisplayName("태그 조회 테스트")
	class GetTagsTest {

		@Test
		@DisplayName("모든 태그 조회 성공")
		void getAllTags_Success() {
			// given
			List<Tag> tags = List.of(testTag);
			when(tagMapper.findAllTags()).thenReturn(tags);

			// when
			List<TagResponseDto> result = tagService.getAllTags();

			// then
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(testTag.getTagName(), result.get(0).getTagName());
			verify(tagMapper).findAllTags();
		}

		@Test
		@DisplayName("시스템 태그 조회 성공")
		void getSystemTags_Success() {
			// given
			List<Tag> systemTags = List.of(testTag);
			when(tagMapper.findSystemTags()).thenReturn(systemTags);

			// when
			List<TagResponseDto> result = tagService.getSystemTags();

			// then
			assertNotNull(result);
			assertEquals(1, result.size());
			verify(tagMapper).findSystemTags();
		}

		@Test
		@DisplayName("이미지별 태그 조회 성공")
		void getTagsByImageId_Success() {
			// given
			Long imageId = 1L;
			List<Tag> tags = List.of(testTag);
			when(tagMapper.findTagsByImageId(imageId)).thenReturn(tags);

			// when
			List<TagResponseDto> result = tagService.getTagsByImageId(imageId);

			// then
			assertNotNull(result);
			assertEquals(1, result.size());
			verify(tagMapper).findTagsByImageId(imageId);
		}
	}

	@Nested
	@DisplayName("이미지-태그 연결 테스트")
	class ImageTagConnectionTest {

		@Test
		@DisplayName("이미지에 태그 추가 성공")
		void addTagToImage_Success() {
			// given
			Long imageId = 1L;
			Long tagId = 1L;
			doNothing().when(tagMapper).insertImageTag(any());
			doNothing().when(tagMapper).incrementTagUseCount(tagId);

			// when
			tagService.addTagToImage(imageId, tagId);

			// then
			verify(tagMapper).insertImageTag(any());
			verify(tagMapper).incrementTagUseCount(tagId);
		}

		@Test
		@DisplayName("이미지에서 태그 제거 성공")
		void removeTagFromImage_Success() {
			// given
			Long imageId = 1L;
			Long tagId = 1L;
			doNothing().when(tagMapper).deleteImageTag(imageId, tagId);
			doNothing().when(tagMapper).decrementTagUseCount(tagId);

			// when
			tagService.removeTagFromImage(imageId, tagId);

			// then
			verify(tagMapper).deleteImageTag(imageId, tagId);
			verify(tagMapper).decrementTagUseCount(tagId);
		}
	}

	@Nested
	@DisplayName("위치 기반 태그 생성 테스트")
	class LocationTagTest {

		@Test
		@DisplayName("위치 기반 태그 생성 성공")
		void createLocationTagsForImage_Success() {
			// given
			double latitude = 37.5665;
			double longitude = 126.9780;
			JusoSido sido = new JusoSido();
			sido.setSidoName("서울특별시");
			JusoSigungu sigungu = new JusoSigungu();
			sigungu.setSigunguName("종로구");
			sigungu.setSido(sido);

			when(locationService.findSigunguByCoordinate(latitude, longitude)).thenReturn(sigungu);
			when(tagMapper.findTagByNameAndType("서울특별시", TagType.SIDO)).thenReturn(null);
			when(tagMapper.findTagByNameAndType("종로구", TagType.SIGUNGU)).thenReturn(null);
			doNothing().when(tagMapper).insertTag(any(Tag.class));

			// when
			List<Tag> result = tagService.createLocationTagsForImage(latitude, longitude);

			// then
			assertNotNull(result);
			assertEquals(2, result.size());
			verify(locationService).findSigunguByCoordinate(latitude, longitude);
			verify(tagMapper, times(2)).insertTag(any(Tag.class));
		}

		@Test
		@DisplayName("위치를 찾을 수 없으면 빈 리스트 반환")
		void createLocationTagsForImage_NoLocation() {
			// given
			double latitude = 0.0;
			double longitude = 0.0;
			when(locationService.findSigunguByCoordinate(latitude, longitude)).thenReturn(null);

			// when
			List<Tag> result = tagService.createLocationTagsForImage(latitude, longitude);

			// then
			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(locationService).findSigunguByCoordinate(latitude, longitude);
			verify(tagMapper, never()).insertTag(any(Tag.class));
		}

		@Test
		@DisplayName("이미지에 위치 태그 자동 연결 성공")
		void attachLocationTagsToImage_Success() {
			// given
			Long imageId = 1L;
			double latitude = 37.5665;
			double longitude = 126.9780;
			JusoSido sido = new JusoSido();
			sido.setSidoName("서울특별시");
			JusoSigungu sigungu = new JusoSigungu();
			sigungu.setSigunguName("종로구");
			sigungu.setSido(sido);

			when(locationService.findSigunguByCoordinate(latitude, longitude)).thenReturn(sigungu);
			when(tagMapper.findTagByNameAndType("서울특별시", TagType.SIDO)).thenReturn(null);
			when(tagMapper.findTagByNameAndType("종로구", TagType.SIGUNGU)).thenReturn(null);
			doNothing().when(tagMapper).insertTag(any(Tag.class));
			doNothing().when(tagMapper).insertImageTag(any());
			doNothing().when(tagMapper).incrementTagUseCount(any());

			// when
			tagService.attachLocationTagsToImage(imageId, latitude, longitude);

			// then
			verify(locationService).findSigunguByCoordinate(latitude, longitude);
			verify(tagMapper, times(2)).insertTag(any(Tag.class));
			verify(tagMapper, times(2)).insertImageTag(any());
			verify(tagMapper, times(2)).incrementTagUseCount(any());
		}

		@Test
		@DisplayName("지역 태그 문자열 리스트로 태그 연결")
		void attachRegionalTagsToImage_Success() {
			// given
			Long imageId = 1L;
			List<String> tags = List.of("서울", "한국", "도시");
			when(tagMapper.findExistingTagByName(any())).thenReturn(null);
			doNothing().when(tagMapper).insertTag(any(Tag.class));
			doNothing().when(tagMapper).insertImageTag(any());
			doNothing().when(tagMapper).incrementTagUseCount(any());

			// when
			tagService.attachRegionalTagsToImage(imageId, tags);

			// then
			verify(tagMapper, times(3)).findExistingTagByName(any());
			verify(tagMapper, times(3)).insertTag(any(Tag.class));
			verify(tagMapper, times(3)).insertImageTag(any());
			verify(tagMapper, times(3)).incrementTagUseCount(any());
		}

		@Test
		@DisplayName("빈 지역 태그 리스트 처리")
		void attachRegionalTagsToImage_EmptyList() {
			// given
			Long imageId = 1L;
			List<String> tags = List.of();

			// when
			tagService.attachRegionalTagsToImage(imageId, tags);

			// then
			verify(tagMapper, never()).findExistingTagByName(any());
			verify(tagMapper, never()).insertTag(any(Tag.class));
		}
	}

	@Nested
	@DisplayName("태그 삭제 테스트")
	class DeleteTagTest {

		@Test
		@DisplayName("관리자가 사용되지 않는 CUSTOM 태그 삭제 성공")
		void deleteTagByAdmin_Success() {
			// given
			Long tagId = 1L;
			Tag customTag = Tag.builder()
				.tagName("삭제할태그")
				.tagType(TagType.CUSTOM)
				.tagUseCount(0L)
				.build();

			when(tagMapper.findTagById(tagId)).thenReturn(customTag);
			when(tagMapper.isTagInUse(tagId)).thenReturn(false);
			when(tagMapper.deleteUnusedTag(tagId)).thenReturn(1);

			// when
			boolean result = tagService.deleteTagByAdmin(tagId);

			// then
			assertTrue(result);
			verify(tagMapper).findTagById(tagId);
			verify(tagMapper).isTagInUse(tagId);
			verify(tagMapper).deleteUnusedTag(tagId);
		}

		@Test
		@DisplayName("존재하지 않는 태그 삭제 시 예외 발생")
		void deleteTagByAdmin_TagNotFound() {
			// given
			Long tagId = 1L;
			when(tagMapper.findTagById(tagId)).thenReturn(null);

			// when & then
			ApiException exception = assertThrows(ApiException.class, 
				() -> tagService.deleteTagByAdmin(tagId));
			assertEquals(ErrorCode.NOT_FOUND_TAG, exception.getErrorCode());
		}

		@Test
		@DisplayName("시스템 태그 삭제 시 예외 발생")
		void deleteTagByAdmin_SystemTagCannotDelete() {
			// given
			Long tagId = 1L;
			Tag systemTag = Tag.builder()
				.tagName("시스템태그")
				.tagType(TagType.SIDO)
				.tagUseCount(0L)
				.build();

			when(tagMapper.findTagById(tagId)).thenReturn(systemTag);

			// when & then
			ApiException exception = assertThrows(ApiException.class, 
				() -> tagService.deleteTagByAdmin(tagId));
			assertEquals(ErrorCode.BAD_REQUEST_CANNOT_DELETE_SYSTEM_TAG, exception.getErrorCode());
		}

		@Test
		@DisplayName("사용 중인 태그 삭제 시 예외 발생")
		void deleteTagByAdmin_TagInUse() {
			// given
			Long tagId = 1L;
			Tag customTag = Tag.builder()
				.tagName("사용중태그")
				.tagType(TagType.CUSTOM)
				.tagUseCount(5L)
				.build();

			when(tagMapper.findTagById(tagId)).thenReturn(customTag);
			when(tagMapper.isTagInUse(tagId)).thenReturn(true);

			// when & then
			ApiException exception = assertThrows(ApiException.class, 
				() -> tagService.deleteTagByAdmin(tagId));
			assertEquals(ErrorCode.BAD_REQUEST_CANNOT_DELETE_TAG_IN_USE, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("태그 수정 테스트")
	class UpdateTagTest {

		@Test
		@DisplayName("이미지 태그 수정 성공")
		void updateImageTag_Success() {
			// given
			Long imageId = 1L;
			Long tagId = 1L;
			String newTagName = "새로운태그명";

			// Create a new tag with the new name for the final lookup
			Tag newTag = Tag.builder()
				.tagName(newTagName)
				.tagType(TagType.CUSTOM)
				.tagUseCount(0L)
				.createdAt(LocalDateTime.now())
				.build();
			org.springframework.test.util.ReflectionTestUtils.setField(newTag, "tagId", 2L);

			when(tagMapper.findTagById(tagId)).thenReturn(testTag);
			when(tagMapper.isImageTagConnected(imageId, tagId)).thenReturn(true);
			when(tagMapper.findExistingTagByName(newTagName)).thenReturn(null);
			
			// Mock insertTag to set the ID of the tag after insertion
			doAnswer(invocation -> {
				Tag tag = invocation.getArgument(0);
				org.springframework.test.util.ReflectionTestUtils.setField(tag, "tagId", 2L);
				return null;
			}).when(tagMapper).insertTag(any(Tag.class));
			
			doNothing().when(tagMapper).deleteImageTag(imageId, tagId);
			doNothing().when(tagMapper).insertImageTag(any());
			doNothing().when(tagMapper).decrementTagUseCount(tagId);
			doNothing().when(tagMapper).incrementTagUseCount(any());
			when(tagMapper.deleteUnusedTag(tagId)).thenReturn(1);
			when(tagMapper.findTagById(2L)).thenReturn(newTag);

			// when
			TagResponseDto result = tagService.updateImageTag(imageId, tagId, newTagName);

			// then
			assertNotNull(result);
			assertEquals(newTagName, result.getTagName());
			verify(tagMapper).deleteImageTag(imageId, tagId);
			verify(tagMapper).insertImageTag(any());
		}

		@Test
		@DisplayName("존재하지 않는 태그 수정 시 예외 발생")
		void updateImageTag_TagNotFound() {
			// given
			Long imageId = 1L;
			Long tagId = 1L;
			String newTagName = "새로운태그명";

			when(tagMapper.findTagById(tagId)).thenReturn(null);

			// when & then
			ApiException exception = assertThrows(ApiException.class, 
				() -> tagService.updateImageTag(imageId, tagId, newTagName));
			assertEquals(ErrorCode.NOT_FOUND_TAG, exception.getErrorCode());
		}

		@Test
		@DisplayName("이미지에 연결되지 않은 태그 수정 시 예외 발생")
		void updateImageTag_TagNotConnected() {
			// given
			Long imageId = 1L;
			Long tagId = 1L;
			String newTagName = "새로운태그명";

			when(tagMapper.findTagById(tagId)).thenReturn(testTag);
			when(tagMapper.isImageTagConnected(imageId, tagId)).thenReturn(false);

			// when & then
			ApiException exception = assertThrows(ApiException.class, 
				() -> tagService.updateImageTag(imageId, tagId, newTagName));
			assertEquals(ErrorCode.BAD_REQUEST_TAG_NOT_USED_BY_USER, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("계절 태그 생성 테스트")
	class SeasonTagTest {

		@Test
		@DisplayName("날짜 기반 계절 태그 생성 성공")
		void createSeasonTags_Success() {
			// given
			String dateTimeStr = "2024/7/15 14:30:25";
			when(tagMapper.findTagByNameAndType("여름", TagType.SEASON)).thenReturn(null);
			doNothing().when(tagMapper).insertTag(any(Tag.class));

			// when
			List<TagNameResponseDto> result = tagService.createSeasonTags(dateTimeStr);

			// then
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals("여름", result.get(0).getTagName());
			verify(tagMapper, times(1)).insertTag(any(Tag.class));
		}

		@Test
		@DisplayName("잘못된 날짜 형식으로 계절 태그 생성 시 예외 발생")
		void createSeasonTags_InvalidFormat() {
			// given
			String invalidDateTimeStr = "invalid-date-format";

			// when & then
			ApiException exception = assertThrows(ApiException.class, 
				() -> tagService.createSeasonTags(invalidDateTimeStr));
			assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
		}

		static Stream<Arguments> provideSeasonTestData() {
			return Stream.of(
				Arguments.of("2024/12/25 10:30:25", "겨울", "겨울 계절 태그 생성"),
				Arguments.of("2024/4/15 08:30:25", "봄", "봄 계절 태그 생성"),
				Arguments.of("2024/10/15 19:30:25", "가을", "가을 계절 태그 생성"),
				Arguments.of("2024/7/15 23:30:25", "여름", "밤 시간대에도 계절 태그만 생성"),
				Arguments.of("2024/7/15 12:30:25", "여름", "점심 시간대에도 계절 태그만 생성")
			);
		}

		@ParameterizedTest
		@MethodSource("provideSeasonTestData")
		@DisplayName("다양한 날짜와 시간에 따른 계절 태그 생성")
		void createSeasonTags_VariousSeasons(String dateTimeStr, String expectedSeason, String testDescription) {
			// given
			when(tagMapper.findTagByNameAndType(expectedSeason, TagType.SEASON)).thenReturn(null);
			doNothing().when(tagMapper).insertTag(any(Tag.class));

			// when
			List<TagNameResponseDto> result = tagService.createSeasonTags(dateTimeStr);

			// then
			assertEquals(1, result.size());
			assertEquals(expectedSeason, result.get(0).getTagName());
		}
	}

	@Nested
	@DisplayName("태그 이름 기반 연결 테스트")
	class TagNameConnectionTest {

		@Test
		@DisplayName("이미지에 태그 이름으로 태그 추가 성공")
		void addTagToImageByName_Success() {
			// given
			Long imageId = 1L;
			String tagName = "테스트태그";
			when(tagMapper.findExistingTagByName(tagName)).thenReturn(testTag);
			doNothing().when(tagMapper).insertImageTag(any());
			doNothing().when(tagMapper).incrementTagUseCount(testTag.getTagId());

			// when
			tagService.addTagToImageByName(imageId, tagName);

			// then
			verify(tagMapper).findExistingTagByName(tagName);
			verify(tagMapper).insertImageTag(any());
			verify(tagMapper).incrementTagUseCount(testTag.getTagId());
		}

		@Test
		@DisplayName("존재하지 않는 태그 이름으로 새 태그 생성 후 추가")
		void addTagToImageByName_CreateNewTag() {
			// given
			Long imageId = 1L;
			String tagName = "새로운태그";
			when(tagMapper.findExistingTagByName(tagName)).thenReturn(null);
			doNothing().when(tagMapper).insertTag(any(Tag.class));
			doNothing().when(tagMapper).insertImageTag(any());
			doNothing().when(tagMapper).incrementTagUseCount(any());

			// when
			tagService.addTagToImageByName(imageId, tagName);

			// then
			verify(tagMapper).findExistingTagByName(tagName);
			verify(tagMapper).insertTag(any(Tag.class));
			verify(tagMapper).insertImageTag(any());
			verify(tagMapper).incrementTagUseCount(any());
		}
	}

	@Nested
	@DisplayName("태그 정렬 테스트")
	class TagSortingTest {

		@Test
		@DisplayName("태그 타입에 따른 정렬 순서 확인")
		void getTagsForImageDisplay_SortedByTagType() {
			// given
			Long imageId = 1L;
			Tag sidoTag = Tag.builder()
				.tagName("서울특별시")
				.tagType(TagType.SIDO)
				.tagUseCount(1L)
				.createdAt(LocalDateTime.now())
				.build();
			org.springframework.test.util.ReflectionTestUtils.setField(sidoTag, "tagId", 1L);

			Tag sigunguTag = Tag.builder()
				.tagName("강남구")
				.tagType(TagType.SIGUNGU)
				.tagUseCount(1L)
				.createdAt(LocalDateTime.now())
				.build();
			org.springframework.test.util.ReflectionTestUtils.setField(sigunguTag, "tagId", 2L);

			Tag customTag = Tag.builder()
				.tagName("커스텀태그")
				.tagType(TagType.CUSTOM)
				.tagUseCount(1L)
				.createdAt(LocalDateTime.now())
				.build();
			org.springframework.test.util.ReflectionTestUtils.setField(customTag, "tagId", 3L);

			// 무순서로 리스트 생성
			List<Tag> unorderedTags = List.of(customTag, sidoTag, sigunguTag);
			when(tagMapper.findTagsByImageId(imageId)).thenReturn(unorderedTags);

			// when
			var result = tagService.getTagsForImageDisplay(imageId);

			// then
			assertNotNull(result);
			assertEquals(3, result.size());
			// 정렬 순서 확인: SIDO(1) -> SIGUNGU(2) -> CUSTOM(0)
			assertEquals("서울특별시", result.get(0).getTagName());
			assertEquals("강남구", result.get(1).getTagName());
			assertEquals("커스텀태그", result.get(2).getTagName());
		}
	}

	@Nested
	@DisplayName("태그 타입 핸들러 테스트")
	class TagTypeHandlerTest {

		@Test
		@DisplayName("TagType enum의 코드 변환 테스트")
		void tagTypeFromCode_Success() {
			// given & when & then
			assertEquals(TagType.CUSTOM, TagType.fromCode(0));
			assertEquals(TagType.SIDO, TagType.fromCode(1));
			assertEquals(TagType.SIGUNGU, TagType.fromCode(2));
			assertEquals(TagType.SEASON, TagType.fromCode(3));
			assertEquals(TagType.TIME, TagType.fromCode(4));
			assertEquals(TagType.WEATHER, TagType.fromCode(5));
		}

		@Test
		@DisplayName("잘못된 코드로 TagType 변환 시 예외 발생")
		void tagTypeFromCode_InvalidCode() {
			// given
			int invalidCode = 999;

			// when & then
			assertThrows(IllegalArgumentException.class, () -> TagType.fromCode(invalidCode));
		}
	}

	@Nested
	@DisplayName("모든 태그 연결 해제 테스트")
	class RemoveAllTagsTest {

		@Test
		@DisplayName("이미지의 모든 태그 연결 해제 성공")
		void removeAllTagsFromImage_Success() {
			// given
			Long imageId = 1L;
			doNothing().when(tagMapper).decrementTagUseCountByImageId(imageId);
			doNothing().when(tagMapper).deleteImageTagsByImageId(imageId);

			// when
			tagService.removeAllTagsFromImage(imageId);

			// then
			verify(tagMapper).decrementTagUseCountByImageId(imageId);
			verify(tagMapper).deleteImageTagsByImageId(imageId);
		}
	}
}
