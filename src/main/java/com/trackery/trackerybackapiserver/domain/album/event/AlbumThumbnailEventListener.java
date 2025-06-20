package com.trackery.trackerybackapiserver.domain.album.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.trackery.trackerybackapiserver.domain.album.service.AlbumThumbnailService;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.service
 * fileName       : AlbumThumbnailEventListener
 * author         : durururuk
 * date           : 25. 6. 20.
 * description    : 앨범 썸네일 이벤트 리스너
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 20.		durururuk		최초 생성
 */
@Component
@RequiredArgsConstructor
public class AlbumThumbnailEventListener {
	private final AlbumThumbnailService albumThumbnailService;

	/**
	 * 앨범 이미지가 추가/삭제됐을 때 썸네일 서비스로 이벤트를 전달해주는 이벤트리스너
	 * @param event 앨범 이미지 추가/삭제 이벤트
	 */
	@EventListener
	public void handleAlbumEditEvent(AlbumImageEditEvent event) {
		albumThumbnailService.handleAlbumThumbnailChange(event.album(), event.albumImageEditResponseDto(),
			event.albumImageEditOperation());
	}
}
