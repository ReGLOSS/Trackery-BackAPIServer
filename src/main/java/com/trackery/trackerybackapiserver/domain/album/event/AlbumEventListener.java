package com.trackery.trackerybackapiserver.domain.album.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.trackery.trackerybackapiserver.domain.album.service.AlbumService;
import com.trackery.trackerybackapiserver.domain.album.service.AlbumThumbnailService;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.event
 * fileName       : AlbumEventListener
 * author         : durururuk
 * date           : 25. 6. 20.
 * description    : 앨범 이벤트 리스너
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 20.		durururuk		최초 생성
 * 25. 6. 20.		durururuk		이벤트 리스너 작성
 * 25. 6. 20.		durururuk		AlbumImageEditEvent 클래스 레코드로 전환
 * 25. 6. 20.		durururuk		이벤트 관련 클래스 이벤트 패키지로 이동
 * 25. 6. 20.		durururuk		이벤트를 발행할 때 앨범 정보도 같이 넘겨줘서 Album 두 번 조회하지 않게 수정
 * 25. 6. 20.		durururuk		주석 수정
 * 25. 7. 15.		durururuk		앨범 썸네일 이벤트 리스너 이름 변경
 */
@Component
@RequiredArgsConstructor
public class AlbumEventListener {
	private final AlbumThumbnailService albumThumbnailService;
	private final AlbumService albumService;

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
