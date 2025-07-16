package com.trackery.trackerybackapiserver.domain.location.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateRequestDto;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationNameResponseDto;
import com.trackery.trackerybackapiserver.domain.location.dto.MapResponseDto;
import com.trackery.trackerybackapiserver.domain.location.dto.UserStatsDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;
import com.trackery.trackerybackapiserver.domain.location.mapper.LocationMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.service
 * fileName       : LocationServiceTest
 * author         : durururuk
 * date           : 25. 4. 23.
 * description    : LocationService 테스트 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 23.		durururuk		최초 생성
 * 25. 4. 23.		durururuk		LocationService Mock 단위 테스트 작성
 * 25. 4. 24.		durururuk		ImageUploadController MockMvc 단위테스트 코드 작성
 * 25. 6. 13.		inari		location에 추가된 테스트 코드 추가
 * 25. 6. 16.		inari		state에서 시도 제거
 * 25. 6. 22.		inari		이미지 메타데이터 수정시 좌표 인서트가 아닌 업데이트로 변경
 * 25. 7. 11.		inari		test코드 추가
 * 25. 7. 14.		inari		테스트코드 수정 및 adoc 변경
 * 25. 7. 15.		inari		테스트 코드 작성 및 문서 수정
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceTest {
	@InjectMocks
	private LocationService locationService;
	@Mock
	private LocationMapper locationMapper;

	@Nested
	@DisplayName("좌표로 주소명 가져오기 테스트")
	class getLocationNameByCoordTest {
		CoordinateDto coordinateDto;
		JusoSido sido;
		JusoSigungu sigungu;

		@BeforeEach
		void setUp() {
			coordinateDto = new CoordinateDto(37.5665, 126.9780);
			sido = new JusoSido();
			ReflectionTestUtils.setField(sido, "sidoName", "서울특별시");
			ReflectionTestUtils.setField(sido, "sidoId", 11L);

			sigungu = new JusoSigungu();
			ReflectionTestUtils.setField(sigungu, "sigunguName", "동작구");
			ReflectionTestUtils.setField(sigungu, "sigunguId", 11200L);
			ReflectionTestUtils.setField(sigungu, "sido", sido);
		}

		@Test
		@DisplayName("성공")
		void success() {
			when(locationMapper.findSigunguByCoordinate(coordinateDto)).thenReturn(Optional.of(sigungu));

			LocationNameResponseDto result = locationService.getLocationNameByCoord(coordinateDto);

			assertEquals("서울특별시", result.getSdName());
			assertEquals("동작구", result.getSggName());
			verify(locationMapper, times(1)).findSigunguByCoordinate(coordinateDto);
		}

		@Test
		@DisplayName("실패 - 좌표로 주소를 찾지 못했을 경우")
		void testGetLocationNameByCoord_NotFound() {
			when(locationMapper.findSigunguByCoordinate(coordinateDto)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class,
				() -> locationService.getLocationNameByCoord(coordinateDto));

			assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
			verify(locationMapper, times(1)).findSigunguByCoordinate(coordinateDto);
		}
	}

	@Nested
	@DisplayName("Point 객체로 시군구 엔티티 반환 테스트")
	class getSigunguByPointTest {
		Point point;

		@BeforeEach
		void setUp() {
			point = mock(Point.class);
		}

		@Test
		@DisplayName("성공")
		void success() {
			when(locationMapper.findSigunguByPoint(point)).thenReturn(Optional.of(new JusoSigungu()));

			JusoSigungu sigungu = locationService.getSigunguByPoint(point);

			verify(locationMapper, times(1)).findSigunguByPoint(point);
		}

		@Test
		@DisplayName("실패 - 포인트 객체로 위치를 찾지 못했을 경우")
		void failure_1() {
			when(locationMapper.findSigunguByPoint(point)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class, () -> locationService.getSigunguByPoint(point));

			assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("CoordinatePoint 삽입 테스트")
	class insertCoordinatePointTest {
		Point point;
		JusoSigungu sigungu;
		JusoSido sido;

		@BeforeEach
		void setUp() {
			point = mock(Point.class);
			sigungu = mock(JusoSigungu.class);
			sido = mock(JusoSido.class);
		}

		@Test
		@DisplayName("성공")
		void success() {
			when(locationMapper.findSigunguByPoint(any(Point.class))).thenReturn(Optional.of(sigungu));
			when(sigungu.getSigunguName()).thenReturn("동작구");
			when(sigungu.getSido()).thenReturn(sido);
			when(sido.getSidoName()).thenReturn("서울특별시");

			doNothing().when(locationMapper).insertCoordinatePoint(any(CoordinatePoint.class));

			CoordinatePoint coordinatePoint = locationService.insertCoordinatePoint(new CoordinateDto(37.5665, 126.9780));

			verify(locationMapper, times(1)).insertCoordinatePoint(any(CoordinatePoint.class));

			assertEquals("서울특별시 동작구", coordinatePoint.getCoordinatePointName());
		}
	}

	@Nested
	@DisplayName("CoordinatePoint 업데이트 테스트")
	class updateCoordinatePointTest {
		Long coordinatePointId;
		CoordinateDto coordinateDto;
		Point point;
		JusoSigungu sigungu;
		JusoSido sido;

		@BeforeEach
		void setUp() {
			coordinatePointId = 1L;
			coordinateDto = new CoordinateDto(37.5665, 126.9780);
			point = mock(Point.class);
			sigungu = mock(JusoSigungu.class);
			sido = mock(JusoSido.class);
		}

		@Test
		@DisplayName("성공 - 좌표 포인트 업데이트 성공")
		void success() {
			when(locationMapper.findSigunguByPoint(any(Point.class))).thenReturn(Optional.of(sigungu));
			when(sigungu.getSigunguName()).thenReturn("강남구");
			when(sigungu.getSido()).thenReturn(sido);
			when(sido.getSidoName()).thenReturn("서울특별시");
			when(sigungu.getSigunguId()).thenReturn(11680L);
			when(locationMapper.updateCoordinatePointById(eq(coordinatePointId), eq("서울특별시 강남구"), 
				any(Point.class), eq(11680L), any(LocalDateTime.class))).thenReturn(1);

			int result = locationService.updateCoordinatePoint(coordinatePointId, coordinateDto);

			assertEquals(1, result);
			verify(locationMapper, times(1)).findSigunguByPoint(any(Point.class));
			verify(locationMapper, times(1)).updateCoordinatePointById(eq(coordinatePointId), eq("서울특별시 강남구"), 
				any(Point.class), eq(11680L), any(LocalDateTime.class));
		}

		@Test
		@DisplayName("실패 - 좌표로 시군구를 찾을 수 없는 경우")
		void failureNotFoundSigungu() {
			when(locationMapper.findSigunguByPoint(any(Point.class))).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class,
				() -> locationService.updateCoordinatePoint(coordinatePointId, coordinateDto));

			assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
			verify(locationMapper, times(1)).findSigunguByPoint(any(Point.class));
			verify(locationMapper, never()).updateCoordinatePointById(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("성공 - 업데이트된 행이 0개인 경우")
		void successZeroRowsUpdated() {
			when(locationMapper.findSigunguByPoint(any(Point.class))).thenReturn(Optional.of(sigungu));
			when(sigungu.getSigunguName()).thenReturn("동작구");
			when(sigungu.getSido()).thenReturn(sido);
			when(sido.getSidoName()).thenReturn("서울특별시");
			when(sigungu.getSigunguId()).thenReturn(11200L);
			when(locationMapper.updateCoordinatePointById(eq(coordinatePointId), eq("서울특별시 동작구"), 
				any(Point.class), eq(11200L), any(LocalDateTime.class))).thenReturn(0);

			int result = locationService.updateCoordinatePoint(coordinatePointId, coordinateDto);

			assertEquals(0, result);
			verify(locationMapper, times(1)).updateCoordinatePointById(eq(coordinatePointId), eq("서울특별시 동작구"), 
				any(Point.class), eq(11200L), any(LocalDateTime.class));
		}
	}

	@Nested
	@DisplayName("전체 시도 목록 조회 테스트")
	class getAllSidoTest {
		@Test
		@DisplayName("성공 - 모든 시도 목록 반환")
		void success() {
			JusoSido seoul = createSido(11L, "서울특별시");
			JusoSido busan = createSido(26L, "부산광역시");
			JusoSido gyeonggi = createSido(41L, "경기도");
			List<JusoSido> expectedSidos = Arrays.asList(seoul, busan, gyeonggi);

			when(locationMapper.findAllSido()).thenReturn(expectedSidos);

			List<JusoSido> result = locationService.getAllSido();

			assertNotNull(result);
			assertEquals(3, result.size());
			assertEquals("서울특별시", result.get(0).getSidoName());
			assertEquals("부산광역시", result.get(1).getSidoName());
			assertEquals("경기도", result.get(2).getSidoName());
			verify(locationMapper, times(1)).findAllSido();
		}

		@Test
		@DisplayName("성공 - 빈 목록 반환")
		void emptyList() {
			when(locationMapper.findAllSido()).thenReturn(Collections.emptyList());

			List<JusoSido> result = locationService.getAllSido();

			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(locationMapper, times(1)).findAllSido();
		}
	}

	@Nested
	@DisplayName("시도별 시군구 목록 조회 테스트")
	class getSigunguBySidoTest {
		@Test
		@DisplayName("성공 - 해당 시도의 시군구 목록 반환")
		void success() {
			Long sidoId = 11L;
			JusoSido seoul = createSido(sidoId, "서울특별시");
			JusoSigungu gangnam = createSigungu(11680L, "강남구", seoul);
			JusoSigungu dongjak = createSigungu(11200L, "동작구", seoul);
			List<JusoSigungu> expectedSigungus = Arrays.asList(gangnam, dongjak);

			when(locationMapper.findSigunguBySido(sidoId)).thenReturn(expectedSigungus);

			List<JusoSigungu> result = locationService.getSigunguBySido(sidoId);

			assertNotNull(result);
			assertEquals(2, result.size());
			assertEquals("강남구", result.get(0).getSigunguName());
			assertEquals("동작구", result.get(1).getSigunguName());
			verify(locationMapper, times(1)).findSigunguBySido(sidoId);
		}

		@Test
		@DisplayName("성공 - 해당 시도에 시군구가 없는 경우 빈 목록 반환")
		void emptyList() {
			Long sidoId = 99L;
			when(locationMapper.findSigunguBySido(sidoId)).thenReturn(Collections.emptyList());

			List<JusoSigungu> result = locationService.getSigunguBySido(sidoId);

			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(locationMapper, times(1)).findSigunguBySido(sidoId);
		}
	}

	@Nested
	@DisplayName("좌표로 시군구 조회 (DTO 반환) 테스트")
	class getSigunguByCoordinateDtoTest {
		@Test
		@DisplayName("성공 - 좌표에 해당하는 시군구 정보 DTO 반환")
		void success() {
			CoordinateRequestDto request = new CoordinateRequestDto(37.5665, 126.9780);
			JusoSido seoul = createSido(11L, "서울특별시");
			JusoSigungu dongjak = createSigungu(11200L, "동작구", seoul);

			when(locationMapper.findSigunguByCoordinateRequest(request)).thenReturn(dongjak);

			MapResponseDto result = locationService.getSigunguByCoordinateDto(request);

			assertNotNull(result);
			assertEquals("서울특별시", result.getSidoName());
			assertEquals("동작구", result.getSigunguName());
			assertEquals(11L, result.getSidoId());
			assertEquals(11200L, result.getSigunguId());
			verify(locationMapper, times(1)).findSigunguByCoordinateRequest(request);
		}

		@Test
		@DisplayName("실패 - 좌표에 해당하는 시군구를 찾을 수 없는 경우")
		void notFound() {
			CoordinateRequestDto request = new CoordinateRequestDto(0.0, 0.0);
			when(locationMapper.findSigunguByCoordinateRequest(request)).thenReturn(null);

			ApiException exception = assertThrows(ApiException.class,
				() -> locationService.getSigunguByCoordinateDto(request));

			assertEquals(ErrorCode.NOT_FOUND_SIGUNGU, exception.getErrorCode());
			verify(locationMapper, times(1)).findSigunguByCoordinateRequest(request);
		}
	}

	@Nested
	@DisplayName("시도 경계선 GeoJSON 조회 테스트")
	class getSidoBorderAsGeoJsonTest {
		@Test
		@DisplayName("성공 - 시도 경계선 GeoJSON 반환")
		void success() {
			Long sidoId = 11L;
			String expectedGeoJson = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[126.734086,37.413294]]]}}";

			when(locationMapper.findSidoBorderAsGeoJson(sidoId)).thenReturn(expectedGeoJson);

			String result = locationService.getSidoBorderAsGeoJson(sidoId);

			assertNotNull(result);
			assertEquals(expectedGeoJson, result);
			verify(locationMapper, times(1)).findSidoBorderAsGeoJson(sidoId);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 시도 ID")
		void notFound() {
			Long sidoId = 99L;
			when(locationMapper.findSidoBorderAsGeoJson(sidoId)).thenReturn(null);

			ApiException exception = assertThrows(ApiException.class,
				() -> locationService.getSidoBorderAsGeoJson(sidoId));

			assertEquals(ErrorCode.NOT_FOUND_SIDO, exception.getErrorCode());
			verify(locationMapper, times(1)).findSidoBorderAsGeoJson(sidoId);
		}
	}

	@Nested
	@DisplayName("시군구 경계선 GeoJSON 조회 테스트")
	class getSigunguBorderAsGeoJsonTest {
		@Test
		@DisplayName("성공 - 시군구 경계선 GeoJSON 반환")
		void success() {
			Long sigunguId = 11200L;
			String expectedGeoJson = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[126.734086,37.413294]]]}}";

			when(locationMapper.findSigunguBorderAsGeoJson(sigunguId)).thenReturn(expectedGeoJson);

			String result = locationService.getSigunguBorderAsGeoJson(sigunguId);

			assertNotNull(result);
			assertEquals(expectedGeoJson, result);
			verify(locationMapper, times(1)).findSigunguBorderAsGeoJson(sigunguId);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 시군구 ID")
		void notFound() {
			Long sigunguId = 99999L;
			when(locationMapper.findSigunguBorderAsGeoJson(sigunguId)).thenReturn(null);

			ApiException exception = assertThrows(ApiException.class,
				() -> locationService.getSigunguBorderAsGeoJson(sigunguId));

			assertEquals(ErrorCode.NOT_FOUND_SIGUNGU, exception.getErrorCode());
			verify(locationMapper, times(1)).findSigunguBorderAsGeoJson(sigunguId);
		}
	}

	@Nested
	@DisplayName("시군구 ID로 시군구 정보 조회 테스트")
	class getSigunguByIdTest {
		@Test
		@DisplayName("성공 - 시군구 ID로 시군구 정보 반환")
		void success() {
			Long sigunguId = 11200L;
			JusoSido seoul = createSido(11L, "서울특별시");
			JusoSigungu dongjak = createSigungu(sigunguId, "동작구", seoul);

			when(locationMapper.findSigunguById(sigunguId)).thenReturn(Optional.of(dongjak));

			JusoSigungu result = locationService.getSigunguById(sigunguId);

			assertNotNull(result);
			assertEquals("동작구", result.getSigunguName());
			assertEquals(sigunguId, result.getSigunguId());
			assertEquals("서울특별시", result.getSido().getSidoName());
			assertEquals(11L, result.getSido().getSidoId());
			verify(locationMapper, times(1)).findSigunguById(sigunguId);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 시군구 ID")
		void notFound() {
			Long sigunguId = 99999L;
			when(locationMapper.findSigunguById(sigunguId)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class,
				() -> locationService.getSigunguById(sigunguId));

			assertEquals(ErrorCode.NOT_FOUND_SIGUNGU, exception.getErrorCode());
			verify(locationMapper, times(1)).findSigunguById(sigunguId);
		}
	}

	@Nested
	@DisplayName("좌표로 시군구 조회 테스트")
	class findSigunguByCoordinateTest {
		@Test
		@DisplayName("성공 - 좌표로 시군구 정보 반환")
		void success() {
			double latitude = 37.5665;
			double longitude = 126.9780;
			JusoSido seoul = createSido(11L, "서울특별시");
			JusoSigungu dongjak = createSigungu(11200L, "동작구", seoul);

			when(locationMapper.findSigunguByPoint(any(Point.class))).thenReturn(Optional.of(dongjak));

			JusoSigungu result = locationService.findSigunguByCoordinate(latitude, longitude);

			assertNotNull(result);
			assertEquals("동작구", result.getSigunguName());
			assertEquals(11200L, result.getSigunguId());
			assertEquals("서울특별시", result.getSido().getSidoName());
			assertEquals(11L, result.getSido().getSidoId());
			verify(locationMapper, times(1)).findSigunguByPoint(any(Point.class));
		}

		@Test
		@DisplayName("실패 - 좌표로 시군구를 찾을 수 없는 경우 null 반환")
		void notFound() {
			double latitude = 0.0;
			double longitude = 0.0;

			when(locationMapper.findSigunguByPoint(any(Point.class))).thenReturn(Optional.empty());

			JusoSigungu result = locationService.findSigunguByCoordinate(latitude, longitude);

			assertNull(result);
			verify(locationMapper, times(1)).findSigunguByPoint(any(Point.class));
		}
	}

	@Nested
	@DisplayName("시군구 ID로 위치 정보 조회 테스트")
	class getSigunguLocationInfoByIdTest {
		@Test
		@DisplayName("성공 - 시군구 ID로 위치 정보 반환")
		void success() {
			Long sigunguId = 11200L;
			JusoSido seoul = createSido(11L, "서울특별시");
			JusoSigungu dongjak = createSigungu(sigunguId, "동작구", seoul);

			when(locationMapper.findSigunguById(sigunguId)).thenReturn(Optional.of(dongjak));

			LocationInfoDto result = locationService.getSigunguLocationInfoById(sigunguId);

			assertNotNull(result);
			assertEquals(0.0, result.latitude());
			assertEquals(0.0, result.longitude());
			assertEquals("서울특별시", result.sidoName());
			assertEquals("동작구", result.sigunguName());
			verify(locationMapper, times(1)).findSigunguById(sigunguId);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 시군구 ID")
		void notFound() {
			Long sigunguId = 99999L;
			when(locationMapper.findSigunguById(sigunguId)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(ApiException.class,
				() -> locationService.getSigunguLocationInfoById(sigunguId));

			assertEquals(ErrorCode.NOT_FOUND_SIGUNGU, exception.getErrorCode());
			verify(locationMapper, times(1)).findSigunguById(sigunguId);
		}
	}

	private JusoSido createSido(Long sidoId, String sidoName) {
		JusoSido sido = new JusoSido();
		sido.setSidoId(sidoId);
		sido.setSidoName(sidoName);
		return sido;
	}

	private JusoSigungu createSigungu(Long sigunguId, String sigunguName, JusoSido sido) {
		JusoSigungu sigungu = new JusoSigungu();
		sigungu.setSigunguId(sigunguId);
		sigungu.setSigunguName(sigunguName);
		sigungu.setSido(sido);
		return sigungu;
	}

	@Nested
	@DisplayName("이미지 위치 정보 업데이트 테스트")
	class updateImageLocationTest {
		@Test
		@DisplayName("성공 - 위치 정보 수정")
		void success() {
			Long coordinatePointId = 1L;
			Double latitude = 37.5665;
			Double longitude = 126.9780;
			Long imageId = 1L;
			
			JusoSido seoul = createSido(11L, "서울특별시");
			JusoSigungu dongjak = createSigungu(11200L, "동작구", seoul);
			
			when(locationMapper.findSigunguByPoint(any(Point.class))).thenReturn(Optional.of(dongjak));
			when(locationMapper.updateCoordinatePointById(eq(coordinatePointId), eq("서울특별시 동작구"),
				any(Point.class), eq(11200L), any(LocalDateTime.class))).thenReturn(1);
			
			assertDoesNotThrow(() -> locationService.updateImageLocation(coordinatePointId, latitude, longitude, imageId));
			
			verify(locationMapper, times(1)).findSigunguByPoint(any(Point.class));
			verify(locationMapper, times(1)).updateCoordinatePointById(eq(coordinatePointId), eq("서울특별시 동작구"),
				any(Point.class), eq(11200L), any(LocalDateTime.class));
		}

		@Test
		@DisplayName("성공 - latitude가 null인 경우 처리하지 않음")
		void skipWhenLatitudeIsNull() {
			Long coordinatePointId = 1L;
			Double latitude = null;
			Double longitude = 126.9780;
			Long imageId = 1L;
			
			assertDoesNotThrow(() -> locationService.updateImageLocation(coordinatePointId, latitude, longitude, imageId));
			
			verify(locationMapper, never()).findSigunguByPoint(any(Point.class));
			verify(locationMapper, never()).updateCoordinatePointById(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("성공 - longitude가 null인 경우 처리하지 않음")
		void skipWhenLongitudeIsNull() {
			Long coordinatePointId = 1L;
			Double latitude = 37.5665;
			Double longitude = null;
			Long imageId = 1L;
			
			assertDoesNotThrow(() -> locationService.updateImageLocation(coordinatePointId, latitude, longitude, imageId));
			
			verify(locationMapper, never()).findSigunguByPoint(any(Point.class));
			verify(locationMapper, never()).updateCoordinatePointById(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("실패 - 좌표 업데이트 실패 시 ApiException 발생")
		void failureWhenUpdateFails() {
			Long coordinatePointId = 1L;
			Double latitude = 37.5665;
			Double longitude = 126.9780;
			Long imageId = 1L;
			
			when(locationMapper.findSigunguByPoint(any(Point.class))).thenThrow(new RuntimeException("Database error"));
			
			ApiException exception = assertThrows(ApiException.class,
				() -> locationService.updateImageLocation(coordinatePointId, latitude, longitude, imageId));
			
			assertEquals(ErrorCode.UPDATE_FAILED_LOCATION, exception.getErrorCode());
			verify(locationMapper, times(1)).findSigunguByPoint(any(Point.class));
		}
	}

	@Nested
	@DisplayName("사용자 통계 조회 테스트")
	class getUserStatsTest {
		@Test
		@DisplayName("성공 - 사용자 통계 데이터 반환")
		void success() {
			Long userId = 1L;
			UserStatsDto expectedStats = new UserStatsDto(10L, 5L, 3L);

			when(locationMapper.getUserStats(userId)).thenReturn(expectedStats);

			UserStatsDto result = locationService.getUserStats(userId);

			assertNotNull(result);
			assertEquals(10L, result.getImageCount());
			assertEquals(5L, result.getAlbumCount());
			assertEquals(3L, result.getSigunguCount());
			verify(locationMapper, times(1)).getUserStats(userId);
		}

		@Test
		@DisplayName("성공 - 모든 통계가 0인 경우")
		void zeroStats() {
			Long userId = 2L;
			UserStatsDto expectedStats = new UserStatsDto(0L, 0L, 0L);

			when(locationMapper.getUserStats(userId)).thenReturn(expectedStats);

			UserStatsDto result = locationService.getUserStats(userId);

			assertNotNull(result);
			assertEquals(0L, result.getImageCount());
			assertEquals(0L, result.getAlbumCount());
			assertEquals(0L, result.getSigunguCount());
			verify(locationMapper, times(1)).getUserStats(userId);
		}
	}
}
