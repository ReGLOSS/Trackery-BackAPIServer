package com.trackery.trackerybackapiserver.domain.tag.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagForImageResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagNameResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.entity.ImageTag;
import com.trackery.trackerybackapiserver.domain.tag.entity.Tag;
import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;
import com.trackery.trackerybackapiserver.domain.tag.mapper.TagMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.service
 * fileName       : TagService
 * author         : Nari-Lee
 * date           : 25. 7. 2.
 * description    : 태그 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 2.		Nari-Lee		최초 생성
 * 25. 7. 8.		Nari-Lee		태그 도메인 구현
 * 25. 7. 9.		Nari-Lee		regionalTags를 Tags로 수정 및 관련 메서드 및 변수 변경
 * 25. 7. 9.		Nari-Lee		체크스타일 수정 및 메서드 분리
 * 25. 7. 10.		Nari-Lee		이미지 단건 조회시 태그 추가
 * 25. 7. 10.		Nari-Lee		태그 생성시 계절만 사용
 * 25. 7. 11.		Nari-Lee		기본 태그 생성 기능 추가
 * 25. 7. 11.		Nari-Lee		기본 태그 생성시 좌표대신 시도, 시군구 이름 받게 설정
 * 25. 7. 11.		Nari-Lee		기존 매퍼에 존재하던 이미지 삭제시 태그 일괄 삭제 기능 적용하여 N+1 문제 해결
 * 25. 7. 11.		Nari-Lee		test코드 추가
 * 25. 7. 13.		Nari-Lee		이미지 수정시 태그 삭제 기능 구현
 * 25. 7. 14.		Nari-Lee		이미지 수정시 태그 추가 기능 구현
 * 25. 7. 14.		Nari-Lee		/api/tags/default에서 좌표 삭제후 /api/tags/season으로 변경
 * 25. 7. 14.		Nari-Lee		TagType enum에 추가하고 locationName 파라미터대신 sidoName, sigunguName으로 분리
 * 25. 7. 14.		Nari-Lee		TagType에 따른 태그 정렬 추가
 * 25. 7. 15.		Nari-Lee		ImageService에 있던 updateImageLocation, processTagRemoval, processTagAddition 각 도메인으로 이동
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

	private final TagMapper tagMapper;
	private final LocationService locationService;
	private static final String ASIA_SEOUL = "Asia/Seoul";

	/**
	 * 사용자가 생성한 태그를 저장합니다.
	 * 기존 시스템 태그가 있으면 그것을 우선 사용합니다.
	 * @param request 태그 생성 요청 데이터
	 * @return 생성되거나 기존 태그 정보
	 */
	@Transactional
	public TagResponseDto createTag(TagCreateRequestDto request) {
		// 먼저 같은 이름의 기존 태그가 있는지 확인 (시스템 태그 우선)
		Tag existingTag = tagMapper.findExistingTagByName(request.getTagName());
		if (existingTag != null) {
			return convertToResponseDto(existingTag);
		}

		// 새로 생성시 무조건 CUSTOM 타입으로만 생성
		Tag tag = Tag.builder()
			.tagName(request.getTagName())
			.tagType(TagType.CUSTOM)
			.tagUseCount(0L)
			.createdAt(LocalDateTime.now(ZoneId.of(ASIA_SEOUL)))
			.build();

		tagMapper.insertTag(tag);

		return convertToResponseDto(tag);
	}

	/**
	 * 모든 태그 목록을 조회합니다.
	 * @return 모든 태그 목록
	 */
	@Transactional(readOnly = true)
	public List<TagResponseDto> getAllTags() {
		List<Tag> allTags = tagMapper.findAllTags();

		return allTags.stream()
			.map(this::convertToResponseDto)
			.toList();
	}

	/**
	 * 시스템 태그 목록을 조회합니다.
	 * @return 시스템 태그 목록
	 */
	@Transactional(readOnly = true)
	public List<TagResponseDto> getSystemTags() {
		List<Tag> systemTags = tagMapper.findSystemTags();
		return systemTags.stream()
			.map(this::convertToResponseDto)
			.toList();
	}

	/**
	 * 특정 이미지에 연결된 태그 목록을 조회합니다.
	 * @param imageId 이미지 ID
	 * @return 이미지에 연결된 태그 목록
	 */
	@Transactional(readOnly = true)
	public List<TagResponseDto> getTagsByImageId(Long imageId) {
		List<Tag> tags = tagMapper.findTagsByImageId(imageId);
		return tags.stream()
			.map(this::convertToResponseDto)
			.toList();
	}

	/**
	 * 특정 이미지에 연결된 태그명만 조회합니다.
	 * @param imageId 이미지 ID
	 * @return 이미지에 연결된 태그명 목록
	 */
	@Transactional(readOnly = true)
	public List<TagNameResponseDto> getTagNamesByImageId(Long imageId) {
		List<Tag> tags = tagMapper.findTagsByImageId(imageId);
		return tags.stream()
			.map(tag -> TagNameResponseDto.builder()
				.tagName(tag.getTagName())
				.build())
			.toList();
	}

	/**
	 * 이미지 조회용 태그 목록을 반환합니다 (ID와 이름만 포함).
	 * 태그 타입(TagType)에 따라 정렬됩니다. (SIDO, SIGUNGU, SEASON, TIME, WEATHER, CUSTOM 순서)
	 *
	 * @param imageId 이미지 ID
	 * @return 이미지에 연결된 간소화된 태그 목록
	 */
	@Transactional(readOnly = true)
	public List<TagForImageResponseDto> getTagsForImageDisplay(Long imageId) {
		List<Tag> tags = tagMapper.findTagsByImageId(imageId);
		return tags.stream()
			.sorted((tag1, tag2) -> {
				// 태그 타입에 따른 사용자 정의 정렬 순서 정의
				int order1 = getTagTypeOrder(tag1.getTagType());
				int order2 = getTagTypeOrder(tag2.getTagType());
				return Integer.compare(order1, order2);
			})
			.map(tag -> TagForImageResponseDto.builder()
				.tagId(tag.getTagId())
				.tagName(tag.getTagName())
				.build())
			.toList();
	}

	/**
	 * 태그 타입에 따른 정렬 순서를 반환합니다.
	 * SIDO(1), SIGUNGU(2), SEASON(3), TIME(4), WEATHER(5) 순서로 우선순위를 가지며,
	 * CUSTOM(0)은 가장 마지막에 정렬됩니다.
	 *
	 * @param tagType 태그 타입
	 * @return 정렬 순서 (낮은 숫자가 우선)
	 */
	private int getTagTypeOrder(TagType tagType) {
		return switch (tagType) {
			case SIDO -> 1;
			case SIGUNGU -> 2;
			case SEASON -> 3;
			case TIME -> 4;
			case WEATHER -> 5;
			case CUSTOM -> 6; // CUSTOM (0)은 가장 마지막에 정렬
			default -> Integer.MAX_VALUE; // 정의되지 않은 타입은 마지막으로
		};
	}

	/**
	 * 이미지에 태그를 연결합니다.
	 * @param imageId 이미지 ID
	 * @param tagId 태그 ID
	 */
	@Transactional
	public void addTagToImage(Long imageId, Long tagId) {
		ImageTag imageTag = ImageTag.builder()
			.imageId(imageId)
			.tagId(tagId)
			.createdAt(LocalDateTime.now(ZoneId.of(ASIA_SEOUL)))
			.build();

		tagMapper.insertImageTag(imageTag);
		tagMapper.incrementTagUseCount(tagId);
	}

	/**
	 * 태그명으로 태그를 찾거나 생성한 후 이미지에 연결합니다.
	 * @param imageId 이미지 ID
	 * @param tagName 태그명
	 */
	@Transactional
	public void addTagToImageByName(Long imageId, String tagName) {
		Tag tag = findOrCreateTagByName(tagName);
		addTagToImage(imageId, tag.getTagId());
	}

	/**
	 * 이미지에서 태그 연결을 제거합니다.
	 * @param imageId 이미지 ID
	 * @param tagId 태그 ID
	 */
	@Transactional
	public void removeTagFromImage(Long imageId, Long tagId) {
		tagMapper.deleteImageTag(imageId, tagId);
		tagMapper.decrementTagUseCount(tagId);
	}

	/**
	 * 특정 이미지와 연결된 모든 태그를 제거합니다.
	 * @param imageId 이미지 ID
	 */
	@Transactional
	public void removeAllTagsFromImage(Long imageId) {
		tagMapper.decrementTagUseCountByImageId(imageId);
		tagMapper.deleteImageTagsByImageId(imageId);
		log.info("이미지 연결 태그 정리 완료 - imageId: {}", imageId);
	}

	/**
	 * 좌표를 기반으로 위치 태그를 생성합니다.
	 * @param latitude 위도
	 * @param longitude 경도
	 * @return 생성된 위치 태그 목록 (시도, 시군구)
	 */
	@Transactional
	public List<Tag> createLocationTagsForImage(double latitude, double longitude) {
		JusoSigungu sigungu = locationService.findSigunguByCoordinate(latitude, longitude);
		if (sigungu == null) {
			return List.of();
		}

		String sidoName = sigungu.getSido().getSidoName();
		String sigunguName = sigungu.getSigunguName();

		Tag sidoTag = findOrCreateSidoTag(sidoName);
		Tag sigunguTag = findOrCreateSigunguTag(sigunguName);

		return List.of(sidoTag, sigunguTag);
	}

	/**
	 * 이미지에 위치 기반 태그를 자동으로 연결합니다.
	 * @param imageId 이미지 ID
	 * @param latitude 위도
	 * @param longitude 경도
	 */
	@Transactional
	public void attachLocationTagsToImage(Long imageId, double latitude, double longitude) {
		List<Tag> locationTags = createLocationTagsForImage(latitude, longitude);

		for (Tag tag : locationTags) {
			try {
				addTagToImage(imageId, tag.getTagId());
			} catch (Exception e) {
				log.warn("Failed to attach location tag {} to image {}: {}",
						tag.getTagName(), imageId, e.getMessage());
			}
		}
	}

	/**
	 * 지역 태그 문자열 리스트를 이미지에 연결합니다.
	 * 시스템 태그가 존재하면 해당 타입으로, 없으면 CUSTOM 타입으로 생성합니다.
	 * @param imageId 이미지 ID
	 * @param tags 태그 문자열 리스트
	 */
	@Transactional
	public void attachRegionalTagsToImage(Long imageId, List<String> tags) {
		if (tags == null || tags.isEmpty()) {
			return;
		}

		for (String tagName : tags) {
			if (tagName != null && !tagName.trim().isEmpty()) {
				Tag tag = findOrCreateTagByName(tagName.trim());
				try {
					addTagToImage(imageId, tag.getTagId());
				} catch (Exception e) {
					log.warn("Failed to attach regional tag '{}' to image {}: {}",
							tagName, imageId, e.getMessage());
				}
			}
		}
	}

	/**
	 * 태그 이름으로 기존 시스템 태그를 찾거나 새로운 CUSTOM 태그를 생성합니다.
	 * @param tagName 태그 이름
	 * @return 찾았거나 생성된 태그
	 */
	private Tag findOrCreateTagByName(String tagName) {
		Tag existingTag = tagMapper.findExistingTagByName(tagName);
		if (existingTag != null) {
			return existingTag;
		}

		Tag newTag = Tag.builder()
			.tagName(tagName)
			.tagType(TagType.CUSTOM)
			.tagUseCount(0L)
			.createdAt(LocalDateTime.now(ZoneId.of(ASIA_SEOUL)))
			.build();

		tagMapper.insertTag(newTag);
		return newTag;
	}


	/**
	 * 시도 이름으로 기존 태그를 찾거나 새로 생성합니다.
	 * @param sidoName 시도 이름
	 * @return 찾았거나 생성된 시도 태그
	 */
	private Tag findOrCreateSidoTag(String sidoName) {
		Tag existingTag = tagMapper.findTagByNameAndType(sidoName, TagType.SIDO);

		if (existingTag != null) {
			return existingTag;
		}

		Tag newTag = Tag.builder()
			.tagName(sidoName)
			.tagType(TagType.SIDO)
			.tagUseCount(1L)
			.createdAt(LocalDateTime.now(ZoneId.of(ASIA_SEOUL)))
			.build();

		tagMapper.insertTag(newTag);
		return newTag;
	}

	/**
	 * 시군구 이름으로 기존 태그를 찾거나 새로 생성합니다.
	 * @param sigunguName 시군구 이름
	 * @return 찾았거나 생성된 시군구 태그
	 */
	private Tag findOrCreateSigunguTag(String sigunguName) {
		Tag existingTag = tagMapper.findTagByNameAndType(sigunguName, TagType.SIGUNGU);

		if (existingTag != null) {
			return existingTag;
		}

		Tag newTag = Tag.builder()
			.tagName(sigunguName)
			.tagType(TagType.SIGUNGU)
			.tagUseCount(1L)
			.createdAt(LocalDateTime.now(ZoneId.of(ASIA_SEOUL)))
			.build();

		tagMapper.insertTag(newTag);
		return newTag;
	}

	/**
	 * 태그를 안전하게 삭제합니다. (관리자 전용)
	 * - 사용되지 않는 태그만 삭제 가능
	 * - 시스템 태그 (CUSTOM 제외)는 삭제 불가
	 * @param tagId 삭제할 태그 ID
	 * @return 항상 true (사용자에게는 성공으로 표시)
	 * @throws ApiException 태그가 존재하지 않는 경우, 또는 삭제 불가 태그인 경우
	 */
	@Transactional
	public boolean deleteTagByAdmin(Long tagId) {
		// 태그 존재 여부 확인
		Tag tag = tagMapper.findTagById(tagId);
		if (tag == null) {
			throw new ApiException(ErrorCode.NOT_FOUND_TAG);
		}

		// 시스템 태그 (CUSTOM 제외) 삭제 금지
		if (!tag.getTagType().equals(TagType.CUSTOM)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_CANNOT_DELETE_SYSTEM_TAG);
		}

		// 태그 사용 여부 확인
		boolean isInUse = tagMapper.isTagInUse(tagId);
		if (isInUse || tag.getTagUseCount() > 0) {
			throw new ApiException(ErrorCode.BAD_REQUEST_CANNOT_DELETE_TAG_IN_USE);
		}

		// 안전한 삭제 수행
		int deletedRows = tagMapper.deleteUnusedTag(tagId);
		if (deletedRows > 0) {
			log.info("Admin successfully deleted unused tag {} ({})", tag.getTagName(), tagId);
		}

		return deletedRows > 0;
	}

	/**
	 * 특정 이미지의 태그를 수정합니다.
	 * @param imageId 이미지 ID
	 * @param tagId 수정할 태그 ID
	 * @param newTagName 새로운 태그명
	 * @return 수정된 태그 정보
	 */
	@Transactional
	public TagResponseDto updateImageTag(Long imageId, Long tagId, String newTagName) {
		// 기존 태그 존재 여부 확인
		Tag oldTag = tagMapper.findTagById(tagId);
		if (oldTag == null) {
			throw new ApiException(ErrorCode.NOT_FOUND_TAG);
		}

		// 해당 이미지에 해당 태그가 연결되어 있는지 확인
		boolean isTagConnected = tagMapper.isImageTagConnected(imageId, tagId);
		if (!isTagConnected) {
			throw new ApiException(ErrorCode.BAD_REQUEST_TAG_NOT_USED_BY_USER);
		}

		// 새 태그명으로 기존 태그가 있는지 확인 (시스템 태그 우선)
		Tag existingNewTag = tagMapper.findExistingTagByName(newTagName);
		Tag targetTag;

		if (existingNewTag != null) {
			// 기존에 같은 이름의 태그가 있으면 그것을 사용
			targetTag = existingNewTag;
		} else {
			// 새로운 태그 생성 (CUSTOM 타입)
			targetTag = Tag.builder()
				.tagName(newTagName)
				.tagType(TagType.CUSTOM)
				.tagUseCount(0L)
				.createdAt(LocalDateTime.now(ZoneId.of(ASIA_SEOUL)))
				.build();
			tagMapper.insertTag(targetTag);
		}

		// 기존 태그와 새 태그가 같으면 변경할 필요 없음
		if (oldTag.getTagId().equals(targetTag.getTagId())) {
			return convertToResponseDto(targetTag);
		}

		// 기존 이미지-태그 관계 삭제 후 새 관계 생성
		tagMapper.deleteImageTag(imageId, tagId);
		ImageTag newImageTag = ImageTag.builder()
			.imageId(imageId)
			.tagId(targetTag.getTagId())
			.createdAt(LocalDateTime.now(ZoneId.of(ASIA_SEOUL)))
			.build();
		tagMapper.insertImageTag(newImageTag);

		// 태그 사용횟수 업데이트
		tagMapper.decrementTagUseCount(tagId);
		tagMapper.incrementTagUseCount(targetTag.getTagId());

		// 기존 태그가 더 이상 사용되지 않으면 삭제 (CUSTOM 타입만)
		Tag updatedOldTag = tagMapper.findTagById(tagId);
		if (updatedOldTag != null && updatedOldTag.getTagUseCount() == 0
			&& updatedOldTag.getTagType().equals(TagType.CUSTOM)) {
			tagMapper.deleteUnusedTag(tagId);
		}

		// 업데이트된 새 태그 정보 반환
		Tag finalTag = tagMapper.findTagById(targetTag.getTagId());
		return convertToResponseDto(finalTag);
	}

	/**
	 * 날짜 문자열을 기반으로 계절 태그 목록을 생성합니다.
	 * @param dateStr 날짜 문자열 (예: "2024 / 1 / 15")
	 * @return 생성된 태그명 목록 (계절 태그)
	 */
	@Transactional
	public List<TagNameResponseDto> createSeasonTags(String dateStr) {
		List<TagNameResponseDto> tags = new ArrayList<>();

		// 날짜 기반 계절 태그 생성
		LocalDate date = parseDate(dateStr);
		String seasonTag = getSeasonTag(date);
		findOrCreateSeasonTag(seasonTag);
		tags.add(TagNameResponseDto.builder().tagName(seasonTag).build());

		return tags;
	}


	/**
	 * 날짜 문자열을 LocalDate로 파싱합니다.
	 * @param dateStr 날짜 문자열 (예: "2024 / 1 / 15")
	 * @return 파싱된 LocalDate
	 */
	private LocalDate parseDate(String dateStr) {
		try {
			String normalized = normalizeDateString(dateStr);
			String datePart = normalized.split(" ")[0];
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
			return LocalDate.parse(datePart, formatter);
		} catch (Exception e) {
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}
	}

	/**
	 * 날짜 문자열의 공백과 슬래시를 정규화합니다.
	 * ReDoS 취약점을 방지하기 위해 복잡한 정규식 대신 단순한 문자열 치환을 사용합니다.
	 * @param dateStr 원본 날짜 문자열
	 * @return 정규화된 날짜 문자열
	 */
	private String normalizeDateString(String dateStr) {
		String result = dateStr.trim();
		result = result.replace(" /", "/");
		result = result.replace("/ ", "/");
		return result;
	}

	/**
	 * 날짜를 기반으로 계절 태그를 결정합니다.
	 * @param date 날짜
	 * @return 계절 태그명
	 */
	private String getSeasonTag(LocalDate date) {
		int month = date.getMonthValue();
		if (month == 12 || month <= 2) {
			return "겨울";
		} else if (month <= 5) {
			return "봄";
		} else if (month <= 8) {
			return "여름";
		} else {
			return "가을";
		}
	}

	/**
	 * 계절 태그를 찾거나 새로 생성합니다.
	 * @param tagName 태그명
	 */
	private void findOrCreateSeasonTag(String tagName) {
		Tag existingTag = tagMapper.findTagByNameAndType(tagName, TagType.SEASON);
		if (existingTag != null) {
			return;
		}
		Tag newTag = Tag.builder()
			.tagName(tagName)
			.tagType(TagType.SEASON)
			.tagUseCount(0L)
			.createdAt(LocalDateTime.now(ZoneId.of(ASIA_SEOUL)))
			.build();
		tagMapper.insertTag(newTag);
	}

	/**
	 * 이미지 수정 요청에서 삭제할 태그들을 처리합니다.
	 * @param imageId 이미지 ID
	 * @param updateRequest 수정 요청 데이터
	 */
	@Transactional
	public void processTagRemoval(Long imageId, ImageUpdateRequestDto updateRequest) {
		if (updateRequest.tagsToRemove() == null || updateRequest.tagsToRemove().isEmpty()) {
			return;
		}

		for (Long tagId : updateRequest.tagsToRemove()) {
			try {
				removeTagFromImage(imageId, tagId);
				log.info("이미지에서 태그 삭제 완료 - imageId: {}, tagId: {}", imageId, tagId);
			} catch (Exception e) {
				log.warn("이미지에서 태그 삭제 실패 - imageId: {}, tagId: {}, 오류: {}",
					imageId, tagId, e.getMessage());
			}
		}
	}

	/**
	 * 이미지 수정 요청에서 추가할 태그들을 처리합니다.
	 * @param imageId 이미지 ID
	 * @param updateRequest 수정 요청 데이터
	 */
	@Transactional
	public void processTagAddition(Long imageId, ImageUpdateRequestDto updateRequest) {
		if (updateRequest.tagsToAdd() == null || updateRequest.tagsToAdd().isEmpty()) {
			return;
		}

		for (String tagName : updateRequest.tagsToAdd()) {
			if (tagName != null && !tagName.trim().isEmpty()) {
				try {
					addTagToImageByName(imageId, tagName.trim());
					log.info("이미지에 태그 추가 완료 - imageId: {}, tagName: {}", imageId, tagName.trim());
				} catch (Exception e) {
					log.warn("이미지에 태그 추가 실패 - imageId: {}, tagName: {}, 오류: {}",
						imageId, tagName.trim(), e.getMessage());
				}
			}
		}
	}

	/**
	 * Tag 엔티티를 TagResponseDto로 변환합니다.
	 * @param tag 태그 엔티티
	 * @return 태그 응답 DTO
	 */
	private TagResponseDto convertToResponseDto(Tag tag) {
		return TagResponseDto.builder()
			.tagId(tag.getTagId())
			.tagName(tag.getTagName())
			.tagType(tag.getTagType())
			.tagUseCount(tag.getTagUseCount())
			.createdAt(tag.getCreatedAt())
			.build();
	}
}
