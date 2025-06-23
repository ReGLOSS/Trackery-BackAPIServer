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
import com.trackery.trackerybackapiserver.domain.album.event.AlbumThumbnailEventListener;
import com.trackery.trackerybackapiserver.domain.album.service.AlbumThumbnailService;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.listener
 * fileName       : AlbumThumbnailEventListenerTest
 * author         : durururuk
 * date           : 25. 6. 23.
 * description    : AlbumThumbnailEventListener 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 23.		durururuk		최초 생성
 */
@ExtendWith(MockitoExtension.class)
class AlbumThumbnailEventListenerTest {

	@Mock
	private AlbumThumbnailService albumThumbnailService;

	@InjectMocks
	private AlbumThumbnailEventListener albumThumbnailEventListener;

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
		albumThumbnailEventListener.handleAlbumEditEvent(addEvent);

		// then
		verify(albumThumbnailService, times(1)).handleAlbumThumbnailChange(album, dto, AlbumImageEditOperation.ADD);
	}

	@Test
	void 이미지_삭제_이벤트_처리_성공() {
		doNothing().when(albumThumbnailService).handleAlbumThumbnailChange(album, dto, AlbumImageEditOperation.DELETE);

		albumThumbnailEventListener.handleAlbumEditEvent(deleteEvent);

		verify(albumThumbnailService, times(1)).handleAlbumThumbnailChange(album, dto, AlbumImageEditOperation.DELETE);
	}

	@Test
	void 서비스_메서드에_올바른_파라미터_전달_확인() {
		albumThumbnailEventListener.handleAlbumEditEvent(addEvent);

		verify(albumThumbnailService).handleAlbumThumbnailChange(
			eq(album),
			eq(dto),
			eq(AlbumImageEditOperation.ADD)
		);
	}
}
