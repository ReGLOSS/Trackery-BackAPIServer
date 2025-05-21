package com.trackery.trackerybackapiserver.domain.album.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.album.dto.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;

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

	public void insertAlbum(Long userId, AlbumCreateRequestDto albumCreateRequestDto) {
		Album album = Album.builder()
			.userId(userId)
			.albumTitle(albumCreateRequestDto.getAlbumTitle())
			.albumDescription(albumCreateRequestDto.getAlbumDescription())
			.albumRegDate(LocalDateTime.now())
			.albumModDate(LocalDateTime.now())
			.isPublic(albumCreateRequestDto.getIsPublic())
			.build();

		albumMapper.insertAlbum(album);

		log.info("앨범 생성 완료 userId : {}, albumId : {}", userId, album.getAlbumId());
	}

	public void addImageIntoAlbum(Long userId, Long albumId, List<Long> imageIdList) {
		Album album = albumMapper.findByAlbumId(albumId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_ALBUM)
		);

		if (!Objects.equals(album.getUserId(), userId)) {
			throw new ApiException(ErrorCode.FORBIDDEN);
		}

		imageIdList.forEach(imageId -> {
			Image image = imageMapper.findImageByImageId(imageId).orElseThrow(
				() -> new ApiException(ErrorCode.NOT_FOUND_IMAGE)
			);

			if (!image.getUserId().equals(userId)) {
				throw new ApiException(ErrorCode.FORBIDDEN);
			}

			AlbumImage albumImage = AlbumImage.builder().albumId(albumId).imageId(imageId).build();

			albumMapper.insertAlbumImage(albumImage);
		});
	}
}