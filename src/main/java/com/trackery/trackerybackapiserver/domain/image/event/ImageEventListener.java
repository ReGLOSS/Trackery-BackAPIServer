package com.trackery.trackerybackapiserver.domain.image.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.trackery.trackerybackapiserver.domain.album.service.AlbumService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.event
 * fileName       : ImageEventListener
 * author         : durururuk
 * date           : 25. 7. 15.
 * description    : 이미지 관련 이벤트를 처리하는 리스너
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 15.		durururuk		최초 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageEventListener {
	private final AlbumService albumService;

	@EventListener
	public void handleImageDeleteEvent(ImageDeleteEvent event) {
		try {
			albumService.deleteAllImageFromAlbum(event.imageId());
			log.info("모든 앨범에서 이미지 삭제 완료 - imageId: {}", event.imageId());
		} catch (Exception e) {
			log.error("앨범에서 이미지 삭제 실패 - imageId: {}, 오류: {}", event.imageId(), e.getMessage());
			throw e;
		}
	}
}
