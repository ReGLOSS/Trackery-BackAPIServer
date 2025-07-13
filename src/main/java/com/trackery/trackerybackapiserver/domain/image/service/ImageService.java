package com.trackery.trackerybackapiserver.domain.image.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PageUtil;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageInfoForThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageSearchByUserIdDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.location.service.LocationUtil;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagForImageResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.service.TagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.home.service
 * fileName       : ImageService
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 이미지 관련 비즈니스 로직을 처리하는 서비스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 * 25. 2. 19.        inari       이미지를 불러오지 못했을시 예외 처리
 * 25. 5. 15.		durururuk	 이미지 단건/다건 조회 기능 작성
 * 25. 6. 16.		 inari		 지도를 통한 이미지 조회 기능 추가
 * 25. 6. 20.		 inari		 이미지 수정 및 삭제 추가
 * 25. 6. 22.		 inari		 이미지 수정시 좌표 인서트가 아닌 업데이트로 변경
 * 25. 7. 7.		 inari		 이미지 좌표 인서트시 태그 추가
 * 25. 7. 9.		 inari		 removeAllTagsFromImage로 메서드 분리
 * 25. 7. 10.        inari       	이미지 단건 조회시 태그 추가
 * 25. 7. 11.		durururuk	 내 이미지 리스트 조회 시 S3에서 이미지 조회 실패한 이미지는 제외하고 결과를 반환하게 수정
 * 25. 7. 11.		durururuk	 중복되는 리스팅 메서드 추출
 * 25. 7. 12.		inari		이미지 수정시 태그 삭제 추가
 * 25. 7. 13.		inari		이미지 수정시 태그 수정 추가
 * 25. 7. 14.		inari		updateImageMetadata 코드 복잡도 수정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
	private final ImageMapper imageMapper;
	private final ImageS3Service imageS3Service;
	private final LocationService locationService;
	private final TagService tagService;

	/**
	 * 공개된 이미지 URL 목록을 조회합니다.
	 * 이미지가 없거나 비었을시 ErrorCode.NOT_FOUND_IMAGE
	 * 결과는 하루동안 캐시됩니다.
	 *
	 * @return 공개된 이미지 URL 목록
	 */
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
	@CacheEvict(value = "publicImageUrls", allEntries = true)
	public ImageDto updateImageMetadata(Long imageId, Long userId, ImageUpdateRequestDto updateRequest) {
		Image existingImage = validateImageUpdatePermission(imageId, userId);

		updateBasicImageMetadata(imageId, updateRequest);
		updateLocationIfProvided(imageId, existingImage, updateRequest);
		processTagRemoval(imageId, updateRequest);
		processTagAddition(imageId, updateRequest);

		return getOriginalImageByImageId(userId, imageId);
	}

	/**
	 * 이미지 수정 권한을 검증합니다.
	 * @param imageId 이미지 ID
	 * @param userId 사용자 ID
	 * @return 검증된 이미지 엔티티
	 * @throws ApiException 이미지를 찾을 수 없거나 권한이 없는 경우
	 */
	private Image validateImageUpdatePermission(Long imageId, Long userId) {
		Image existingImage = imageMapper.findImageByImageId(imageId)
			.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE));

		if (!existingImage.getUserId().equals(userId)) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		return existingImage;
	}

	/**
	 * 기본 이미지 메타데이터를 수정합니다.
	 * @param imageId 이미지 ID
	 * @param updateRequest 수정 요청 데이터
	 */
	private void updateBasicImageMetadata(Long imageId, ImageUpdateRequestDto updateRequest) {
		imageMapper.updateImageMetadata(
			imageId,
			updateRequest.imageName(),
			updateRequest.imageContent(),
			updateRequest.imageDate(),
			updateRequest.isPublic()
		);
	}

	/**
	 * 위치 정보가 제공된 경우 위치 정보를 수정합니다.
	 * @param imageId 이미지 ID
	 * @param existingImage 기존 이미지 정보
	 * @param updateRequest 수정 요청 데이터
	 */
	private void updateLocationIfProvided(Long imageId, Image existingImage, ImageUpdateRequestDto updateRequest) {
		if (updateRequest.latitude() == null || updateRequest.longitude() == null) {
			return;
		}

		try {
			CoordinateDto coordinateDto = new CoordinateDto(
				updateRequest.latitude(),
				updateRequest.longitude()
			);

			Long existingCoordPointId = existingImage.getCoordPoint().getCoordinatePointId();
			locationService.updateCoordinatePoint(existingCoordPointId, coordinateDto);

			log.info("이미지 위치 정보 수정 완료 - imageId: {}, coord_point_id: {}, 새로운 위치: {}, {}",
				imageId, existingCoordPointId, updateRequest.latitude(), updateRequest.longitude());
		} catch (Exception e) {
			log.error("이미지 위치 정보 수정 실패 - imageId: {}, 에러: {}", imageId, e.getMessage());
			throw new ApiException(ErrorCode.UPDATE_FAILED_LOCATION);
		}
	}

	/**
	 * 삭제할 태그들을 처리합니다.
	 * @param imageId 이미지 ID
	 * @param updateRequest 수정 요청 데이터
	 */
	private void processTagRemoval(Long imageId, ImageUpdateRequestDto updateRequest) {
		if (updateRequest.tagsToRemove() == null || updateRequest.tagsToRemove().isEmpty()) {
			return;
		}

		for (Long tagId : updateRequest.tagsToRemove()) {
			try {
				tagService.removeTagFromImage(imageId, tagId);
				log.info("이미지에서 태그 삭제 완료 - imageId: {}, tagId: {}", imageId, tagId);
			} catch (Exception e) {
				log.warn("이미지에서 태그 삭제 실패 - imageId: {}, tagId: {}, 오류: {}",
					imageId, tagId, e.getMessage());
			}
		}
	}

	/**
	 * 추가할 태그들을 처리합니다.
	 * @param imageId 이미지 ID
	 * @param updateRequest 수정 요청 데이터
	 */
	private void processTagAddition(Long imageId, ImageUpdateRequestDto updateRequest) {
		if (updateRequest.tagsToAdd() == null || updateRequest.tagsToAdd().isEmpty()) {
			return;
		}

		for (String tagName : updateRequest.tagsToAdd()) {
			if (tagName != null && !tagName.trim().isEmpty()) {
				try {
					tagService.addTagToImageByName(imageId, tagName.trim());
					log.info("이미지에 태그 추가 완료 - imageId: {}, tagName: {}", imageId, tagName.trim());
				} catch (Exception e) {
					log.warn("이미지에 태그 추가 실패 - imageId: {}, tagName: {}, 오류: {}",
						imageId, tagName.trim(), e.getMessage());
				}
			}
		}
	}

	/**
	 * 이미지를 삭제합니다 (논리적 삭제) 이후 연결된 태그의 사용 카운트를 감소시킨후 연결된 태그 관계를 해제합니다.
	 * @param imageId 삭제할 이미지 ID
	 * @param userId 요청하는 사용자 ID (권한 확인용)
	 */
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

		log.info("이미지 삭제 완료 - imageId: {}, userId: {}", imageId, userId);
	}

	/**
	 * 썸네일 이미지, 유저 프로필 사진과 같이 이미지 전체의 정보가 필요없고 이미지 S3 URL만 필요할 때 사용하는 메서드입니다.
	 * @param imageId 이미지 ID
	 * @return S3 Presigned URL
	 * @throws ApiException 이미지를 찾을 수 없는 경우
	 */
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
}
