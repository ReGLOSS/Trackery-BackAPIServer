package com.trackery.trackerybackapiserver.domain.image.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageInfoForThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.location.service.LocationUtil;

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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
	private final ImageMapper imageMapper;
	private final ImageS3Service imageS3Service;
	private final LocationService locationService;

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

	public ImageDto getOriginalImageByImageId(Long userId, Long imageId) {
		Image image = imageMapper.findImageByImageId(imageId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE)
		);

		if (!image.getUserId().equals(userId) && image.getIsPublic().equals(0)) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		return convertImageToImageDto(image, userId);
	}

	public ImageThumbnailDto convertImageToImageThumbnailDto(Image image, Long userId) {
		String imagePresignedUrl = imageS3Service.generatePreSignedGetUrl(image.getImageName(), userId, "thumbnail");
		return ImageThumbnailDto
			.builder()
			.thumbnailUrl(imagePresignedUrl)
			.imageId(image.getImageId())
			.build();
	}

	/**
	 * 유저 ID로 이미지 다건 조회 썸네일 페이지네이션 버전
	 * @param userId 유저 ID
	 * @param pageNum 페이지 번호
	 * @param pageSize 페이지 사이즈
	 * @return 페이지네이션된 이미지 DTO 리스트
	 */
	public PageInfo<ImageThumbnailDto> getImageListByUserIdV2(Long userId, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		List<ImageThumbnailDto> imageThumbnailDtoList = imageMapper.findImagesByUserId(userId).stream()
			.map(image -> convertImageToImageThumbnailDto(image, userId))
			.toList();

		return new PageInfo<>(imageThumbnailDtoList);
	}

	/**
	 * 이미지 객체를 이미지 DTO로 가공하는 메서드
	 * @param image 이미지 객체
	 * @return 이미지 정보를 담고있는 DTO
	 */
	public ImageDto convertImageToImageDto(Image image, Long userId) {
		String imagePresignedUrl = imageS3Service.generatePreSignedGetUrl(image.getImageName(), userId, "original");

		CoordinatePoint coordPoint = image.getCoordPoint();
		LocationInfoDto locationInfoDto = LocationUtil.getLocationInfoByCoordinatePoint(coordPoint);

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
			.build();
	}

	/**
	 * 특정 시도에 등록된 사용자의 이미지를 조회합니다.
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
		Image existingImage = imageMapper.findImageByImageId(imageId)
			.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE));

		if (!existingImage.getUserId().equals(userId)) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		// 기본 메타데이터 수정
		imageMapper.updateImageMetadata(
			imageId,
			updateRequest.imageName(),
			updateRequest.imageContent(),
			updateRequest.imageDate(),
			updateRequest.isPublic()
		);

		// 위치 정보가 제공된 경우 위치 정보도 수정
		if (updateRequest.latitude() != null && updateRequest.longitude() != null) {
			try {
				CoordinateDto coordinateDto = new CoordinateDto(
					updateRequest.latitude(),
					updateRequest.longitude()
				);

				// 기존 이미지의 coord_point_id를 사용하여 업데이트
				Long existingCoordPointId = existingImage.getCoordPoint().getCoordinatePointId();
				locationService.updateCoordinatePoint(existingCoordPointId, coordinateDto);

				log.info("이미지 위치 정보 수정 완료 - imageId: {}, coord_point_id: {}, 새로운 위치: {}, {}",
					imageId, existingCoordPointId, updateRequest.latitude(), updateRequest.longitude());
			} catch (Exception e) {
				log.error("이미지 위치 정보 수정 실패 - imageId: {}, 에러: {}", imageId, e.getMessage());
				throw new ApiException(ErrorCode.UPDATE_FAILED_LOCATION);
			}
		}

		return getOriginalImageByImageId(userId, imageId);
	}

	/**
	 * 이미지를 삭제합니다 (논리적 삭제).
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
	 */
	public String fetchS3PresignedUrlByImageId(Long imageId) {
		Image image = imageMapper.findImageByImageId(imageId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE));

		return imageS3Service.generatePreSignedGetUrl(image.getImageFile(), image.getUserId(), "original");
	}

	/**
	 * ImageInfoForThumbnailDto를 ImageThumbnailDto로 변환합니다.
	 * @param imageInfo 썸네일 생성용 이미지 정보
	 * @param userId 사용자 ID
	 * @return 썸네일 DTO
	 */
	public ImageThumbnailDto convertToThumbnail(ImageInfoForThumbnailDto imageInfo, Long userId) {
		String thumbnailUrl = imageS3Service.generatePreSignedGetUrl(imageInfo.getImageName(), userId, "thumbnail");
		
		return ImageThumbnailDto.builder()
			.imageId(imageInfo.getImageId())
			.thumbnailUrl(thumbnailUrl)
			.build();
	}
}
