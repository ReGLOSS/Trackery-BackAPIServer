package com.trackery.trackerybackapiserver.domain.album.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumCreateResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumDetailedResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumSimpledResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.MyAlbumResponseDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.album.enums.AlbumImageEditOperation;
import com.trackery.trackerybackapiserver.domain.album.event.AlbumImageEditEvent;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;

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
	private final ImageService imageService;
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 앨범 생성 기능
	 * @param userId 인증된 사용자 ID
	 * @param albumCreateRequestDto 생성될 앨범 정보 DTO
	 */
	public AlbumCreateResponseDto insertAlbum(Long userId, AlbumCreateRequestDto albumCreateRequestDto) {
		ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
		Album album = Album.builder()
			.userId(userId)
			.albumTitle(albumCreateRequestDto.getAlbumTitle())
			.albumDescription(albumCreateRequestDto.getAlbumDescription())
			.albumRegDate(LocalDateTime.now(seoulZoneId))
			.albumModDate(LocalDateTime.now(seoulZoneId))
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
	 * 앨범이 있는지, 요청한 유저가 앨범을 생성한 유저와 동일한지 체크하는 메서드
	 * 앨범 수정 권한을 추가할 일이 있을 때 메서드 수정 필요
	 * @param userId : 유저 ID
	 * @param albumId : 앨범 ID
	 */
	public Album findAlbumAndCheckPermission(Long userId, Long albumId) {
		Album album = albumMapper.findByAlbumId(albumId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_ALBUM));

		if (!album.getUserId().equals(userId)) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		return album;
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
	public AlbumImageEditResponseDto addImageIntoAlbum(Long userId, Long albumId, List<Long> imageIdList) {
		Album album = findAlbumAndCheckPermission(userId, albumId);

		Set<Long> succeededImageIds = new HashSet<>();
		Map<Long, String> failedImageIds = new HashMap<>();

		imageIdList.forEach(imageId -> {
			try {
				Image image = imageMapper.findImageByImageId(imageId)
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE));

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

		AlbumImageEditResponseDto result = AlbumImageEditResponseDto.builder()
			.albumId(albumId)
			.succeededImageCount(succeededImageIds.size())
			.failedImageCount(failedImageIds.size())
			.succeededImageIds(succeededImageIds)
			.failedImageIds(failedImageIds)
			.build();

		applicationEventPublisher.publishEvent(
			new AlbumImageEditEvent(album, result, AlbumImageEditOperation.ADD)
		);

		return result;
	}

	/**
	 * 앨범 이미지 삭제 기능
	 * 1.ID로 앨범이 있는지 확인
	 * 2.앨범 생성 유저와 요청 유저가 같은지 확인
	 * 3.앨범에 해당 이미지가 존재하는지 확인
	 * 4.이미지 삭제
	 * 5.DTO 반환
	 * @param userId 인증된 사용자 ID
	 * @param albumId 이미지를 삭제할 앨범 ID
	 * @param imageIdList 삭제할 이미지 ID 리스트
	 * @return 앨범 ID, 삭제 성공한 이미지 ID, 실패한 이미지 ID, 이유를 담은 DTO
	 */
	public AlbumImageEditResponseDto deleteImageFromAlbum(Long userId, Long albumId, List<Long> imageIdList) {
		Album album = findAlbumAndCheckPermission(userId, albumId);

		Set<Long> succeededImageIds = new HashSet<>();
		Map<Long, String> failedImageIds = new HashMap<>();

		imageIdList.forEach(imageId -> {
			try {
				AlbumImage albumImage = albumMapper.findAlbumImageByAlbumIdAndImageId(albumId, imageId)
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

				albumMapper.deleteAlbumImageByAlbumImageId(albumImage.getAlbumImageId());

				succeededImageIds.add(imageId);
			} catch (ApiException e) {
				log.error("앨범 이미지 삭제 실패: {}, userId: {}, albumId: {}, imageId: {}", e.getMessage(), userId, albumId,
					imageId);
				failedImageIds.put(imageId, e.getMessage());
			}
		});

		AlbumImageEditResponseDto result = AlbumImageEditResponseDto.builder()
			.albumId(albumId)
			.succeededImageCount(succeededImageIds.size())
			.failedImageCount(failedImageIds.size())
			.succeededImageIds(succeededImageIds)
			.failedImageIds(failedImageIds)
			.build();

		applicationEventPublisher.publishEvent(new AlbumImageEditEvent(album, result, AlbumImageEditOperation.DELETE));

		return result;
	}

	/**
	 * 앨범 메타데이터 조회
	 * @param userId 요청한 유저 ID
	 * @param albumId 조회할 앨범 ID
	 * @return 앨범의 정보를 담은 DTO
	 */
	public AlbumDetailedResponseDto getAlbumMetadata(Long userId, Long albumId) {
		Album album = albumMapper.findByAlbumId(albumId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_ALBUM)
		);

		if (album.getIsPublic() == 0 && !userId.equals(album.getUserId())) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		List<AlbumImage> albumImageList = albumMapper.findAlbumImagesByAlbumId(albumId);

		return AlbumDetailedResponseDto.builder()
			.albumId(album.getAlbumId())
			.createdUserId(album.getUserId())
			.albumTitle(album.getAlbumTitle())
			.albumDescription(album.getAlbumDescription())
			.isPublic(album.getIsPublic())
			.imageCount(albumImageList.size())
			.build();
	}

	/**
	 * 앨범 이미지 조회
	 * @param albumId 조회할 이미지 ID
	 * @param pageNum 페이지 번호
	 * @param pageSize 페이지 크기
	 * @return 페이지네이션된 이미지 DTO 리스트
	 */
	@SuppressWarnings("squid:S3252")
	public PageInfo<ImageDto> getAlbumImages(Long albumId, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		List<AlbumImage> albumImageList = albumMapper.findAlbumImagesByAlbumId(albumId);

		List<ImageDto> albumImageDtoList = albumImageList.stream()
			.map(albumImage -> imageMapper.findImageByImageId(albumImage.getImageId()).orElseThrow(
				() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE)
			))
			.map(imageService::convertImageToImageDto)
			.toList();

		return new PageInfo<>(albumImageDtoList);
	}

	/**
	 * 앨범 정보 수정
	 * @param userId 유저 ID
	 * @param albumUpdateRequestDto 앨범 정보 수정 Request DTO
	 */
	public void updateAlbumInfo(Long userId, Long albumId, AlbumUpdateRequestDto albumUpdateRequestDto) {
		findAlbumAndCheckPermission(userId, albumId);

		albumMapper.updateAlbumInfo(albumId, albumUpdateRequestDto);
	}

	/**
	 * 내 앨범 간단 조회
	 * 내가 만든 앨범의 제목, 앨범에 포함된 이미지 수, 공개 여부, 썸네일 이미지를 알려주는 메서드입니다.
	 * @param userId 조회하고자 하는 유저 ID
	 * @return DTO
	 */
	@Transactional(readOnly = true)
	public MyAlbumResponseDto getMyAlbumSimpleInfo(Long userId) {
		List<Album> albumList = albumMapper.findAlbumsByUserId(userId);

		List<AlbumSimpledResponseDto> albumSimpledResponseDtoList = albumList.stream().map(album -> {
			List<AlbumImage> albumImageList = albumMapper.findAlbumImagesByAlbumId(album.getAlbumId());
			String thumbnailImageUrl = null;

			if (album.getThumbnailImageId() != null) {
				thumbnailImageUrl = imageService.fetchS3PresignedUrlByImageId(album.getThumbnailImageId());
			}

			return AlbumSimpledResponseDto.builder()
				.albumId(album.getAlbumId())
				.albumTitle(album.getAlbumTitle())
				.albumImageCount(albumImageList.size())
				.isPublic(album.getIsPublic())
				.albumThumbnailUrl(thumbnailImageUrl)
				.build();
		}).toList();

		return MyAlbumResponseDto.builder()
			.userId(userId)
			.albumCount(albumList.size())
			.albumList(albumSimpledResponseDtoList)
			.build();
	}

	/**
	 * 앨범을 삭제(비활성)하는 메서드
	 * @param userId 유저 ID
	 * @param albumId 앨범 ID
	 */
	public void deleteAlbum(Long userId, Long albumId) {
		findAlbumAndCheckPermission(userId, albumId);

		albumMapper.deleteAlbumByAlbumId(albumId);
	}
}
