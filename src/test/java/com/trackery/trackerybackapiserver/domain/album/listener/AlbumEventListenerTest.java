package com.trackery.trackerybackapiserver.domain.album.listener;

import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.enums.AlbumImageEditOperation;
import com.trackery.trackerybackapiserver.domain.album.event.AlbumImageEditEvent;
import com.trackery.trackerybackapiserver.domain.album.event.AlbumEventListener;
import com.trackery.trackerybackapiserver.domain.album.service.AlbumThumbnailService;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.listener
 * fileName       : AlbumEventListenerTest
 * author         : durururuk
 * date           : 25. 6. 23.
 * description    : AlbumThumbnailEventListener 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 23.		durururuk		최초 생성
 * 25. 6. 23.		durururuk		앨범 썸네일 이벤트 리스너 테스트 코드 작성
 * 25. 6. 23.		durururuk		앨범 썸네일 이벤트 리스너 테스트 코드 작성
 * 25. 6. 28.		Nari-Lee		테스트 코드의 잘못된 eq()수정 및 컨트롤러 문서화 추가
 * 25. 7. 15.		durururuk		앨범 썸네일 이벤트 리스너 이름 변경
 */
@ExtendWith(MockitoExtension.class)
class AlbumEventListenerTest {

	@Mock
	private AlbumThumbnailService albumThumbnailService;

	@InjectMocks
	private AlbumEventListener albumEventListener;

	private static final Long ALBUM_ID = 1L;
	private static final Long IMAGE_ID = 2L;

	private Album album;
	private AlbumImageEditResponseDto dto;
	private AlbumImageEditEvent addEvent;
	private AlbumImageEditEvent deleteEvent;

	@BeforeEach
	void setUp() {
		album = Album.builder().build();
		ReflectionTestUtils.setField(album, "albumId", ALBUM_ID);

		dto = AlbumImageEditResponseDto.builder()
			.albumId(ALBUM_ID)
			.succeededImageIds(Set.of(IMAGE_ID))
			.succeededImageCount(1)
			.build();

		addEvent = new AlbumImageEditEvent(album, dto, AlbumImageEditOperation.ADD);
		deleteEvent = new AlbumImageEditEvent(album, dto, AlbumImageEditOperation.DELETE);
	}

	@Test
	void 이미지_추가_이벤트_처리_성공() {
		// given
		doNothing().when(albumThumbnailService).handleAlbumThumbnailChange(album, dto, AlbumImageEditOperation.ADD);

		// when
		albumEventListener.handleAlbumEditEvent(addEvent);

		// then
		verify(albumThumbnailService, times(1)).handleAlbumThumbnailChange(album, dto, AlbumImageEditOperation.ADD);
	}

	@Test
	void 이미지_삭제_이벤트_처리_성공() {
		doNothing().when(albumThumbnailService).handleAlbumThumbnailChange(album, dto, AlbumImageEditOperation.DELETE);

		albumEventListener.handleAlbumEditEvent(deleteEvent);

		verify(albumThumbnailService, times(1)).handleAlbumThumbnailChange(album, dto, AlbumImageEditOperation.DELETE);
	}

	@Test
	void 서비스_메서드에_올바른_파라미터_전달_확인() {
		albumEventListener.handleAlbumEditEvent(addEvent);

		verify(albumThumbnailService).handleAlbumThumbnailChange(
			// eq(album)이 불필요한 이유는 album이 객체 참조이고, Mockito가 기본적으로 equals() 메서드로 객체를 비교하기 때문
			album,
			dto,
			AlbumImageEditOperation.ADD
		);
	}
}
