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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumCreateResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumDetailedResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageInsertResponseDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.image.service.ImageS3Service;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.service.LocationUtil;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {
	@Mock
	private AlbumMapper albumMapper;

	@Mock
	private ImageMapper imageMapper;

	@Mock
	private ImageS3Service imageS3Service;

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
				Album album = (Album) args[0];
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
		@Test
		@DisplayName("성공")
		void testAddImageIntoAlbum_Success() {
			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));
			when(imageMapper.findImageByImageId(1L)).thenReturn(Optional.of(image1));
			when(imageMapper.findImageByImageId(2L)).thenReturn(Optional.of(image2));
			when(imageMapper.findImageByImageId(3L)).thenReturn(Optional.of(image3));

			AlbumImageInsertResponseDto response = albumService.addImageIntoAlbum(USER_ID, ALBUM_ID, IMAGE_IDS);

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

			AlbumImageInsertResponseDto response = albumService.addImageIntoAlbum(USER_ID, ALBUM_ID, IMAGE_IDS);

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

			AlbumImageInsertResponseDto response = albumService.addImageIntoAlbum(USER_ID, ALBUM_ID, List.of(5L));

			assertEquals(ALBUM_ID, response.getAlbumId());
			assertEquals(0, response.getSucceededImageCount());
			assertEquals(1, response.getFailedImageCount());
			assertTrue(response.getSucceededImageIds().isEmpty());
			assertTrue(response.getFailedImageIds().containsKey(5L));

			verify(albumMapper, never()).insertAlbumImage(any(AlbumImage.class));
		}
	}

	@Nested
	@DisplayName("앨범 상세 정보 조회 테스트")
	class GetAlbumDetailedInfoTest {
		@Test
		@DisplayName("성공")
		void testGetAlbumDetailedInfo_Success() {
			CoordinatePoint mockCoordPoint = mock(CoordinatePoint.class);

			ReflectionTestUtils.setField(image1, "coordPoint", mockCoordPoint);
			ReflectionTestUtils.setField(image2, "coordPoint", mockCoordPoint);

			ReflectionTestUtils.setField(image1, "imageFile", "image1.jpg");
			ReflectionTestUtils.setField(image2, "imageFile", "image2.jpg");

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));
			when(albumMapper.findAlbumImagesByAlbumId(ALBUM_ID)).thenReturn(
				List.of(
					AlbumImage.builder().albumId(ALBUM_ID).imageId(1L).build(),
					AlbumImage.builder().albumId(ALBUM_ID).imageId(2L).build()
				)
			);

			when(imageMapper.findImageByImageId(1L)).thenReturn(Optional.of(image1));
			when(imageMapper.findImageByImageId(2L)).thenReturn(Optional.of(image2));

			when(imageS3Service.generatePreSignedGetUrl(anyString())).thenReturn("http://image-url.com/presigned");

			try (MockedStatic<LocationUtil> mockedLocationUtil = mockStatic(LocationUtil.class)) {
				mockedLocationUtil.when(() -> LocationUtil.getLocationInfoByCoordinatePoint(any(CoordinatePoint.class)))
					.thenReturn(new LocationInfoDto(37.497942, 127.027621, "서울특별시", "강남구"));

				AlbumDetailedResponseDto response = albumService.getAlbumDetailedInfo(USER_ID, ALBUM_ID);

				assertNotNull(response);
				assertEquals(album.getAlbumId(), response.getAlbumId());
				assertEquals(album.getAlbumTitle(), response.getAlbumTitle());
				assertEquals(2, response.getImageList().size());
				verify(albumMapper).findByAlbumId(ALBUM_ID);
				verify(albumMapper).findAlbumImagesByAlbumId(ALBUM_ID);
				verify(imageMapper, times(2)).findImageByImageId(anyLong());
				verify(imageS3Service, times(2)).generatePreSignedGetUrl(anyString());
			}
		}

		@Test
		@DisplayName("실패 - 앨범을 찾을 수 없음")
		void testGetAlbumDetailedInfo_AlbumNotFound() {
			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.getAlbumDetailedInfo(USER_ID, ALBUM_ID));

			assertEquals(ErrorCode.NOT_FOUND_ALBUM, exception.getErrorCode());
			verify(albumMapper).findByAlbumId(ALBUM_ID);
		}

		@Test
		@DisplayName("실패 - 유저가 앨범에 접근 권한이 없음")
		void testGetAlbumDetailedInfo_ForbiddenAccessToPrivateAlbum() {
			Album privateAlbum = Album.builder().userId(2L).isPublic(0).build();
			ReflectionTestUtils.setField(privateAlbum, "albumId", ALBUM_ID);

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(privateAlbum));

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.getAlbumDetailedInfo(USER_ID, ALBUM_ID));

			assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
			verify(albumMapper).findByAlbumId(ALBUM_ID);
		}
	}

	@Nested
	@DisplayName("앨범 정보 수정 테스트")
	class UpdateAlbumInfoTest {
		@Test
		@DisplayName("성공")
		void testUpdateAlbumInfo_Success() {
			AlbumUpdateRequestDto requestDto = AlbumUpdateRequestDto.builder()
				.albumId(ALBUM_ID)
				.build();

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(album));

			doNothing().when(albumMapper).updateAlbumInfo(requestDto);

			assertDoesNotThrow(() -> albumService.updateAlbumInfo(USER_ID, requestDto));

			verify(albumMapper).updateAlbumInfo(requestDto);
		}

		@Test
		@DisplayName("실패 - 앨범을 찾을 수 없음")
		void testUpdateAlbumInfo_AlbumNotFound() {
			AlbumUpdateRequestDto requestDto = AlbumUpdateRequestDto.builder()
				.albumId(ALBUM_ID)
				.build();

			when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.updateAlbumInfo(USER_ID, requestDto));

			assertEquals(ErrorCode.NOT_FOUND_ALBUM, exception.getErrorCode());

			verify(albumMapper, never()).updateAlbumInfo(any());
		}

		@Test
		@DisplayName("실패 - 유저가 앨범에 접근 권한이 없음")
		void testUpdateAlbumInfo_ForbiddenAccess() {
			AlbumUpdateRequestDto requestDto = AlbumUpdateRequestDto.builder()
				.albumId(5L)
				.build();

			Album forbiddenAlbum = Album.builder()
				.userId(2L)
				.build();
			ReflectionTestUtils.setField(forbiddenAlbum, "albumId", 5L);

			when(albumMapper.findByAlbumId(5L)).thenReturn(Optional.of(forbiddenAlbum));

			ApiException exception = assertThrows(ApiException.class,
				() -> albumService.updateAlbumInfo(USER_ID, requestDto));

			assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());

			verify(albumMapper, never()).updateAlbumInfo(any());
		}
	}
}