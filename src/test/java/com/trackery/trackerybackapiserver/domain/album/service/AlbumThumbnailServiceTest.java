package com.trackery.trackerybackapiserver.domain.album.service;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.album.enums.AlbumImageEditOperation;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.service
 * fileName       : AlbumThumbnailServiceTest
 * author         : durururuk
 * date           : 25. 6. 20.
 * description    : AlbumThumbnailService 단위테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 20.		durururuk		최초 생성
 * 25. 6. 23.		durururuk		AlbumThumbnailService 단위테스트 작성
 */
@ExtendWith(MockitoExtension.class)
class AlbumThumbnailServiceTest {
	@Mock
	private AlbumMapper albumMapper;

	@InjectMocks
	private AlbumThumbnailService albumThumbnailService;

	private static final Long THUMBNAIL_IMAGE_ID = 5L;
	private static final Long ALBUM_ID = 3L;
	private static final Long NEW_IMAGE_ID = 1L;
	private static final Long ANOTHER_IMAGE_ID = 2L;

	private Album album;
	private Album albumWithThumbnail;
	private AlbumImageEditResponseDto dto;
	private AlbumImageEditResponseDto emptyDto;

	@BeforeEach
	void setUp() {
		album = Album
			.builder()
			.build();
		ReflectionTestUtils.setField(album, "albumId", ALBUM_ID);

		albumWithThumbnail = Album
			.builder()
			.build();
		ReflectionTestUtils.setField(albumWithThumbnail, "albumId", ALBUM_ID);
		ReflectionTestUtils.setField(albumWithThumbnail, "thumbnailImageId", THUMBNAIL_IMAGE_ID);

		dto = AlbumImageEditResponseDto
			.builder()
			.albumId(ALBUM_ID)
			.succeededImageIds(Set.of(NEW_IMAGE_ID))
			.succeededImageCount(1)
			.build();

		emptyDto = AlbumImageEditResponseDto
			.builder()
			.albumId(ALBUM_ID)
			.succeededImageIds(Collections.emptySet())
			.succeededImageCount(0)
			.build();
	}

	@Nested
	class HandleAlbumThumbnailChangeTest {
		@Test
		void 이미지_추가_시_핸들링_성공() {
			// given
			doNothing().when(albumMapper).setThumbnail(ALBUM_ID, NEW_IMAGE_ID);

			// when
			albumThumbnailService.handleAlbumThumbnailChange(album, dto, AlbumImageEditOperation.ADD);

			// then
			verify(albumMapper, times(1)).setThumbnail(ALBUM_ID, NEW_IMAGE_ID);
		}

		@Test
		void 이미지_삭제_시_핸들링_성공() {
			AlbumImageEditResponseDto deleteDto = AlbumImageEditResponseDto
				.builder()
				.albumId(ALBUM_ID)
				.succeededImageIds(Set.of(THUMBNAIL_IMAGE_ID))
				.succeededImageCount(1)
				.build();

			List<AlbumImage> remainingImages = List.of(
				AlbumImage.builder().albumId(ALBUM_ID).imageId(ANOTHER_IMAGE_ID).build()
			);

			when(albumMapper.findAlbumImagesByAlbumId(ALBUM_ID)).thenReturn(remainingImages);
			doNothing().when(albumMapper).setThumbnail(ALBUM_ID, ANOTHER_IMAGE_ID);

			albumThumbnailService.handleAlbumThumbnailChange(albumWithThumbnail, deleteDto, AlbumImageEditOperation.DELETE);

			verify(albumMapper, times(1)).findAlbumImagesByAlbumId(ALBUM_ID);
			verify(albumMapper, times(1)).setThumbnail(ALBUM_ID, ANOTHER_IMAGE_ID);
		}
	}

	@Nested
	class SetAlbumThumbnailForAddImagesIntoAlbumTest {
		@Test
		void 썸네일이_없는_앨범에_이미지_추가_시_첫번째_이미지를_썸네일로_설정() {
			doNothing().when(albumMapper).setThumbnail(ALBUM_ID, NEW_IMAGE_ID);

			albumThumbnailService.setAlbumThumbnailForAddImagesIntoAlbum(album, dto);

			verify(albumMapper, times(1)).setThumbnail(ALBUM_ID, NEW_IMAGE_ID);
		}

		@Test
		void 이미_썸네일이_있는_앨범에_이미지_추가_시_썸네일_변경하지_않음() {
			albumThumbnailService.setAlbumThumbnailForAddImagesIntoAlbum(albumWithThumbnail, dto);

			verify(albumMapper, never()).setThumbnail(any(), any());
		}

		@Test
		void 성공한_이미지가_없을_때_아무_작업하지_않음() {
			albumThumbnailService.setAlbumThumbnailForAddImagesIntoAlbum(album, emptyDto);

			verify(albumMapper, never()).setThumbnail(any(), any());
		}

		@Test
		void 여러_이미지_추가_시_첫번째_이미지를_썸네일로_설정() {
			AlbumImageEditResponseDto multipleImagesDto = AlbumImageEditResponseDto
				.builder()
				.albumId(ALBUM_ID)
				.succeededImageIds(Set.of(NEW_IMAGE_ID, ANOTHER_IMAGE_ID))
				.succeededImageCount(2)
				.build();

			doNothing().when(albumMapper).setThumbnail(eq(ALBUM_ID), anyLong());

			albumThumbnailService.setAlbumThumbnailForAddImagesIntoAlbum(album, multipleImagesDto);

			// then
			verify(albumMapper, times(1)).setThumbnail(eq(ALBUM_ID), anyLong());
		}
	}

	@Nested
	class ChangeAlbumThumbnailForDeleteImagesFromAlbumTest {
		@Test
		void 썸네일_이미지가_삭제된_경우_다른_이미지로_썸네일_변경() {
			AlbumImageEditResponseDto deleteDto = AlbumImageEditResponseDto
				.builder()
				.albumId(ALBUM_ID)
				.succeededImageIds(Set.of(THUMBNAIL_IMAGE_ID))
				.succeededImageCount(1)
				.build();

			List<AlbumImage> remainingImages = List.of(
				AlbumImage.builder().albumId(ALBUM_ID).imageId(ANOTHER_IMAGE_ID).build()
			);

			when(albumMapper.findAlbumImagesByAlbumId(ALBUM_ID)).thenReturn(remainingImages);
			doNothing().when(albumMapper).setThumbnail(ALBUM_ID, ANOTHER_IMAGE_ID);

			albumThumbnailService.changeAlbumThumbnailForDeleteImagesFromAlbum(albumWithThumbnail, deleteDto);

			verify(albumMapper, times(1)).findAlbumImagesByAlbumId(ALBUM_ID);
			verify(albumMapper, times(1)).setThumbnail(ALBUM_ID, ANOTHER_IMAGE_ID);
		}

		@Test
		void 썸네일이_아닌_이미지가_삭제된_경우_썸네일_변경하지_않음() {
			AlbumImageEditResponseDto deleteDto = AlbumImageEditResponseDto
				.builder()
				.albumId(ALBUM_ID)
				.succeededImageIds(Set.of(ANOTHER_IMAGE_ID))
				.succeededImageCount(1)
				.build();

			albumThumbnailService.changeAlbumThumbnailForDeleteImagesFromAlbum(albumWithThumbnail, deleteDto);

			// then
			verify(albumMapper, never()).findAlbumImagesByAlbumId(any());
			verify(albumMapper, never()).setThumbnail(any(), any());
		}

		@Test
		void 모든_이미지가_삭제된_경우_썸네일을_null로_설정() {
			AlbumImageEditResponseDto deleteDto = AlbumImageEditResponseDto
				.builder()
				.albumId(ALBUM_ID)
				.succeededImageIds(Set.of(THUMBNAIL_IMAGE_ID))
				.succeededImageCount(1)
				.build();

			List<AlbumImage> emptyList = Collections.emptyList();

			when(albumMapper.findAlbumImagesByAlbumId(ALBUM_ID)).thenReturn(emptyList);
			doNothing().when(albumMapper).setThumbnail(ALBUM_ID, null);

			albumThumbnailService.changeAlbumThumbnailForDeleteImagesFromAlbum(albumWithThumbnail, deleteDto);

			verify(albumMapper, times(1)).findAlbumImagesByAlbumId(ALBUM_ID);
			verify(albumMapper, times(1)).setThumbnail(ALBUM_ID, null);
		}

		@Test
		void 썸네일_이미지를_포함한_여러_이미지_삭제_시_남은_첫번째_이미지로_썸네일_설정() {
			AlbumImageEditResponseDto deleteDto = AlbumImageEditResponseDto
				.builder()
				.albumId(ALBUM_ID)
				.succeededImageIds(Set.of(THUMBNAIL_IMAGE_ID, NEW_IMAGE_ID))
				.succeededImageCount(2)
				.build();

			List<AlbumImage> remainingImages = List.of(
				AlbumImage.builder().albumId(ALBUM_ID).imageId(ANOTHER_IMAGE_ID).build()
			);

			when(albumMapper.findAlbumImagesByAlbumId(ALBUM_ID)).thenReturn(remainingImages);
			doNothing().when(albumMapper).setThumbnail(ALBUM_ID, ANOTHER_IMAGE_ID);

			albumThumbnailService.changeAlbumThumbnailForDeleteImagesFromAlbum(albumWithThumbnail, deleteDto);

			verify(albumMapper, times(1)).findAlbumImagesByAlbumId(ALBUM_ID);
			verify(albumMapper, times(1)).setThumbnail(ALBUM_ID, ANOTHER_IMAGE_ID);
		}

		@Test
		void 앨범에_썸네일이_없는_경우_삭제_작업_무시() {
			AlbumImageEditResponseDto deleteDto = AlbumImageEditResponseDto
				.builder()
				.albumId(ALBUM_ID)
				.succeededImageIds(Set.of(NEW_IMAGE_ID))
				.succeededImageCount(1)
				.build();

			albumThumbnailService.changeAlbumThumbnailForDeleteImagesFromAlbum(album, deleteDto);

			verify(albumMapper, never()).findAlbumImagesByAlbumId(any());
			verify(albumMapper, never()).setThumbnail(any(), any());
		}
	}
}