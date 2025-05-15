package com.trackery.trackerybackapiserver.domain.album.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.album.dto.AlbumImageListDto;
import com.trackery.trackerybackapiserver.domain.album.dto.AlbumRegisterDto;
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

	public Long createAlbum(Long userId, AlbumRegisterDto albumRegisterDto) {
		Album album = Album.builder()
			.userId(userId)
			.albumTitle(albumRegisterDto.getAlbumTitle())
			.albumDescription(albumRegisterDto.getAlbumDescription())
			.albumRegDate(LocalDateTime.now())
			.albumModDate(LocalDateTime.now())
			.isPublic(albumRegisterDto.isPublic())
			.build();

		albumMapper.saveAlbum(album);

		log.info("앨범 생성 완료 userId : {}, albumId : {}", userId, album.getAlbumId());

		return album.getAlbumId();
	}

	public void createAlbumImage(Long userId, Long albumId, AlbumImageListDto albumImageListDto) {

	}
}
