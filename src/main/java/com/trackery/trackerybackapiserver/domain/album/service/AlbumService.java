package com.trackery.trackerybackapiserver.domain.album.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.album.dto.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;

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
}
