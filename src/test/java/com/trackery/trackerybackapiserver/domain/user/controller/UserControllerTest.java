package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.user.dto.DetailedUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserNameAvailabilityResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.entity.OAuth;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

import jakarta.servlet.http.Cookie;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.controller
 * fileName       : UserControllerTest
 * author         : durururuk
 * date           : 25. 2. 14.
 * description    : 유저 컨트롤러의 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.		durururuk		최초 생성
 * 25. 2. 14.		durururuk		UserController 성공 케이스 테스트 코드 작성
 * 25. 2. 17.		durururuk		Java 컨벤션에 맞게 username -> userName 수정
 * 25. 2. 17.		durururuk		클래스 JavaDoc 설명 추가
 * 25. 2. 18.		inari			dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 19.		durururuk		MockMvc 테스트코드 작성을 도와주는 추상 클래스 추가
 * 25. 2. 19.		durururuk		코딩 컨벤션에 맞게 정리
 * 25. 2. 19.		durururuk		테스트 하고자 하는 컨트롤러만 로드하게 수정
 * 25. 2. 20.		durururuk		변경된 로직에 맞게 테스트코드 수정
 * 25. 2. 20.		durururuk		변경된 로직에 맞게 테스트코드 수정
 * 25. 2. 21.		durururuk		CustomWebSecurityConfig으로 퍼블릭 uri 관리 일원화
 * 25. 2. 24.		durururuk		회원가입 시 인증 헤더 -> http-only 쿠키 방식으로 변경
 * 25. 2. 26.		durururuk		로그인 컨트롤러 테스트 코드 작성
 * 25. 3. 14.		durururuk		바뀐 로직에 맞게 테스트코드 수정
 * 25. 3. 14.		durururuk		수정된 로직에 맞게 테스트 코드 수정
 * 25. 3. 28.		durururuk		변경된 로직에 맞게 테스트 코드 수정
 * 25. 3. 28.		durururuk		바뀐 로직에 맞게 테스트 코드 수정
 * 25. 3. 29.		durururuk		코드 가독성을 위해 List.of(accessToken, refreshToken) 구조에서 DTO 방식으로 변경
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 * 25. 4. 9.		durururuk		유저 상세정보 조회 API 단위테스트 코드 작성
 * 25. 4. 9.		durururuk		유저 상세정보 조회 API 단위테스트 코드 작성
 * 25. 4. 10.		durururuk		인증 기반 비밀번호 변경 컨트롤러 mockMvc 테스트 작성
 * 25. 4. 10.		durururuk		final이 될 수 있는 변수 final화, 로직 수정으로 사용되지 않는 메서드 삭제
 * 25. 4. 12.		durururuk		유저명 변경 기능 구현
 * 25. 4. 14.		durururuk		UpdateUserInfo Controller, Serivce 단위테스트 작성
 * 25. 6. 17.		inari			user 도메인 테스트를 Spring-Rest-Docs에 맞게 리팩터링 및 문서 추가하였습니다.
 * 25. 6. 25.		inari			테스트 코드 추가
 * 25. 6. 26.		inari			탈퇴시 테스트코드 작성 및 문서화
 * 25. 6. 26.		inari			탈퇴시 서비스에서 컨트롤러로 쿠키삭제 처리 피드백 반영
 * 25. 8. 7.		inari			중복체크 실패 테스트 코드 추가
 */
@WebMvcTest(UserController.class)
class UserControllerTest extends CommonMockMvcControllerTestSetUp {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Spy
	private UserRegisterDto registerDto;

	@Spy
	private UserLoginDto loginDto;

	@Test
	void 회원가입_성공() throws Exception {
		ReflectionTestUtils.setField(registerDto, "nickname", "김커피");
		ReflectionTestUtils.setField(registerDto, "password", "Qwerasdf1234!!asdf");

		String accessToken = "accessToken";
		String refreshToken = "refreshToken";
		AuthTokenDto authTokenDto = new AuthTokenDto(accessToken, refreshToken);
		String emailToken = "emailJwt";
		String userNameToken = "userNameJwt";

		when(userService.registerUser(eq(emailToken), eq(userNameToken), any(UserRegisterDto.class))).thenReturn(
			authTokenDto);

		ResultActions result = mockMvc
			.perform(post("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(new Cookie("emailToken", emailToken))
				.cookie(new Cookie("userNameToken", userNameToken))
				.content(objectMapper.writeValueAsString(registerDto))
				.with(csrf())
			);

		result
			.andExpect(status().isCreated())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().value("accessToken", accessToken))
			.andExpect(cookie().exists("refreshToken"))
			.andExpect(cookie().httpOnly("refreshToken", true))
			.andExpect(cookie().value("refreshToken", refreshToken))
			.andDo(document("register-user-success",
				requestFields(
					fieldWithPath("nickname").description("사용자 닉네임"),
					fieldWithPath("password").description("사용자 비밀번호"),
					fieldWithPath("userProfile").description("사용자 프로필 이미지").optional()
				),
				relaxedResponseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));
	}

	@Test
	void 유저명_중복체크_성공() throws Exception {
		UserNameAvailabilityResponseDto dto = new UserNameAvailabilityResponseDto(true, "jwt");
		when(userService.checkUsernameAvailability(anyString())).thenReturn(dto);

		ResultActions result = mockMvc
			.perform(get("/api/users/exists/username")
				.queryParam("value", "abcdefg"));

		result.andExpect(status().isOk())
			.andExpect(cookie().exists("userNameToken"))
			.andExpect(cookie().value("userNameToken", "jwt"))
			.andExpect(jsonPath("$.data").value(true))
			.andDo(document("check-username-availability-success",
				queryParameters(
					parameterWithName("value").description("확인할 사용자명")
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("사용자명 사용 가능 여부")
				)
			));
	}

	@Test
	void 유저명_중복체크_실패() throws Exception {
		UserNameAvailabilityResponseDto dto = new UserNameAvailabilityResponseDto(false, null);
		when(userService.checkUsernameAvailability(anyString())).thenReturn(dto);

		ResultActions result = mockMvc
			.perform(get("/api/users/exists/username")
				.queryParam("value", "existinguser"));

		result.andExpect(status().isOk())
			.andExpect(cookie().doesNotExist("userNameToken"))
			.andExpect(jsonPath("$.data").value(false))
			.andDo(document("check-username-availability-failure",
				queryParameters(
					parameterWithName("value").description("확인할 사용자명")
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("사용자명 사용 가능 여부")
				)
			));
	}

	@Test
	void 로그인_테스트() throws Exception {
		ReflectionTestUtils.setField(loginDto, "userName", "abcdefg");
		ReflectionTestUtils.setField(loginDto, "password", "Qwerasdf1234!");

		String accessToken = "accessToken";
		String refreshToken = "refreshToken";
		AuthTokenDto authTokenDto = new AuthTokenDto(accessToken, refreshToken);

		when(userService.login(any())).thenReturn(authTokenDto);

		ResultActions result = mockMvc
			.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto))
				.with(csrf()));

		result
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().value("accessToken", accessToken))
			.andExpect(cookie().exists("refreshToken"))
			.andExpect(cookie().httpOnly("refreshToken", true))
			.andExpect(cookie().value("refreshToken", refreshToken))
			.andDo(document("login-user-success",
				requestFields(
					fieldWithPath("userName").description("사용자명"),
					fieldWithPath("password").description("비밀번호")
				),
				relaxedResponseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));
	}

	@Nested
	@DisplayName("유저 상세 정보 조회 테스트")
	class getDetailedUserInfoTest {
		private OAuth oAuth;

		@BeforeEach
		void setUp() {
			oAuth = OAuth.builder()
				.userId(1L)
				.providerUserId("155788848")
				.provider("KAKAO")
				.build();
			ReflectionTestUtils.setField(oAuth, "oauthId", 1L);
		}

		@Test
		@DisplayName("성공")
		void success() throws Exception {
			CustomUserDetails customUserDetails = CustomUserDetails.builder()
				.userId(1L).roleId(1L).build();

			DetailedUserInfoDto dto = new DetailedUserInfoDto(1L, 1L,
				"abcdefg", "김커피", "a@a.com", List.of(oAuth));

			when(userService.getDetailedUserInfoByUserId(1L)).thenReturn(dto);

			ResultActions result = mockMvc.perform(get("/api/users/details")
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andExpect(jsonPath("$.data.userId").value(1))
				.andExpect(jsonPath("$.data.userRoleId").value(1))
				.andExpect(jsonPath("$.data.userName").value("abcdefg"))
				.andExpect(jsonPath("$.data.nickname").value("김커피"))
				.andExpect(jsonPath("$.data.email").value("a@a.com"))
				.andExpect(jsonPath("$.data.OAuthList[0].oauthId").value(1))
				.andExpect(jsonPath("$.data.OAuthList[0].userId").value(1))
				.andExpect(jsonPath("$.data.OAuthList[0].provider").value("KAKAO"))
				.andExpect(jsonPath("$.data.OAuthList[0].providerUserId").value("155788848"))
				.andDo(document("get-user-detailed-info-success",
					responseFields(
						fieldWithPath("code").description("상태 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.userId").description("사용자 ID"),
						fieldWithPath("data.userRoleId").description("사용자 역할 ID"),
						fieldWithPath("data.userName").description("사용자명"),
						fieldWithPath("data.nickname").description("닉네임"),
						fieldWithPath("data.email").description("이메일"),
						fieldWithPath("data.OAuthList[].oauthId").description("OAuth ID"),
						fieldWithPath("data.OAuthList[].userId").description("연결된 사용자 ID"),
						fieldWithPath("data.OAuthList[].provider").description("OAuth 제공자"),
						fieldWithPath("data.OAuthList[].providerUserId").description("제공자 사용자 ID")
					)
				));
		}
	}

	@Test
	@DisplayName("로그아웃 성공")
	void 로그아웃_성공() throws Exception {
		String accessToken = "validAccessToken";
		String refreshToken = "validRefreshToken";
		
		doNothing().when(userService).logout(accessToken, refreshToken);
		
		ResultActions result = mockMvc
			.perform(post("/api/users/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(new Cookie("accessToken", accessToken))
				.cookie(new Cookie("refreshToken", refreshToken))
				.with(csrf())
			);
		
		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andDo(document("logout-user-success",
				relaxedResponseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));
		
		verify(userService, times(1)).logout(accessToken, refreshToken);
	}

	@Test
	@DisplayName("회원탈퇴 성공")
	void 회원탈퇴_성공() throws Exception {
		String accessToken = "validAccessToken";
		String refreshToken = "validRefreshToken";
		Long userId = 1L;
		
		CustomUserDetails customUserDetails = new CustomUserDetails(userId, "testuser", 1L);
		
		doNothing().when(userService).deleteUser(userId, accessToken, refreshToken);
		
		ResultActions result = mockMvc
			.perform(delete("/api/users/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(new Cookie("accessToken", accessToken))
				.cookie(new Cookie("refreshToken", refreshToken))
				.with(user(customUserDetails))
				.with(csrf())
			);
		
		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andDo(document("delete-user-success",
				relaxedResponseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));
		
		verify(userService, times(1)).deleteUser(userId, accessToken, refreshToken);
	}
}
