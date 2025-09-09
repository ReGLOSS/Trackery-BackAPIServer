package com.trackery.trackerybackapiserver.domain.image.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PageUtil;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageSidoCoverageResponseDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageSigunguCoverageResponseDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageInfoForThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageSearchByUserIdDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.event.ImageDeleteEvent;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.location.service.LocationUtil;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagForImageResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.service.TagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.service
 * fileName       : ImageService
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 이미지 관련 비즈니스 로직을 처리하는 서비스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.		durururuk		최초 생성
 * 25. 2. 14.		inari		랜덤이미지 가져오기 구현
 * 25. 2. 14.		inari		매퍼 오타 수정 및 DB연결 테스트
 * 25. 2. 18.		inari		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 18.		inari		ImageService 트랜잭션 제거
 * 25. 2. 19.		inari		이미지가 비었을시 에러코드로 변경
 * 25. 2. 19.		inari		주석 추가
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 4. 21.		durururuk		이미지 업로드 DTO 작성
 * 25. 4. 21.		durururuk		이미지 업로드 기능 구현
 * 25. 5. 15.		durururuk		이미지 조회 기능 구현
 * 25. 5. 15.		durururuk		javadoc 주석 추가
 * 25. 6. 16.		inari		location 도메인과 image 도메인 리팩토링 및 지도 이미지 조회기능 추가
 * 25. 6. 16.		durururuk		앨범 API 문서 설명 작성
 * 25. 6. 16.		inari		체크스타일 적용
 * 25. 6. 17.		durururuk		getImageListByUserIdV2 서비스 테스트 코드 작성
 * 25. 6. 20.		inari		이미지 수정 및 삭제기능 추가
 * 25. 6. 20.		inari		pr 코멘트받은 내용 수정
 * 25. 6. 20.		inari		ImageService 500에러 최소화 및 adoc수정
 * 25. 6. 22.		inari		이미지 메타데이터 수정시 좌표 인서트가 아닌 업데이트로 변경
 * 25. 6. 23.		durururuk		앨범 목록 조회 시 썸네일도 함께 조회할 수 있도록 수정
 * 25. 6. 23.		durururuk		기존 앨범 정보 조회에서 이미지와 앨범 정보를 전부 한 번에 조회하던 것을 분리
 * 25. 7. 1.		durururuk		deprecated된 이미지 서비스 메서드 삭제
 * 25. 7. 1.		durururuk		이미지 썸네일을 조회하는 메서드에서 썸네일 DTO를 반환하게 변경
 * 25. 7. 1.		durururuk		앨범 이미지 리스트를 조회할 때 기존 원본이미지 조회에서 이미지 썸네일을 조회하게 변경
 * 25. 7. 7.		durururuk		쿼리가 산발적으로 돼있어서 페이지네이션 정보가 실제 값과 일치하지 않던 문제 수정
 * 25. 7. 8.		durururuk		내 이미지 조회 시 조회 결과에서 제외될 앨범 ID 파라미터 추가
 * 25. 7. 8.		durururuk		개발 도중 흔적 제거
 * 25. 7. 8.		inari		이미지 업로드시 태그 추가
 * 25. 7. 9.		inari		체크스타일 수정 및 메서드 분리
 * 25. 7. 10.		durururuk		단순 이미지 로드 작업에 예전 로직이 들어있던 문제 수정
 * 25. 7. 10.		inari		이미지 단건 조회시 태그 추가
 * 25. 7. 11.		durururuk		내 이미지 리스트 조회 시 S3에서 이미지 조회 실패한 이미지는 제외하고 결과를 반환하게 수정
 * 25. 7. 11.		inari		image 도메인의 자바독 누락 및 체크스타일 해결
 * 25. 7. 11.		durururuk		JavaDoc주석 추가
 * 25. 7. 11.		durururuk		중복되는 리스팅 메서드 추출
 * 25. 7. 13.		inari		이미지 수정시 태그 삭제 기능 구현
 * 25. 7. 14.		inari		이미지 수정시 태그 추가 기능 구현
 * 25. 7. 15.		durururuk		이미지 삭제 시 이벤트 발행하게 수정
 * 25. 7. 15.		durururuk		ImageService @Transactional 어노테이션 추가
 * 25. 7. 15.		inari		ImageService에 있던 updateImageLocation, processTagRemoval, processTagAddition 각 도메인으로 이동
 * 25. 8. 14.		durururuk		changeImageProcessingStatus javadoc 주석 보충
 * 25. 9. 5.		durururuk		이미지 시도 커버리지 조회 서비스 메서드 작성
 * 25. 9. 5.		durururuk		Javadoc 주석 작성
 * 25. 9. 5.		durururuk		시도 커버리지 조회 메서드 Optional처리로 null 처리 강화
 * 25. 9. 5.		durururuk		시도 커버리지 조회 메서드 Set null 예외 처리 강화
 * 25. 9. 8.		durururuk		사용자의 시도별 시군구 이미지 커버리지 데이터를 조회 메서드 추가
 * 25. 9. 9.		durururuk		시도 커버리지 조회 메서드 필드명 수정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
	private final ImageMapper imageMapper;
	private final ImageS3Service imageS3Service;
	private final LocationService locationService;
	private final TagService tagService;
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 공개된 이미지 URL 목록을 조회합니다.
	 * 이미지가 없거나 비었을시 ErrorCode.NOT_FOUND_IMAGE
	 * 결과는 하루동안 캐시됩니다.
	 *
	 * @return 공개된 이미지 URL 목록
	 */
	@Transactional(readOnly = true)
	@Cacheable(value = "publicImageUrls")
	public List<String> getPublicImageUrls() {
		log.info("공개된 이미지의 주소들을 가져옵니다.");

		List<String> images = imageMapper.selectPublicImageFiles();

		if (images == null || images.isEmpty()) {
			throw new ApiException(ErrorCode.NOT_FOUND_IMAGE);
		}

		return images;
	}

	/**
	 * 매일 0시에 이미지 URL 캐시를 갱신합니다.
	 */
	@Scheduled(cron = "0 0 0 * * *")
	@CacheEvict(value = "publicImageUrls", allEntries = true)
	public void evictImageCache() {
		log.info("공개된 이미지 주소들을 삭제합니다.");
	}

	/**
	 * 이미지 ID로 원본 이미지 정보를 조회합니다.
	 * @param userId 사용자 ID (권한 확인용)
	 * @param imageId 이미지 ID
	 * @return 이미지 정보 DTO
	 * @throws ApiException 이미지를 찾을 수 없거나 권한이 없는 경우
	 */
	@Transactional(readOnly = true)
	public ImageDto getOriginalImageByImageId(Long userId, Long imageId) {
		Image image = imageMapper.findImageByImageId(imageId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE)
		);

		if (!image.getUserId().equals(userId) && image.getIsPublic().equals(0)) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		return convertImageToImageDto(image, userId);
	}

	/**
	 * 이미지 엔티티를 이미지 썸네일 DTO로 변환합니다.
	 * @param image 변환할 이미지 엔티티
	 * @param userId 사용자 ID
	 * @return 썸네일 URL과 이미지 ID를 포함한 썸네일 DTO
	 */
	public ImageThumbnailDto convertImageToImageThumbnailDto(Image image, Long userId) {
		String imagePresignedUrl = imageS3Service.generatePreSignedGetUrl(image.getImageName(), userId, "thumbnail");
		return ImageThumbnailDto
			.builder()
			.thumbnailUrl(imagePresignedUrl)
			.imageId(image.getImageId())
			.build();
	}

	/**
	 * 사용자 ID로 이미지 목록을 페이지네이션하여 조회합니다.
	 * S3에서 썸네일 조회 실패한 이미지는 결과에서 제외됩니다.
	 * @param imageSearchByUserIdDto 사용자 이미지 검색 조건 (사용자 ID, 페이지 번호, 페이지 크기 등)
	 * @return 썸네일 DTO 목록을 포함한 페이지네이션 정보
	 */
	@SuppressWarnings("squid:S3252")
	@Transactional(readOnly = true)
	public PageInfo<ImageThumbnailDto> getImageListByUserId(ImageSearchByUserIdDto imageSearchByUserIdDto) {
		PageHelper.startPage(imageSearchByUserIdDto.getPageNum(), imageSearchByUserIdDto.getPageSize());

		List<ImageInfoForThumbnailDto> imageInfoForThumbnailDtos = imageMapper.findImageThumbnailsByUserId(
			imageSearchByUserIdDto);

		PageInfo<ImageInfoForThumbnailDto> sourcePageInfo = new PageInfo<>(imageInfoForThumbnailDtos);

		return convertToThumbnailPageInfo(sourcePageInfo, imageSearchByUserIdDto.getUserId());
	}

	/**
	 * 이미지 객체를 이미지 DTO로 가공하는 메서드
	 * @param image 이미지 객체
	 * @param userId 사용자 ID
	 * @return 이미지 정보를 담고있는 DTO
	 */
	public ImageDto convertImageToImageDto(Image image, Long userId) {
		String imagePresignedUrl = imageS3Service.generatePreSignedGetUrl(image.getImageName(), userId, "original");

		CoordinatePoint coordPoint = image.getCoordPoint();
		LocationInfoDto locationInfoDto = LocationUtil.getLocationInfoByCoordinatePoint(coordPoint);

		List<TagForImageResponseDto> imageTags = tagService.getTagsForImageDisplay(image.getImageId());
		return ImageDto.builder()
			.imageId(image.getImageId())
			.userId(image.getUserId())
			.imageRegDate(image.getImageRegDate())
			.sdName(locationInfoDto.sidoName())
			.sggName(locationInfoDto.sigunguName())
			.latitude(locationInfoDto.latitude())
			.longitude(locationInfoDto.longitude())
			.imageName(image.getImageName())
			.imageContent(image.getImageContent())
			.imageDate(image.getImageDate())
			.isPublic(image.getIsPublic())
			.imageUrl(imagePresignedUrl)
			.tags(imageTags)
			.build();
	}

	/**
	 * 특정 시도에 등록된 사용자의 이미지를 조회합니다.
	 * @param sidoId 시도 ID
	 * @param userId 사용자 ID
	 * @return 이미지 썸네일 목록
	 */
	@Transactional(readOnly = true)
	public List<ImageThumbnailDto> getImagesBySido(Long sidoId, Long userId) {
		log.debug("시도 ID {}의 사용자 ID {} 이미지 목록 조회", sidoId, userId);
		List<Image> images = imageMapper.findImagesBySidoIdAndUserId(sidoId, userId);
		log.debug("시도 ID {}의 사용자 ID {} 이미지 조회 완료 - {} 개", sidoId, userId, images.size());
		return images.stream()
			.map(image -> convertImageToImageThumbnailDto(image, userId))
			.toList();
	}

	/**
	 * 특정 시군구에 등록된 사용자의 이미지를 조회합니다.
	 * @param sigunguId 시군구 ID
	 * @param userId 사용자 ID
	 * @return 이미지 썸네일 목록
	 */
	@Transactional(readOnly = true)
	public List<ImageThumbnailDto> getImagesBySigungu(Long sigunguId, Long userId) {
		log.debug("시군구 ID {}의 사용자 ID {} 이미지 목록 조회", sigunguId, userId);
		List<Image> images = imageMapper.findImagesBySigunguIdAndUserId(sigunguId, userId);
		log.debug("시군구 ID {}의 사용자 ID {} 이미지 조회 완료 - {} 개", sigunguId, userId, images.size());
		return images.stream()
			.map(image -> convertImageToImageThumbnailDto(image, userId))
			.toList();
	}

	/**
	 * 이미지 메타데이터를 수정합니다.
	 * @param imageId 이미지 ID
	 * @param userId 요청하는 사용자 ID (권한 확인용)
	 * @param updateRequest 수정할 데이터
	 * @return 수정된 이미지 정보
	 */
	@Transactional
	@CacheEvict(value = "publicImageUrls", allEntries = true)
	public ImageDto updateImageMetadata(Long imageId, Long userId, ImageUpdateRequestDto updateRequest) {
		// 이미지 수정 권한 검증
		Image existingImage = imageMapper.findImageByImageId(imageId)
			.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE));

		if (!existingImage.getUserId().equals(userId)) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		// 기본 이미지 메타데이터 수정
		imageMapper.updateImageMetadata(
			imageId,
			updateRequest.imageName(),
			updateRequest.imageContent(),
			updateRequest.imageDate(),
			updateRequest.isPublic()
		);

		// 위치 정보 수정
		Long existingCoordPointId = existingImage.getCoordPoint().getCoordinatePointId();
		locationService.updateImageLocation(existingCoordPointId, updateRequest.latitude(),
			updateRequest.longitude(), imageId);

		// 태그 삭제 및 추가 처리
		tagService.processTagRemoval(imageId, updateRequest);
		tagService.processTagAddition(imageId, updateRequest);

		return getOriginalImageByImageId(userId, imageId);
	}

	/**
	 * 이미지를 삭제합니다 (논리적 삭제) 이후 연결된 태그의 사용 카운트를 감소시킨후 연결된 태그 관계를 해제합니다.
	 * @param imageId 삭제할 이미지 ID
	 * @param userId 요청하는 사용자 ID (권한 확인용)
	 */
	@Transactional
	@CacheEvict(value = "publicImageUrls", allEntries = true)
	public void deleteImage(Long imageId, Long userId) {
		Image existingImage = imageMapper.findImageByImageId(imageId)
			.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE));

		if (!existingImage.getUserId().equals(userId)) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		// 이미지와 연결된 모든 태그 관계 해제 및 사용 카운트 감소
		try {
			tagService.removeAllTagsFromImage(imageId);
		} catch (Exception e) {
			log.warn("이미지 태그 해제 중 오류 발생 - imageId: {}, 오류: {}", imageId, e.getMessage());
			// 태그 해제 실패해도 이미지 삭제는 계속 진행
		}

		int deletedRows = imageMapper.deleteImage(imageId);

		if (deletedRows == 0) {
			throw new ApiException(ErrorCode.NOT_FOUND_IMAGE);
		}

		applicationEventPublisher.publishEvent(new ImageDeleteEvent(imageId));

		log.info("이미지 삭제 완료 - imageId: {}, userId: {}", imageId, userId);
	}

	/**
	 * 썸네일 이미지, 유저 프로필 사진과 같이 이미지 전체의 정보가 필요없고 이미지 S3 URL만 필요할 때 사용하는 메서드입니다.
	 * @param imageId 이미지 ID
	 * @return S3 Presigned URL
	 * @throws ApiException 이미지를 찾을 수 없는 경우
	 */
	@Transactional(readOnly = true)
	public String fetchS3PresignedUrlByImageId(Long imageId) {
		Image image = imageMapper.findImageByImageId(imageId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE));

		return imageS3Service.generatePreSignedGetUrl(image.getImageName(), image.getUserId(), "original");
	}

	/**
	 * PageInfo<ImageInfoForThumbnailDto>를 PageInfo<ImageThumbnailDto>로 변환합니다.
	 * S3에서 썸네일 조회 실패한 이미지는 결과에서 제외됩니다.
	 * @param sourcePageInfo 원본 페이지 정보
	 * @param userId 사용자 ID
	 * @return 썸네일 DTO 목록을 포함한 페이지네이션 정보
	 */
	public PageInfo<ImageThumbnailDto> convertToThumbnailPageInfo(
		PageInfo<ImageInfoForThumbnailDto> sourcePageInfo, Long userId) {

		List<ImageThumbnailDto> thumbnailList = new ArrayList<>();
		for (ImageInfoForThumbnailDto imageInfo : sourcePageInfo.getList()) {
			ImageThumbnailDto thumbnail = convertToThumbnail(imageInfo, userId);
			if (thumbnail != null) {
				thumbnailList.add(thumbnail);
			}
		}
		return PageUtil.convert(sourcePageInfo, thumbnailList);
	}

	/**
	 * ImageInfoForThumbnailDto를 ImageThumbnailDto로 변환합니다.
	 * @param imageInfo 썸네일 생성용 이미지 정보
	 * @param userId 사용자 ID
	 * @return 썸네일 DTO
	 */
	public ImageThumbnailDto convertToThumbnail(ImageInfoForThumbnailDto imageInfo, Long userId) {
		try {
			String thumbnailUrl = imageS3Service.generatePreSignedGetUrl(imageInfo.getImageName(), userId, "thumbnail");

			return ImageThumbnailDto.builder()
				.imageId(imageInfo.getImageId())
				.thumbnailUrl(thumbnailUrl)
				.build();
		} catch (ApiException e) {
			if (e.getErrorCode() == ErrorCode.NOT_FOUND_IMAGE_OBJECT_KEY) {
				log.warn("S3에서 이미지 조회 실패, 이미지: {} (user: {})", imageInfo.getImageName(), userId);
				return null;
			}
			throw e;
		}
	}

	/**
	 * 이미지 처리 상태 변경 메서드
	 * @param imageName 이미지 이름
	 * @param processingStatus 처리 상태 (0 : 처리중, 1 : 처리 완료)
	 */
	public void changeImageProcessingStatus(String imageName, int processingStatus) {
		imageMapper.changeImageProcessingStatus(imageName, processingStatus);
	}

	/**
	 * 사용자별 시도 이미지 커버리지 데이터를 조회합니다.
	 * 각 시도별로 모든 시군구에 이미지가 있으면 COMPLETE, 일부만 있으면 PARTIAL로 분류합니다.
	 * @param userId 사용자 ID
	 * @return 시도별 이미지 커버리지 정보 (PARTIAL/COMPLETE 시도 ID 목록)
	 */
	@Transactional(readOnly = true)
	public ImageSidoCoverageResponseDto getImageSidoCoverageData(Long userId) {
		ImageSidoCoverageResponseDto result = imageMapper.selectSidoCoverage(userId).orElseThrow(
			() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
		);

		if (result.getPartialSidoIdList() == null || result.getCompleteSidoIdList() == null) {
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		return result;
	}

	/**
	 * 사용자의 시도별 시군구 이미지 커버리지 데이터를 조회합니다.
	 * 이미지가 존재하는 시군구 ID를 List<Long>이 담긴 DTO로 반환합니다.
	 * @param userId 사용자 ID
	 * @param sidoId 시도 ID
	 * @return 시도별 시군구 이미지 커버리지 정보 ()
	 */
	@Transactional(readOnly = true)
	public ImageSigunguCoverageResponseDto getImageSigunguCoverageData(Long userId, Long sidoId) {
		ImageSigunguCoverageResponseDto result = imageMapper.selectSigunguCoverage(userId, sidoId).orElseThrow(
			() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
		);

		if (result.getHavingImagesSigunguIdList() == null) {
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		return result;
	}
}
