
package com.trackery.trackerybackapiserver.domain.album.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumCreateResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumDetailedResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageInsertResponseDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.image.service.ImageS3Service;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.service.LocationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.service
 * fileName       : AlbumService
 * author         : durururuk
 * date           : 25. 5. 14.
 * description    : 앨범 기능 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.		durururuk		최초 생성
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AlbumService {
	private final AlbumMapper albumMapper;
	private final ImageMapper imageMapper;
	private final ImageS3Service imageS3Service;

	/**
	 * 앨범 생성 기능
	 * @param userId 인증된 사용자 ID
	 * @param albumCreateRequestDto 생성될 앨범 정보 DTO
	 */
	public AlbumCreateResponseDto insertAlbum(Long userId, AlbumCreateRequestDto albumCreateRequestDto) {
		Album album = Album.builder()
			.userId(userId)
			.albumTitle(albumCreateRequestDto.getAlbumTitle())
			.albumDescription(albumCreateRequestDto.getAlbumDescription())
			.albumRegDate(LocalDateTime.now())
			.albumModDate(LocalDateTime.now())
			.isPublic(albumCreateRequestDto.getIsPublic())
			.build();

		albumMapper.insertAlbum(album);

		return AlbumCreateResponseDto.builder()
			.albumId(album.getAlbumId())
			.albumTitle(album.getAlbumTitle())
			.albumDescription(album.getAlbumDescription())
			.build();
	}

	/**
	 * 앨범에 이미지 추가 기능
	 * 1.ID로 앨범이 있는지 확인
	 * 2.앨범 생성 유저와 요청 유저가 같은지 확인
	 * 3.이미지가 존재하는지 확인
	 * 4.이미지를 업로드한 유저와 요청한 유저가 같은지 확인
	 * 5.이미지 추가
	 * 6.DTO 반환
	 * @param userId 인증된 사용자 ID
	 * @param albumId 이미지를 추가할 앨범 ID
	 * @param imageIdList 추가할 이미지 ID 리스트
	 * @return 앨범 ID, 추가 성공한 이미지 ID, 실패한 이미지 ID, 이유를 담은 DTO
	 */
	public AlbumImageInsertResponseDto addImageIntoAlbum(Long userId, Long albumId, List<Long> imageIdList) {
		Album album = albumMapper.findByAlbumId(albumId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_ALBUM)
		);

		if (!Objects.equals(album.getUserId(), userId)) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		Set<Long> succeededImageIds = new HashSet<>();
		Map<Long, String> failedImageIds = new HashMap<>();

		imageIdList.forEach(imageId -> {
			try {
				Image image = imageMapper.findImageByImageId(imageId).orElseThrow(
					() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE)
				);

				if (!image.getUserId().equals(userId)) {
					throw new ApiException(ErrorCode.FORBIDDEN);
				}

				AlbumImage albumImage = AlbumImage.builder().albumId(albumId).imageId(imageId).build();

				albumMapper.insertAlbumImage(albumImage);

				succeededImageIds.add(imageId);
			} catch (ApiException e) {
				log.error("{} userId : {}, albumId : {}, imageId : {}", e.getMessage(), userId, albumId, imageId);
				failedImageIds.put(imageId, e.getMessage());
			}
		});

		return AlbumImageInsertResponseDto.builder()
			.albumId(albumId)
			.succeededImageCount(succeededImageIds.size())
			.failedImageCount(failedImageIds.size())
			.succeededImageIds(succeededImageIds)
			.failedImageIds(failedImageIds)
			.build();
	}

	/*
	앨범 조회 기능
	1. 앨범 ID로 앨범 조회
	2. isPublic인지, isPublic이 0이면 album.userId가 요청한 유저인지
	3. 앨범 이미지 조회
	4. 이미지 ID 리스트 조회
	5. s3서비스에서 가져오기
	 */

	public AlbumDetailedResponseDto getAlbumDetailedInfo(Long userId, Long albumId) {
		log.info("userId : {}, albumId : {}", userId, albumId);
		Album album = albumMapper.findByAlbumId(albumId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_ALBUM)
		);

		if (album.getIsPublic() == 0 && !userId.equals(album.getUserId())) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		List<AlbumImage> albumImageList = albumMapper.findAlbumImagesByAlbumId(albumId);

		List<Image> imageList = albumImageList.stream()
			.map(albumImage -> {
				return imageMapper.findImageByImageId(albumImage.getImageId()).orElseThrow(
					() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE)
				);
			}).toList();

		List<ImageDto> imageDtoList = imageList.stream().map(
			image -> {
				String imagePresignedUrl = imageS3Service.generatePreSignedGetUrl(image.getImageFile());

				LocationInfoDto locationInfoDto = LocationUtil.getLocationInfoByCoordinatePoint(image.getCoordPoint());

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
		).toList();

		return AlbumDetailedResponseDto.of(album, imageDtoList);
	}

}