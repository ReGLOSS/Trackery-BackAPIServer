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
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
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

	/**
	 * 이미지 ID로 단건 조회
	 * @param imageId 이미지 Id
	 * @return 이미지 정보를 담은 DTO
	 */
	public ImageDto getImageByImageId(Long imageId) {
		Image image = imageMapper.findImageByImageId(imageId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE));

		return convertImageToImageDto(image);
	}

	/**
	 * 유저 ID로 이미지 다건 조회
	 * @deprecated 페이지네이션 버전으로 변경 후 삭제 예정
	 * @param userId 조회할 유저 ID
	 * @return 이미지 정보를 담은 DTO
	 */
	@Deprecated(forRemoval = true)
	public List<ImageDto> getImageListByUserId(Long userId) {
		return imageMapper.findImagesByUserId(userId).stream()
			.map(this::convertImageToImageDto)
			.toList();
	}

	/**
	 * 유저 ID로 이미지 다건 조회 페이지네이션 버전
	 * @param userId 유저 ID
	 * @param pageNum 페이지 번호
	 * @param pageSize 페이지 사이즈
	 * @return 페이지네이션된 이미지 DTO 리스트
	 */
	public PageInfo<ImageDto> getImageListByUserIdV2(Long userId, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		List<ImageDto> imageDtoList = imageMapper.findImagesByUserId(userId).stream()
			.map(this::convertImageToImageDto)
			.toList();

		return new PageInfo<>(imageDtoList);
	}

	/**
	 * 이미지 객체를 이미지 DTO로 가공하는 메서드
	 * @param image 이미지 객체
	 * @return 이미지 정보를 담고있는 DTO
	 */
	public ImageDto convertImageToImageDto(Image image) {
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

	/**
	 * 특정 시도에 등록된 사용자의 이미지를 조회합니다.
	 */
	public List<ImageDto> getImagesBySido(Long sidoId, Long userId) {
		log.debug("시도 ID {}의 사용자 ID {} 이미지 목록 조회", sidoId, userId);
		List<Image> images = imageMapper.findImagesBySidoIdAndUserId(sidoId, userId);
		log.debug("시도 ID {}의 사용자 ID {} 이미지 조회 완료 - {} 개", sidoId, userId, images.size());
		return images.stream()
			.map(this::convertImageToImageDto)
			.toList();
	}

	/**
	 * 특정 시군구에 등록된 사용자의 이미지를 조회합니다.
	 */
	public List<ImageDto> getImagesBySigungu(Long sigunguId, Long userId) {
		log.debug("시군구 ID {}의 사용자 ID {} 이미지 목록 조회", sigunguId, userId);
		List<Image> images = imageMapper.findImagesBySigunguIdAndUserId(sigunguId, userId);
		log.debug("시군구 ID {}의 사용자 ID {} 이미지 조회 완료 - {} 개", sigunguId, userId, images.size());
		return images.stream()
			.map(this::convertImageToImageDto)
			.toList();
	}
}
