package com.trackery.trackerybackapiserver.domain.location.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateRequestDto;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationNameResponseDto;
import com.trackery.trackerybackapiserver.domain.location.dto.MapResponseDto;
import com.trackery.trackerybackapiserver.domain.location.dto.UserStatsDto;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.controller
 * fileName       : LocationControllerTest
 * author         : durururuk
 * date           : 25. 4. 23.
 * description    : LocationController 테스트 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 23.		durururuk		최초 생성
 * 25. 6. 13.		inari			테스트 추가
 * 25. 6. 15.		inari			Spring-Rest-Docs api문서 추가
 * 25. 7. 11.		inari			태그 제거
 * 25. 7. 14.       inari       	테스트 코드 수정
 */
@WebMvcTest(LocationController.class)
public class LocationControllerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	LocationService locationService;

	@MockitoBean
	ImageService imageService;

	CustomUserDetails customUserDetails;

	@BeforeEach
	void setUp() {
		customUserDetails = CustomUserDetails.builder().userId(1L).userName("abcdefg").roleId(1L).build();
	}

	@Nested
	@DisplayName("좌표 -> 주소명 api 테스트")
	class getLocationNameTest {
		@Test
		@DisplayName("좌표로 주소명 조회 성공")
		void success() throws Exception {
			CoordinateDto coordinateDto = new CoordinateDto(37.5665, 126.9780);

			LocationNameResponseDto locationResponse = LocationNameResponseDto.builder()
				.sdName("서울특별시")
				.sggName("동작구")
				.build();

			when(locationService.getLocationNameByCoord(coordinateDto)).thenReturn(locationResponse);

			ResultActions result = mockMvc.perform(post("/api/location/name")
				.with(user(customUserDetails))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(coordinateDto)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.sdName").value("서울특별시"))
				.andExpect(jsonPath("$.data.sggName").value("동작구"));

			result.andDo(document("get-location-name-by-coordinate-success",
				requestFields(
					fieldWithPath("latitude").description("위도").type(JsonFieldType.NUMBER),
					fieldWithPath("longitude").description("경도").type(JsonFieldType.NUMBER)
				),
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.sdName").description("시도명"),
					fieldWithPath("data.sggName").description("시군구명")
				)
			));
		}
	}

	@Nested
	@DisplayName("전체 시도 목록 조회 API 테스트")
	class getAllSidoTest {
		@Test
		@DisplayName("전체 시도 목록 조회 성공")
		void success() throws Exception {
			JusoSido seoul = createSido(11L, "서울특별시");
			JusoSido busan = createSido(26L, "부산광역시");
			List<JusoSido> sidoList = Arrays.asList(seoul, busan);

			when(locationService.getAllSido()).thenReturn(sidoList);

			ResultActions result = mockMvc.perform(get("/api/location/sido")
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].sidoId").value(11L))
				.andExpect(jsonPath("$.data[0].sidoName").value("서울특별시"))
				.andExpect(jsonPath("$.data[1].sidoId").value(26L))
				.andExpect(jsonPath("$.data[1].sidoName").value("부산광역시"));

			result.andDo(document("get-all-sido-success",
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("시도 목록").type(JsonFieldType.ARRAY),
					fieldWithPath("data[].sidoId").description("시도 ID"),
					fieldWithPath("data[].sidoName").description("시도명")
				)
			));
		}
	}

	@Nested
	@DisplayName("시도별 시군구 목록 조회 API 테스트")
	class getSigunguBySidoTest {
		@Test
		@DisplayName("시도별 시군구 목록 조회 성공")
		void success() throws Exception {
			JusoSido seoul = createSido(11L, "서울특별시");
			JusoSigungu gangnam = createSigungu(11680L, "강남구", seoul);
			JusoSigungu dongjak = createSigungu(11200L, "동작구", seoul);
			List<JusoSigungu> sigunguList = Arrays.asList(gangnam, dongjak);

			when(locationService.getSigunguBySido(11L)).thenReturn(sigunguList);

			ResultActions result = mockMvc.perform(get("/api/location/sido/{sidoId}/sigungu", 11L)
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].sigunguId").value(11680L))
				.andExpect(jsonPath("$.data[0].sigunguName").value("강남구"))
				.andExpect(jsonPath("$.data[1].sigunguId").value(11200L))
				.andExpect(jsonPath("$.data[1].sigunguName").value("동작구"));

			result.andDo(document("get-sigungu-by-sido-success",
				pathParameters(
					parameterWithName("sidoId").description("시도 ID")
				),
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("시군구 목록").type(JsonFieldType.ARRAY),
					fieldWithPath("data[].sigunguId").description("시군구 ID"),
					fieldWithPath("data[].sigunguName").description("시군구명"),
					fieldWithPath("data[].sido.sidoId").description("시도 ID"),
					fieldWithPath("data[].sido.sidoName").description("시도명")
				)
			));
		}
	}

	@Nested
	@DisplayName("좌표로 시군구 조회 API 테스트")
	class getSigunguByCoordinateTest {
		@Test
		@DisplayName("좌표로 시군구 조회 성공")
		void success() throws Exception {
			CoordinateRequestDto request = new CoordinateRequestDto(37.5665, 126.9780);
			MapResponseDto response = MapResponseDto.builder()
				.sidoName("서울특별시")
				.sigunguName("동작구")
				.sidoId(11L)
				.sigunguId(11200L)
				.build();

			when(locationService.getSigunguByCoordinateDto(any(CoordinateRequestDto.class))).thenReturn(response);

			ResultActions result = mockMvc.perform(post("/api/location/coordinate/sigungu")
				.with(user(customUserDetails))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.sidoName").value("서울특별시"))
				.andExpect(jsonPath("$.data.sigunguName").value("동작구"))
				.andExpect(jsonPath("$.data.sidoId").value(11L))
				.andExpect(jsonPath("$.data.sigunguId").value(11200L));

			result.andDo(document("get-sigungu-by-coordinate-success",
				requestFields(
					fieldWithPath("latitude").description("위도").type(JsonFieldType.NUMBER),
					fieldWithPath("longitude").description("경도").type(JsonFieldType.NUMBER)
				),
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.sidoName").description("시도명"),
					fieldWithPath("data.sigunguName").description("시군구명"),
					fieldWithPath("data.sidoId").description("시도 ID"),
					fieldWithPath("data.sigunguId").description("시군구 ID")
				)
			));
		}

		@Test
		@DisplayName("유효하지 않은 좌표로 조회 실패")
		void invalidCoordinate() throws Exception {
			CoordinateRequestDto request = new CoordinateRequestDto(0.0, 0.0);

			ResultActions result = mockMvc.perform(post("/api/location/coordinate/sigungu")
				.with(user(customUserDetails))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

			result
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("시도 경계선 조회 API 테스트")
	class getSidoBorderTest {
		@Test
		@DisplayName("시도 경계선 GeoJSON 조회 성공")
		void success() throws Exception {
			String geoJsonBorder = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[126.734086,37.413294]]]}}";

			when(locationService.getSidoBorderAsGeoJson(11L)).thenReturn(geoJsonBorder);

			ResultActions result = mockMvc.perform(get("/api/location/sido/{sidoId}/border", 11L)
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isString());

			result.andDo(document("get-sido-border-success",
				pathParameters(
					parameterWithName("sidoId").description("시도 ID")
				),
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("시도 경계선 GeoJSON")
				)
			));
		}
	}

	@Nested
	@DisplayName("시군구 경계선 조회 API 테스트")
	class getSigunguBorderTest {
		@Test
		@DisplayName("시군구 경계선 GeoJSON 조회 성공")
		void success() throws Exception {
			String geoJsonBorder = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[126.734086,37.413294]]]}}";

			when(locationService.getSigunguBorderAsGeoJson(11200L)).thenReturn(geoJsonBorder);

			ResultActions result = mockMvc.perform(get("/api/location/sigungu/{sigunguId}/border", 11200L)
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isString());

			result.andDo(document("get-sigungu-border-success",
				pathParameters(
					parameterWithName("sigunguId").description("시군구 ID")
				),
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("시군구 경계선 GeoJSON")
				)
			));
		}
	}

	@Nested
	@DisplayName("시군구 정보 조회 API 테스트")
	class getSigunguTest {
		@Test
		@DisplayName("시군구 ID로 시군구 정보 조회 성공")
		void success() throws Exception {
			JusoSido seoul = createSido(11L, "서울특별시");
			JusoSigungu dongjak = createSigungu(11200L, "동작구", seoul);

			when(locationService.getSigunguById(11200L)).thenReturn(dongjak);

			ResultActions result = mockMvc.perform(get("/api/location/sigungu/{sigunguId}", 11200L)
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.sigunguId").value(11200L))
				.andExpect(jsonPath("$.data.sigunguName").value("동작구"))
				.andExpect(jsonPath("$.data.sidoId").value(11L))
				.andExpect(jsonPath("$.data.sidoName").value("서울특별시"));

			result.andDo(document("get-sigungu-by-id-success",
				pathParameters(
					parameterWithName("sigunguId").description("시군구 ID")
				),
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.sigunguId").description("시군구 ID"),
					fieldWithPath("data.sigunguName").description("시군구명"),
					fieldWithPath("data.sidoId").description("시도 ID"),
					fieldWithPath("data.sidoName").description("시도명")
				)
			));
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
	@DisplayName("시도별 사용자 이미지 조회 API 테스트")
	class getImagesBySidoTest {
		@Test
		@DisplayName("시도별 사용자 이미지 조회 성공")
		void success() throws Exception {
			List<ImageThumbnailDto> response = List.of();

			when(imageService.getImagesBySido(11L, 1L)).thenReturn(response);

			ResultActions result = mockMvc.perform(get("/api/location/sido/{sidoId}/images", 11L)
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(0));

			result.andDo(document("get-images-by-sido-success",
				pathParameters(
					parameterWithName("sidoId").description("시도 ID")
				),
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("이미지 목록").type(JsonFieldType.ARRAY)
				)
			));
		}
	}

	@Nested
	@DisplayName("시군구별 사용자 이미지 조회 API 테스트")
	class getImagesBySigunguTest {
		@Test
		@DisplayName("시군구별 사용자 이미지 조회 성공")
		void success() throws Exception {
			List<ImageThumbnailDto> response = List.of();

			when(imageService.getImagesBySigungu(11200L, 1L)).thenReturn(response);

			ResultActions result = mockMvc.perform(get("/api/location/sigungu/{sigunguId}/images", 11200L)
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(0));

			result.andDo(document("get-images-by-sigungu-success",
				pathParameters(
					parameterWithName("sigunguId").description("시군구 ID")
				),
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("이미지 목록").type(JsonFieldType.ARRAY)
				)
			));
		}
	}

	@Nested
	@DisplayName("홈 화면 사용자 통계 조회 API 테스트")
	class getHomeStatsTest {
		@Test
		@DisplayName("홈 화면 사용자 통계 조회 성공")
		void success() throws Exception {
			UserStatsDto stats = new UserStatsDto(5L, 3L, 2L);

			when(locationService.getUserStats(1L)).thenReturn(stats);

			ResultActions result = mockMvc.perform(get("/api/location/home/stats")
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.imageCount").value(5L))
				.andExpect(jsonPath("$.data.albumCount").value(3L))
				.andExpect(jsonPath("$.data.sigunguCount").value(2L));

			result.andDo(document("get-home-stats-success",
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.imageCount").description("총 이미지 수"),
					fieldWithPath("data.albumCount").description("총 앨범 수"),
					fieldWithPath("data.sigunguCount").description("방문한 시군구 수")
				)
			));
		}
	}
}
