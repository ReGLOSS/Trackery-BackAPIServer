package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.user.dto.UserProfileDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.controller
 * fileName       : UserProfileControllerTest
 * author         : inari
 * date           : 25. 4. 28.
 * description    : UserProfileController 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 28.        inari      	 최초 생성
 * 25. 6. 17.		 inari		   Spring-Rest-Docs api문서 추가
 */
@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest extends CommonMockMvcControllerTestSetUp {

	@MockitoBean
	private UserService userService;

	private CustomUserDetails customUserDetails;
	private UserProfileDto userProfileDto;

	@BeforeEach
	void setUp() {
		customUserDetails = CustomUserDetails.builder()
			.userId(1L)
			.roleId(1L)
			.userName("testuser")
			.build();

		userProfileDto = new UserProfileDto(1L, "testuser", "테스트유저", "profile-image-url");
	}

	@Test
	@DisplayName("내 프로필 조회 성공")
	void getMyProfile_success() throws Exception {
		when(userService.getUserProfile(1L)).thenReturn(userProfileDto);

		ResultActions result = mockMvc.perform(get("/api/users/profile/me")
			.with(user(customUserDetails)));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data.userId").value(1L))
			.andExpect(jsonPath("$.data.userName").value("testuser"))
			.andExpect(jsonPath("$.data.nickname").value("테스트유저"))
			.andExpect(jsonPath("$.data.userProfilePic").value("profile-image-url"))
			.andDo(document("get-user-profile-success",
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.userId").description("사용자 ID"),
					fieldWithPath("data.userName").description("사용자명"),
					fieldWithPath("data.nickname").description("닉네임"),
					fieldWithPath("data.userProfilePic").description("프로필 이미지 URL")
				)
			));

		verify(userService, times(1)).getUserProfile(1L);
	}

	@Test
	@DisplayName("내 프로필 조회 실패 - 사용자 없음")
	void getMyProfile_fail_userNotFound() throws Exception {
		// given
		when(userService.getUserProfile(1L)).thenThrow(new ApiException(ErrorCode.NOT_FOUND));

		// when & then - 전역 예외 핸들러가 있으므로 404 상태 코드 반환
		mockMvc.perform(get("/api/users/profile/me")
			.with(user(customUserDetails)))
			.andExpect(status().isNotFound());

		verify(userService, times(1)).getUserProfile(1L);
	}

	// 예외의 근본 원인을 찾는 헬퍼 메서드
	private Throwable getRootCause(Throwable throwable) {
		Throwable cause = throwable.getCause();
		if (cause == null) {
			return throwable;
		}
		return getRootCause(cause);
	}

	@Test
	@DisplayName("내 프로필 조회 성공 - 프로필 이미지 없음")
	void getMyProfile_success_noProfileImage() throws Exception {
		// given
		UserProfileDto profileWithoutImage = new UserProfileDto(1L, "testuser", "테스트유저", null);
		when(userService.getUserProfile(1L)).thenReturn(profileWithoutImage);

		// when & then
		mockMvc.perform(get("/api/users/profile/me")
				.with(user(customUserDetails)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data.userId").value(1L))
			.andExpect(jsonPath("$.data.userName").value("testuser"))
			.andExpect(jsonPath("$.data.nickname").value("테스트유저"))
			.andExpect(jsonPath("$.data.userProfilePic").isEmpty());

		verify(userService, times(1)).getUserProfile(1L);
	}

	@Test
	@DisplayName("인증되지 않은 사용자 접근 시 실패")
	void getMyProfile_fail_unauthorized() throws Exception {
		// given
		// null을 반환하는 사용자 정보로 새 컨트롤러 설정
		CustomUserDetails nullUserDetails = mock(CustomUserDetails.class);
		when(nullUserDetails.getUserId()).thenReturn(null);  // ID가 null인 사용자

		TestUserProfileController unauthorizedController = new TestUserProfileController(userService, nullUserDetails);

		MockMvc unauthorizedMockMvc = MockMvcBuilders
			.standaloneSetup(unauthorizedController)
			.addFilter(new CharacterEncodingFilter("UTF-8", true))
			.build();

		// 서비스에서 null ID로 호출할 때 예외 발생하도록 설정
		when(userService.getUserProfile(null)).thenThrow(new ApiException(ErrorCode.UNAUTHORIZED));

		// when & then
		try {
			unauthorizedMockMvc.perform(MockMvcRequestBuilders.get("/api/users/profile/me")
				.contentType(MediaType.APPLICATION_JSON));
			fail("예외가 발생해야 합니다");
		} catch (Exception e) {
			// 근본 원인인 ApiException을 확인
			Throwable rootCause = getRootCause(e);
			assertTrue(rootCause instanceof ApiException);
			assertEquals(ErrorCode.UNAUTHORIZED, ((ApiException) rootCause).getErrorCode());
		}

		verify(userService, times(1)).getUserProfile(null);
	}

	// 테스트용 컨트롤러 서브클래스를 만들어 @AuthenticationPrincipal을 우회하는 테스트 전용 컨트롤러
	static class TestUserProfileController extends UserProfileController {
		private final CustomUserDetails mockUserDetails;

		public TestUserProfileController(UserService userService, CustomUserDetails mockUserDetails) {
			super(userService);
			this.mockUserDetails = mockUserDetails;
		}

		// 원래 메서드를 오버라이드하여 테스트용 사용자 정보 제공
		@Override
		public org.springframework.http.ResponseEntity<com.trackery.trackerybackapiserver.domain.common.response.ApiResponse<UserProfileDto>> getMyProfile(
			CustomUserDetails userDetails) {
			return super.getMyProfile(mockUserDetails);
		}
	}
}
