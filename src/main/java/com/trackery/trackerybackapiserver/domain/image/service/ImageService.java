package com.trackery.trackerybackapiserver.domain.image.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
	private final ImageMapper imageMapper;
	private final ImageS3Service imageS3Service;

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

	public ImageDto getImageByImageId(Long imageId) {
		Image image = imageMapper.findImageByImageId(imageId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE));

		return convertImageToImageDto(image);
	}

	public List<ImageDto> getImageListByUserId(Long userId) {
		return imageMapper.findImagesByUserId(userId).stream()
			.map(this::convertImageToImageDto)
			.toList();
	}

	private ImageDto convertImageToImageDto(Image image) {
		String imagePresignedUrl = imageS3Service.generatePreSignedGetUrl(image.getImageFile());

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
}
