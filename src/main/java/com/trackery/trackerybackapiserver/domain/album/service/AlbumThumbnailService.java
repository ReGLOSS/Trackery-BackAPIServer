package com.trackery.trackerybackapiserver.domain.album.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.album.enums.AlbumImageEditOperation;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.service
 * fileName       : AlbumThumbnailService
 * author         : durururuk
 * date           : 25. 6. 20.
 * description    : 앨범 썸네일 관련 기능을 담당하는 서비스 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 20.		durururuk		최초 생성
 */
@Service
@RequiredArgsConstructor
public class AlbumThumbnailService {
	private final AlbumMapper albumMapper;

	public void handleAlbumThumbnailChange(AlbumImageEditResponseDto dto, AlbumImageEditOperation operation) {
		switch (operation) {
			case ADD:
				setAlbumThumbnailForAdd(dto);
				break;
			case DELETE:
				changeAlbumThumbnailForDeleteImageFromAlbum(dto);
				break;
			default:
				throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public void setAlbumThumbnailForAdd(AlbumImageEditResponseDto dto) {
		Album album = albumMapper.findByAlbumId(dto.getAlbumId()).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_ALBUM)
		);

		if (album.getThumbnailImageId() != null) {
			return;
		}

		Long firstRegisteredImageId = dto.getSucceededImageIds().stream().findFirst().orElseThrow(
			() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR)
		);

		albumMapper.setThumbnail(album.getAlbumId(), firstRegisteredImageId);
	}

	public void changeAlbumThumbnailForDeleteImageFromAlbum(AlbumImageEditResponseDto dto) {
		Album album = albumMapper.findByAlbumId(dto.getAlbumId()).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_ALBUM)
		);

		if (!dto.getSucceededImageIds().contains(album.getThumbnailImageId())) {
			return;
		}

		List<AlbumImage> albumImageList = albumMapper.findAlbumImagesByAlbumId(album.getAlbumId());

		Long newThumbnailImageId = albumImageList.stream().findFirst().map(AlbumImage::getImageId).orElse(null);

		albumMapper.setThumbnail(album.getAlbumId(), newThumbnailImageId);
	}
}
