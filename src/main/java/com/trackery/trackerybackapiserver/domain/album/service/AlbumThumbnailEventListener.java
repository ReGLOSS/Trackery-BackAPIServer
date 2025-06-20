package com.trackery.trackerybackapiserver.domain.album.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.service
 * fileName       : AlbumThumbnailEventListener
 * author         : durururuk
 * date           : 25. 6. 20.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 20.		durururuk		최초 생성
 */
@Component
@RequiredArgsConstructor
public class AlbumThumbnailEventListener {
	private final AlbumThumbnailService albumThumbnailService;

	@EventListener
	public void handleAlbumEditEvent(AlbumImageEditEvent event) {
		albumThumbnailService.handleAlbumThumbnailChange(event.getAlbumImageEditResponseDto(),
			event.getAlbumImageEditOperation());
	}
}
