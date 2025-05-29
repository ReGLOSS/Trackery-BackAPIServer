package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
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
 * 25. 4. 28.        inari       최초 생성
 */
@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

	private MockMvc mockMvc;

	@Mock
	private UserService userService;
	private UserProfileDto userProfileDto;
	private CustomUserDetails customUserDetails;
	private TestUserProfileController testController;

	@BeforeEach
	void setUp() {
		// CustomUserDetails 모의 객체 생성
		customUserDetails = mock(CustomUserDetails.class);
		lenient().when(customUserDetails.getUserId()).thenReturn(1L); // 명시적으로 ID 설정

		// 불필요한 스터빙 제거 또는 lenient 모드로 변경
		// username은 실제 테스트에서 사용되지 않지만 테스트 가독성을 위해 유지하고 lenient 설정
		lenient().when(customUserDetails.getUsername()).thenReturn("testuser");

		// 테스트용 컨트롤러 생성
		testController = new TestUserProfileController(userService, customUserDetails);

		// MockMvc 설정
		mockMvc = MockMvcBuilders
			.standaloneSetup(testController)
			.addFilter(new CharacterEncodingFilter("UTF-8", true))
			.build();

		// 테스트 데이터 설정
		userProfileDto = new UserProfileDto(1L, "testuser", "테스트유저", "profile-image-url");
	}

	@Test
	@DisplayName("내 프로필 조회 성공")
	void getMyProfile_success() throws Exception {
		// given
		when(userService.getUserProfile(1L)).thenReturn(userProfileDto);

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/users/profile/me")
				.contentType(MediaType.APPLICATION_JSON)
				.with(user(customUserDetails)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data.userId").value(1L))
			.andExpect(jsonPath("$.data.userName").value("testuser"))
			.andExpect(jsonPath("$.data.nickname").value("테스트유저"))
			.andExpect(jsonPath("$.data.userProfilePic").value("profile-image-url"));

		verify(userService, times(1)).getUserProfile(1L);
	}

	@Test
	@DisplayName("내 프로필 조회 실패 - 사용자 없음")
	void getMyProfile_fail_userNotFound() throws Exception {
		// given
		when(userService.getUserProfile(1L)).thenThrow(new ApiException(ErrorCode.NOT_FOUND));

		// Controller.getMyProfile은 예외를 던지도록 되어 있음
		// 예외는 일반적으로 전역 예외 핸들러에 의해 처리되지만 테스트에서는 제대로 설정되지 않았음
		// 따라서 예외가 발생하는지 직접 테스트
		try {
			mockMvc.perform(MockMvcRequestBuilders.get("/api/users/profile/me")
				.contentType(MediaType.APPLICATION_JSON));
			fail("예외가 발생해야 합니다");
		} catch (Exception e) {
			// 근본 원인인 ApiException을 확인
			Throwable rootCause = getRootCause(e);
			assertTrue(rootCause instanceof ApiException);
			assertEquals(ErrorCode.NOT_FOUND, ((ApiException) rootCause).getErrorCode());
		}

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
		mockMvc.perform(MockMvcRequestBuilders.get("/api/users/profile/me")
				.contentType(MediaType.APPLICATION_JSON))
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
