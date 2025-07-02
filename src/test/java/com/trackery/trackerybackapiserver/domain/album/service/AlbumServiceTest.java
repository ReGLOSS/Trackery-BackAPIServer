package com.trackery.trackerybackapiserver.domain.album.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumCreateResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumDetailedResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.MyAlbumResponseDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {
	@Mock
	private AlbumMapper albumMapper;

	@Mock
	private ImageMapper imageMapper;

	@Mock
	private ImageService imageService;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@InjectMocks
	private AlbumService albumService;

	private static final Long USER_ID = 1L;
	private static final Long ALBUM_ID = 1L;
	private static final List<Long> IMAGE_IDS = List.of(1L, 2L, 3L);

	private Album album;
	private Image image1;
	private Image image2;
	private Image image3;

	@BeforeEach
	void setUp() {
		album = Album.builder()
			.userId(USER_ID)
			.isPublic(1)
			.build();
		ReflectionTestUtils.setField(album, "albumId", ALBUM_ID);

		image1 = Image.builder()
			.userId(USER_ID)
			.build();
		ReflectionTestUtils.setField(image1, "imageId", 1L);

		image2 = Image.builder()
			.userId(USER_ID)
			.build();
		ReflectionTestUtils.setField(image2, "imageId", 2L);

		image3 = Image.builder()
			.userId(USER_ID)
			.build();
		ReflectionTestUtils.setField(image3, "imageId", 3L);
	}

	@Nested
	@DisplayName("앨범 삽입 테스트")
	class InsertAlbumTest {
		@Test
		@DisplayName("성공")
		void testInsertAlbum_Success() {
			AlbumCreateRequestDto requestDto = new AlbumCreateRequestDto();
			ReflectionTestUtils.setField(requestDto, "albumTitle", "강릉 여행");
			ReflectionTestUtils.setField(requestDto, "albumDescription", "여행 기록");
			ReflectionTestUtils.setField(requestDto, "isPublic", 0);

			doAnswer((Answer<Void>)invocation -> {
				Object[] args = invocation.getArguments();
				Album album = (Album)args[0];
				ReflectionTestUtils.setField(album, "albumId", ALBUM_ID);
				return null;
			}).when(albumMapper).insertAlbum(any(Album.class));

			AlbumCreateResponseDto response = albumService.insertAlbum(USER_ID, requestDto);

			assertNotNull(response);
			assertNotNull(response.getAlbumId());
			assertEquals("강릉 여행", response.getAlbumTitle());
			assertEquals("여행 기록", response.getAlbumDescription());

			verify(albumMapper).insertAlbum(any(Album.class));
		}
	}

	@Nested
	@DisplayName("앨범에 이미지 추가 테스트")
	class AddImageIntoAlbumTest {

		@Nested
		@DisplayName("앨범에서 이미지 삭제 테스트")
		class DeleteImageFromAlbumTest {

			@Test
			@DisplayName("성공 - 앨범의 이미지가 성공적으로 삭제됨")
			void testDeleteImageFromAlbum_Success() {
				AlbumImage albumImage1 = AlbumImage.builder().albumId(ALBUM_ID).imageId(1L).build();
				ReflectionTestUtils.setField(albumImage1, "albumImageId", 101L);

				AlbumImage albumImage2 = AlbumImage.builder().albumId(ALBUM_ID).imageId(2L).build();
				ReflectionTestUtils.setField(albumImage2, "albumImageId", 102L);

				when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));
				when(albumMapper.findAlbumImageByAlbumIdAndImageId(ALBUM_ID, 1L))
					.thenReturn(Optional.of(albumImage1));
				when(albumMapper.findAlbumImageByAlbumIdAndImageId(ALBUM_ID, 2L))
					.thenReturn(Optional.of(albumImage2));

				AlbumImageEditResponseDto response = albumService.deleteImageFromAlbum(USER_ID, ALBUM_ID,
					List.of(1L, 2L));

				assertEquals(ALBUM_ID, response.getAlbumId());
				assertEquals(2, response.getSucceededImageCount());
				assertTrue(response.getFailedImageIds().isEmpty());
				verify(albumMapper, times(2)).deleteAlbumImageByAlbumImageId(anyLong());
			}

			@Test
			@DisplayName("실패 - 앨범을 찾을 수 없음")
			void testDeleteImageFromAlbum_AlbumNotFound() {
				when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.empty());

				List<Long> imageIds = List.of(1L);

				ApiException exception = assertThrows(ApiException.class,
					() -> albumService.deleteImageFromAlbum(USER_ID, ALBUM_ID, imageIds));

				assertEquals(ErrorCode.NOT_FOUND_ALBUM, exception.getErrorCode());
				verify(albumMapper, never()).deleteAlbumImageByAlbumImageId(anyLong());
			}

			@Test
			@DisplayName("실패 - 유저가 앨범에 접근 권한이 없음")
			void testDeleteImageFromAlbum_ForbiddenAccess() {
				Album forbiddenAlbum = Album.builder().userId(2L).build();
				when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(forbiddenAlbum));

				List<Long> imageIds = List.of(1L);

				ApiException exception = assertThrows(ApiException.class,
					() -> albumService.deleteImageFromAlbum(USER_ID, ALBUM_ID, imageIds));

				assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
				verify(albumMapper, never()).deleteAlbumImageByAlbumImageId(anyLong());
			}

			@Test
			@DisplayName("실패 - 앨범 이미지가 앨범에 존재하지 않음")
			void testDeleteImageFromAlbum_ImageNotFoundInAlbum() {
				when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));
				when(albumMapper.findAlbumImageByAlbumIdAndImageId(ALBUM_ID, 1L)).thenReturn(Optional.empty());

				AlbumImageEditResponseDto response = albumService.deleteImageFromAlbum(USER_ID, ALBUM_ID, List.of(1L));

				assertEquals(ALBUM_ID, response.getAlbumId());
				assertEquals(0, response.getSucceededImageCount());
				assertEquals(1, response.getFailedImageCount());
				assertTrue(response.getFailedImageIds().containsKey(1L));
				verify(albumMapper, never()).deleteAlbumImageByAlbumImageId(anyLong());
			}
		}

		@Test
		@DisplayName("성공")
		void testAddImageIntoAlbum_Success() {
			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));
			when(imageMapper.findImageByImageId(1L)).thenReturn(Optional.of(image1));
			when(imageMapper.findImageByImageId(2L)).thenReturn(Optional.of(image2));
			when(imageMapper.findImageByImageId(3L)).thenReturn(Optional.of(image3));

			AlbumImageEditResponseDto response = albumService.addImageIntoAlbum(USER_ID, ALBUM_ID, IMAGE_IDS);

			assertEquals(ALBUM_ID, response.getAlbumId());
			assertEquals(3, response.getSucceededImageCount());
			assertTrue(response.getFailedImageIds().isEmpty());
			assertEquals(new HashSet<>(IMAGE_IDS), response.getSucceededImageIds());

			verify(albumMapper, times(3)).insertAlbumImage(any(AlbumImage.class));
		}

		@Test
		@DisplayName("실패 - DB에서 앨범 조회 실패")
		void testAddImageIntoAlbum_AlbumNotFound() {
			List<Long> imageIdList = List.of(1L);

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.addImageIntoAlbum(USER_ID, ALBUM_ID, imageIdList));

			assertEquals(ErrorCode.NOT_FOUND_ALBUM, exception.getErrorCode());

			verify(albumMapper, never()).insertAlbumImage(any(AlbumImage.class));
		}

		@Test
		@DisplayName("실패 - 유저가 앨범에 권한이 없을 때")
		void testAddImageIntoAlbum_ForbiddenAlbumAccess() {
			Album forbiddenAlbum = Album.builder().userId(2L).build();
			ReflectionTestUtils.setField(forbiddenAlbum, "albumId", 5L);

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(forbiddenAlbum));

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.addImageIntoAlbum(USER_ID, ALBUM_ID, IMAGE_IDS));

			assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());

			verify(albumMapper, never()).insertAlbumImage(any(AlbumImage.class));
		}

		@Test
		@DisplayName("실패 - 이미지를 찾지 못했을 때 실패 리스트에 추가되는지")
		void testAddImageIntoAlbum_ImageNotFound() {
			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));
			when(imageMapper.findImageByImageId(1L)).thenReturn(Optional.of(image1));
			when(imageMapper.findImageByImageId(2L)).thenReturn(Optional.of(image2));
			when(imageMapper.findImageByImageId(3L)).thenReturn(Optional.empty());

			AlbumImageEditResponseDto response = albumService.addImageIntoAlbum(USER_ID, ALBUM_ID, IMAGE_IDS);

			assertEquals(ALBUM_ID, response.getAlbumId());
			assertEquals(2, response.getSucceededImageCount());
			assertEquals(1, response.getFailedImageCount());
			assertEquals(Set.of(1L, 2L), response.getSucceededImageIds());
			assertEquals(1, response.getFailedImageIds().size());
			assertTrue(response.getFailedImageIds().containsKey(3L));

			verify(albumMapper, times(2)).insertAlbumImage(any(AlbumImage.class));
		}

		@Test
		@DisplayName("실패 - 유저가 이미지에 대한 권한이 없을 때 실패 리스트에 추가되는지")
		void testAddImageIntoAlbum_ForbiddenImageAccess() {
			Image forbiddenImage = Image.builder().userId(2L).build();
			ReflectionTestUtils.setField(forbiddenImage, "imageId", 5L);

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));
			when(imageMapper.findImageByImageId(5L)).thenReturn(Optional.of(forbiddenImage));

			AlbumImageEditResponseDto response = albumService.addImageIntoAlbum(USER_ID, ALBUM_ID, List.of(5L));

			assertEquals(ALBUM_ID, response.getAlbumId());
			assertEquals(0, response.getSucceededImageCount());
			assertEquals(1, response.getFailedImageCount());
			assertTrue(response.getSucceededImageIds().isEmpty());
			assertTrue(response.getFailedImageIds().containsKey(5L));

			verify(albumMapper, never()).insertAlbumImage(any(AlbumImage.class));
		}
	}

	@Nested
	@DisplayName("앨범 정보 수정 테스트")
	class UpdateAlbumInfoTest {
		@Test
		@DisplayName("성공")
		void testUpdateAlbumInfo_Success() {
			AlbumUpdateRequestDto requestDto = AlbumUpdateRequestDto.builder()
				.build();

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));

			doNothing().when(albumMapper).updateAlbumInfo(ALBUM_ID, requestDto);

			assertDoesNotThrow(() -> albumService.updateAlbumInfo(USER_ID, ALBUM_ID, requestDto));

			verify(albumMapper).updateAlbumInfo(ALBUM_ID, requestDto);
		}

		@Test
		@DisplayName("실패 - 앨범을 찾을 수 없음")
		void testUpdateAlbumInfo_AlbumNotFound() {
			AlbumUpdateRequestDto requestDto = AlbumUpdateRequestDto.builder()
				.build();

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.updateAlbumInfo(USER_ID, ALBUM_ID, requestDto));

			assertEquals(ErrorCode.NOT_FOUND_ALBUM, exception.getErrorCode());

			verify(albumMapper, never()).updateAlbumInfo(any(), any());
		}

		@Test
		@DisplayName("실패 - 유저가 앨범에 접근 권한이 없음")
		void testUpdateAlbumInfo_ForbiddenAccess() {
			AlbumUpdateRequestDto requestDto = AlbumUpdateRequestDto.builder()
				.build();

			Album forbiddenAlbum = Album.builder()
				.userId(2L)
				.build();
			ReflectionTestUtils.setField(forbiddenAlbum, "albumId", 5L);

			when(albumMapper.findByAlbumId(5L)).thenReturn(Optional.of(forbiddenAlbum));

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.updateAlbumInfo(USER_ID, 5L, requestDto));

			assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());

			verify(albumMapper, never()).updateAlbumInfo(any(), any());
		}
	}

	@Nested
	@DisplayName("앨범 목록 조회 테스트")
	class GetMyAlbumSimpleInfoTest {

		@Test
		@DisplayName("성공 - 사용자의 앨범 목록을 성공적으로 가져옴")
		void testGetMyAlbumSimpleInfo_Success() {
			String album1ThumbnailUrl = "https://s3.album1.thumbnailImage.jpg";
			String album2ThumbnailUrl = "https://s3.album2.thumbnailImage.jpg";

			Album album1 = Album.builder().userId(USER_ID).isPublic(1).build();
			ReflectionTestUtils.setField(album1, "albumId", 101L);
			ReflectionTestUtils.setField(album1, "albumTitle", "Album 1");
			ReflectionTestUtils.setField(album1, "thumbnailImageId", 1L);

			Album album2 = Album.builder().userId(USER_ID).isPublic(0).build();
			ReflectionTestUtils.setField(album2, "albumId", 102L);
			ReflectionTestUtils.setField(album2, "albumTitle", "Album 2");
			ReflectionTestUtils.setField(album2, "thumbnailImageId", 2L);

			when(albumMapper.findAlbumsByUserId(USER_ID)).thenReturn(List.of(album1, album2));
			when(albumMapper.findAlbumImagesByAlbumId(101L)).thenReturn(List.of(
				AlbumImage.builder().albumId(101L).imageId(1L).build(),
				AlbumImage.builder().albumId(101L).imageId(2L).build()
			));
			when(albumMapper.findAlbumImagesByAlbumId(102L)).thenReturn(List.of());
			when(imageService.fetchS3PresignedUrlByImageId(album1.getThumbnailImageId())).thenReturn(
				album1ThumbnailUrl);
			when(imageService.fetchS3PresignedUrlByImageId(album2.getThumbnailImageId())).thenReturn(
				album2ThumbnailUrl);

			MyAlbumResponseDto response = albumService.getMyAlbumSimpleInfo(USER_ID);

			assertNotNull(response);
			assertEquals(USER_ID, response.getUserId());
			assertEquals(2, response.getAlbumCount());
			assertEquals(2, response.getAlbumList().size());
			assertEquals(101L, response.getAlbumList().get(0).getAlbumId());
			assertEquals("Album 1", response.getAlbumList().get(0).getAlbumTitle());
			assertEquals(2, response.getAlbumList().get(0).getAlbumImageCount());
			assertEquals(1, response.getAlbumList().get(0).getIsPublic());
			assertEquals(album1ThumbnailUrl, response.getAlbumList().get(0).getAlbumThumbnailUrl());
			assertEquals(102L, response.getAlbumList().get(1).getAlbumId());
			assertEquals("Album 2", response.getAlbumList().get(1).getAlbumTitle());
			assertEquals(0, response.getAlbumList().get(1).getAlbumImageCount());
			assertEquals(0, response.getAlbumList().get(1).getIsPublic());
			assertEquals(album2ThumbnailUrl, response.getAlbumList().get(1).getAlbumThumbnailUrl());

			verify(albumMapper).findAlbumsByUserId(USER_ID);
			verify(albumMapper).findAlbumImagesByAlbumId(101L);
			verify(albumMapper).findAlbumImagesByAlbumId(102L);
		}

		@Test
		@DisplayName("성공 - 사용자가 가지고 있는 앨범이 없는 경우 빈 목록 반환")
		void testGetMyAlbumSimpleInfo_NoAlbums() {
			when(albumMapper.findAlbumsByUserId(USER_ID)).thenReturn(List.of());

			MyAlbumResponseDto response = albumService.getMyAlbumSimpleInfo(USER_ID);

			assertNotNull(response);
			assertEquals(USER_ID, response.getUserId());
			assertEquals(0, response.getAlbumCount());
			assertTrue(response.getAlbumList().isEmpty());

			verify(albumMapper).findAlbumsByUserId(USER_ID);
		}
	}

	@Nested
	@DisplayName("앨범 메타데이터 조회 테스트")
	class getAlbumMetaInfoTest {
		@Test
		void success() {
			AlbumImage albumImage1 = AlbumImage.builder().albumId(ALBUM_ID).imageId(1L).build();
			AlbumImage albumImage2 = AlbumImage.builder().albumId(ALBUM_ID).imageId(2L).build();
			List<AlbumImage> albumImages = List.of(albumImage1, albumImage2);

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));
			when(albumMapper.findAlbumImagesByAlbumId(ALBUM_ID)).thenReturn(albumImages);

			AlbumDetailedResponseDto result = albumService.getAlbumMetadata(USER_ID, ALBUM_ID);

			assertEquals(ALBUM_ID, result.getAlbumId());
			assertEquals(USER_ID, result.getCreatedUserId());
			assertEquals(1, result.getIsPublic());
			assertEquals(2, result.getImageCount());
		}
	}

	@Nested
	@DisplayName("앨범 이미지 조회 테스트")
	class getAlbumImagesTest {
		@Test
		@DisplayName("성공 - 앨범 이미지들이 정상적으로 조회됨")
		void testGetAlbumImages_Success() {
			int pageNum = 1;
			int pageSize = 10;

			AlbumImage albumImage1 = AlbumImage.builder().albumId(ALBUM_ID).imageId(1L).build();
			AlbumImage albumImage2 = AlbumImage.builder().albumId(ALBUM_ID).imageId(2L).build();
			AlbumImage albumImage3 = AlbumImage.builder().albumId(ALBUM_ID).imageId(3L).build();
			List<AlbumImage> albumImages = List.of(albumImage1, albumImage2, albumImage3);

			when(albumMapper.findAlbumImagesByAlbumId(ALBUM_ID)).thenReturn(albumImages);
			when(imageMapper.findImageByImageId(1L)).thenReturn(Optional.of(image1));
			when(imageMapper.findImageByImageId(2L)).thenReturn(Optional.of(image2));
			when(imageMapper.findImageByImageId(3L)).thenReturn(Optional.of(image3));

			when(imageService.convertImageToImageThumbnailDto(image1, USER_ID)).thenReturn(
				ImageThumbnailDto.builder()
					.imageId(1L)
					.thumbnailUrl("thumbnail1.jpg")
					.build()
			);
			when(imageService.convertImageToImageThumbnailDto(image2, USER_ID)).thenReturn(
				ImageThumbnailDto.builder()
					.imageId(2L)
					.thumbnailUrl("thumbnail2.jpg")
					.build()
			);
			when(imageService.convertImageToImageThumbnailDto(image3, USER_ID)).thenReturn(
				ImageThumbnailDto.builder()
					.imageId(3L)
					.thumbnailUrl("thumbnail3.jpg")
					.build()
			);

			PageInfo<ImageThumbnailDto> result =
				albumService.getAlbumImages(ALBUM_ID, USER_ID, pageNum, pageSize);

			// Then
			assertNotNull(result);
			assertEquals(3, result.getList().size());
			assertEquals(1L, result.getList().get(0).getImageId());
			assertEquals(2L, result.getList().get(1).getImageId());
			assertEquals(3L, result.getList().get(2).getImageId());

			verify(albumMapper).findAlbumImagesByAlbumId(ALBUM_ID);
			verify(imageMapper).findImageByImageId(1L);
			verify(imageMapper).findImageByImageId(2L);
			verify(imageMapper).findImageByImageId(3L);
			verify(imageService).convertImageToImageThumbnailDto(image1, USER_ID);
			verify(imageService).convertImageToImageThumbnailDto(image2, USER_ID);
			verify(imageService).convertImageToImageThumbnailDto(image3, USER_ID);
		}

		@Test
		@DisplayName("실패 - 앨범 이미지 중 일부 이미지를 찾을 수 없는 경우")
		void testGetAlbumImages_ImageNotFound() {
			// Given
			int pageNum = 1;
			int pageSize = 10;

			AlbumImage albumImage1 = AlbumImage.builder().albumId(ALBUM_ID).imageId(1L).build();
			AlbumImage albumImage2 = AlbumImage.builder().albumId(ALBUM_ID).imageId(2L).build();
			List<AlbumImage> albumImages = List.of(albumImage1, albumImage2);

			when(albumMapper.findAlbumImagesByAlbumId(ALBUM_ID)).thenReturn(albumImages);
			when(imageMapper.findImageByImageId(1L)).thenReturn(Optional.of(image1));
			when(imageMapper.findImageByImageId(2L)).thenReturn(Optional.empty()); // 이미지를 찾을 수 없음

			// When & Then
			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.getAlbumImages(ALBUM_ID, USER_ID, pageNum, pageSize));

			assertEquals(ErrorCode.NOT_FOUND_IMAGE, exception.getErrorCode());

			verify(albumMapper).findAlbumImagesByAlbumId(ALBUM_ID);
			verify(imageMapper).findImageByImageId(1L);
			verify(imageMapper).findImageByImageId(2L);
		}

		@Test
		@DisplayName("성공 - 앨범에 이미지가 없는 경우 빈 목록 반환")
		void testGetAlbumImages_EmptyAlbum() {
			// Given
			int pageNum = 1;
			int pageSize = 10;

			when(albumMapper.findAlbumImagesByAlbumId(ALBUM_ID)).thenReturn(List.of());

			// When
			PageInfo<ImageThumbnailDto> result =
				albumService.getAlbumImages(ALBUM_ID, USER_ID, pageNum, pageSize);

			// Then
			assertNotNull(result);
			assertTrue(result.getList().isEmpty());
			assertEquals(0, result.getList().size());

			verify(albumMapper).findAlbumImagesByAlbumId(ALBUM_ID);
			verify(imageMapper, never()).findImageByImageId(anyLong());
			verify(imageService, never()).convertImageToImageThumbnailDto(any(), any());
		}
	}

	@Nested
	@DisplayName("앨범 삭제 테스트")
	class DeleteAlbumTest {

		@Test
		@DisplayName("성공 - 앨범이 성공적으로 삭제됨")
		void testDeleteAlbum_Success() {
			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));
			doNothing().when(albumMapper).deleteAlbumByAlbumId(ALBUM_ID);

			assertDoesNotThrow(() -> albumService.deleteAlbum(USER_ID, ALBUM_ID));

			verify(albumMapper).findByAlbumId(ALBUM_ID);
			verify(albumMapper).deleteAlbumByAlbumId(ALBUM_ID);
		}

		@Test
		@DisplayName("실패 - 앨범을 찾을 수 없음")
		void testDeleteAlbum_AlbumNotFound() {
			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.deleteAlbum(USER_ID, ALBUM_ID));

			assertEquals(ErrorCode.NOT_FOUND_ALBUM, exception.getErrorCode());
			verify(albumMapper).findByAlbumId(ALBUM_ID);
			verify(albumMapper, never()).deleteAlbumByAlbumId(anyLong());
		}

		@Test
		@DisplayName("실패 - 유저가 앨범에 접근 권한이 없음")
		void testDeleteAlbum_ForbiddenAccess() {
			Album forbiddenAlbum = Album.builder().userId(2L).build();
			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(forbiddenAlbum));

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.deleteAlbum(USER_ID, ALBUM_ID));

			assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
			verify(albumMapper).findByAlbumId(ALBUM_ID);
			verify(albumMapper, never()).deleteAlbumByAlbumId(anyLong());
		}
	}
}