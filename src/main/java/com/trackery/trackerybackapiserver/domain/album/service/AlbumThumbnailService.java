package com.trackery.trackerybackapiserver.domain.album.service;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;

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
}
