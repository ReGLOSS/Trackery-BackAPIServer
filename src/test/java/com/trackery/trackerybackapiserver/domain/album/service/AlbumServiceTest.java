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
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.MyAlbumResponseDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;
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
	private ImageService imageService;

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

				ApiException exception = assertThrows(ApiException.class,
					() -> albumService.deleteImageFromAlbum(USER_ID, ALBUM_ID, List.of(1L)));

				assertEquals(ErrorCode.NOT_FOUND_ALBUM, exception.getErrorCode());
				verify(albumMapper, never()).deleteAlbumImageByAlbumImageId(anyLong());
			}

			@Test
			@DisplayName("실패 - 유저가 앨범에 접근 권한이 없음")
			void testDeleteImageFromAlbum_ForbiddenAccess() {
				Album forbiddenAlbum = Album.builder().userId(2L).build();
				when(albumMapper.findByAlbumId(ALBUM_ID)).thenReturn(Optional.of(forbiddenAlbum));

				ApiException exception = assertThrows(ApiException.class,
					() -> albumService.deleteImageFromAlbum(USER_ID, ALBUM_ID, List.of(1L)));

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
	@DisplayName("getMyAlbumSimpleInfo 메소드 테스트")
	class GetMyAlbumSimpleInfoTest {

		@Test
		@DisplayName("성공 - 사용자의 앨범 목록을 성공적으로 가져옴")
		void testGetMyAlbumSimpleInfo_Success() {
			Album album1 = Album.builder().userId(USER_ID).isPublic(1).build();
			ReflectionTestUtils.setField(album1, "albumId", 101L);
			ReflectionTestUtils.setField(album1, "albumTitle", "Album 1");

			Album album2 = Album.builder().userId(USER_ID).isPublic(0).build();
			ReflectionTestUtils.setField(album2, "albumId", 102L);
			ReflectionTestUtils.setField(album2, "albumTitle", "Album 2");

			when(albumMapper.findAlbumsByUserId(USER_ID)).thenReturn(List.of(album1, album2));
			when(albumMapper.findAlbumImagesByAlbumId(101L)).thenReturn(List.of(
				AlbumImage.builder().albumId(101L).imageId(1L).build(),
				AlbumImage.builder().albumId(101L).imageId(2L).build()
			));
			when(albumMapper.findAlbumImagesByAlbumId(102L)).thenReturn(List.of());

			MyAlbumResponseDto response = albumService.getMyAlbumSimpleInfo(USER_ID);

			assertNotNull(response);
			assertEquals(USER_ID, response.getUserId());
			assertEquals(2, response.getAlbumCount());
			assertEquals(2, response.getAlbumList().size());
			assertEquals(101L, response.getAlbumList().get(0).getAlbumId());
			assertEquals("Album 1", response.getAlbumList().get(0).getAlbumTitle());
			assertEquals(2, response.getAlbumList().get(0).getAlbumImageCount());
			assertEquals(1, response.getAlbumList().get(0).getIsPublic());
			assertEquals(102L, response.getAlbumList().get(1).getAlbumId());
			assertEquals("Album 2", response.getAlbumList().get(1).getAlbumTitle());
			assertEquals(0, response.getAlbumList().get(1).getAlbumImageCount());
			assertEquals(0, response.getAlbumList().get(1).getIsPublic());

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