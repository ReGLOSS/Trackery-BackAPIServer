package com.trackery.trackerybackapiserver.domain.location.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;
import com.trackery.trackerybackapiserver.domain.location.mapper.LocationMapper;

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

			String locationName = locationService.getLocationNameByCoord(coordinateDto);

			assertEquals("서울특별시 동작구", locationName);
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

}