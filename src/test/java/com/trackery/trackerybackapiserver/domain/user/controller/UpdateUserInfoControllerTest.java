package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.user.dto.update.UpdateNicknameDto;
import com.trackery.trackerybackapiserver.domain.user.dto.update.UpdatePasswordDto;
import com.trackery.trackerybackapiserver.domain.user.dto.update.UpdateUserNameDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.service.UpdateUserInfoService;

import jakarta.servlet.http.Cookie;

@WebMvcTest(UpdateUserInfoController.class)
class UpdateUserInfoControllerTest extends CommonMockMvcControllerTestSetUp {

	@MockitoBean
	private UpdateUserInfoService updateUserInfoService;

	CustomUserDetails customUserDetails;

	@BeforeEach
	void setUp() {
		customUserDetails = CustomUserDetails.builder()
			.userId(1L)
			.roleId(1L)
			.userName("abcdefg")
			.build();
	}

	@Nested
	@DisplayName("닉네임 수정 API MockMvc 테스트")
	class updateNicknameTest {
		@Test
		@DisplayName("성공")
		void success() throws Exception {
			UpdateNicknameDto updateNicknameDto = new UpdateNicknameDto();
			ReflectionTestUtils.setField(updateNicknameDto, "nickname", "김커피");

			doNothing().when(updateUserInfoService).updateUserNickname(1L, "김커피");

			ResultActions result = mockMvc.perform(patch("/api/users/me/nickname")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateNicknameDto))
				.with(user(customUserDetails)));

			result.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"));

			verify(updateUserInfoService, times(1)).updateUserNickname(anyLong(), any());
		}
	}

	@Nested
	@DisplayName("유저명 수정 API MockMvc 테스트")
	class UpdateUserNameTest {
		@Test
		@DisplayName("성공")
		void success() throws Exception {
			String newUserName = "새 유저명";
			UpdateUserNameDto dto = new UpdateUserNameDto();
			ReflectionTestUtils.setField(dto, "userName", newUserName);

			AuthTokenDto authTokenDto = new AuthTokenDto("newAccessToken", "newRefreshToken");

			when(updateUserInfoService.updateUserName(1L, newUserName))
				.thenReturn(authTokenDto);

			ResultActions result = mockMvc.perform(patch("/api/users/me/username")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))
				.with(user(customUserDetails)));

			result.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"));

			verify(updateUserInfoService, times(1))
				.updateUserName(1L, newUserName);
		}
	}

	@Nested
	@DisplayName("이메일 토큰 기반 비밀번호 변경 API MockMvc 테스트")
	class UpdatePasswordByEmailTokenTest {

		@Test
		@DisplayName("성공")
		void success() throws Exception {
			UpdatePasswordDto dto = new UpdatePasswordDto();
			ReflectionTestUtils.setField(dto, "newPassword", "newStrongPassword123!");

			doNothing().when(updateUserInfoService)
				.updatePasswordByEmailToken("test-token", "newStrongPassword123!");

			mockMvc.perform(patch("/api/users/me/password/email-token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(dto))
					.cookie(new Cookie("emailToken", "test-token")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"));

			verify(updateUserInfoService, times(1))
				.updatePasswordByEmailToken("test-token", "newStrongPassword123!");
		}
	}

	@Nested
	@DisplayName("인증된 유저 기반 비밀번호 변경 API MockMvc 테스트")
	class UpdatePasswordByAuthenticationTest {
		@Test
		@DisplayName("성공")
		void success() throws Exception {
			UpdatePasswordDto dto = new UpdatePasswordDto();
			ReflectionTestUtils.setField(dto, "oldPassword", "Qwerasdf1234!!!!!");
			ReflectionTestUtils.setField(dto, "newPassword", "Rlarlehd1234!!!!!");

			doNothing().when(updateUserInfoService)
				.updatePasswordByAuthentication(eq(1L), any(UpdatePasswordDto.class));

			mockMvc.perform(patch("/api/users/me/password")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(dto))
					.with(user(customUserDetails)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"));

			verify(updateUserInfoService, times(1))
				.updatePasswordByAuthentication(eq(1L), any(UpdatePasswordDto.class));
		}
	}

	@Nested
	@DisplayName("이메일 업데이트 API MockMvc 테스트")
	class UpdateEmailTest {

		@Test
		@DisplayName("성공")
		void success() throws Exception {
			doNothing().when(updateUserInfoService)
				.updateEmail(1L, "test-email-token");

			mockMvc.perform(patch("/api/users/me/email")
					.cookie(new Cookie("emailToken", "test-email-token"))
					.with(user(customUserDetails)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"));

			verify(updateUserInfoService, times(1))
				.updateEmail(1L, "test-email-token");
		}
	}
}