package com.trackery.trackerybackapiserver.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.dto.update.UpdatePasswordDto;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.service
 * fileName       : UpdateUserInfoServiceTest
 * author         : durururuk
 * date           : 25. 4. 14.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 14.		durururuk		최초 생성
 * 25. 4. 14.		durururuk		mock 단위테스트 작성
 */
@ExtendWith(MockitoExtension.class)
class UpdateUserInfoServiceTest {
	@InjectMocks
	private UpdateUserInfoService updateUserInfoService;

	@Mock
	JwtService jwtService;

	@Mock
	UserMapper userMapper;

	User user;

	@BeforeEach
	void setUp() {
		user = User.builder()
			.email("a@a.com")
			.userName("aaaaaa")
			.build();

		ReflectionTestUtils.setField(user, "userId", 1L);
	}

	@Nested
	@DisplayName("이메일 토큰 기반 비밀번호 변경 테스트")
	class updatePasswordByEmailTokenTest {
		@Test
		@DisplayName("성공")
		void success() {
			DecodedJWT mockDecodedJWT = mock(DecodedJWT.class);
			when(jwtService.verifyJwt(anyString())).thenReturn(mockDecodedJWT);
			when(mockDecodedJWT.getSubject()).thenReturn("a@a.com");

			when(userMapper.findByEmail("a@a.com")).thenReturn(java.util.Optional.of(user));

			doNothing().when(userMapper).updatePasswordByUserId(eq(1L), anyString(), anyString());

			updateUserInfoService.updatePasswordByEmailToken("emailToken", "<PASSWORD>");

			verify(jwtService, times(1)).verifyJwt(anyString());
			verify(userMapper, times(1)).findByEmail("a@a.com");
			verify(userMapper, times(1)).updatePasswordByUserId(eq(1L), anyString(), anyString());
		}

		@Test
		@DisplayName("실패 - DB에 이메일로 유저를 찾지 못했을 경우")
		void fail_1() {
			DecodedJWT mockDecodedJWT = mock(DecodedJWT.class);
			when(jwtService.verifyJwt(anyString())).thenReturn(mockDecodedJWT);
			when(mockDecodedJWT.getSubject()).thenReturn("a@a.com");

			when(userMapper.findByEmail(anyString())).thenThrow(new ApiException(ErrorCode.NOT_FOUND_USER));

			assertThrows(ApiException.class, () -> updateUserInfoService.updatePasswordByEmailToken("emailToken", "<PASSWORD>"));
		}
	}

	@Nested
	@DisplayName("인증 기반 비밀번호 변경 테스트")
	class UpdatePasswordByAuthenticationTest {
		@Test
		@DisplayName("성공")
		void success() {
			String oldPassword = "oldPass123!";
			String newPassword = "newPass456!";

			String salt = "SALT123";
			String hashedOldPassword = PasswordUtil.hashPassword(oldPassword, salt);
			String newSalt = "NEW_SALT456";
			String newHashedPassword = PasswordUtil.hashPassword(newPassword, newSalt);

			User user = User.builder()
				.userName("테스트유저")
				.email("user@example.com")
				.password(hashedOldPassword)
				.salt(salt)
				.build();
			ReflectionTestUtils.setField(user, "userId", 1L);

			UpdatePasswordDto dto = new UpdatePasswordDto();
			ReflectionTestUtils.setField(dto, "oldPassword", oldPassword);
			ReflectionTestUtils.setField(dto, "newPassword", newPassword);

			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));

			updateUserInfoService.updatePasswordByAuthentication(1L, dto);

			verify(userMapper).updatePasswordByUserId(eq(1L), anyString(), anyString());
		}

		@Test
		@DisplayName("실패 - 기존 비밀번호 불일치")
		void fail_1() {
			String wrongOldPassword = "wrongOldPass!";
			String actualOldPassword = "correctOldPass!";
			String salt = "SALT";
			String hashedActual = PasswordUtil.hashPassword(actualOldPassword, salt);

			User user = User.builder()
				.userName("테스트유저")
				.email("user@example.com")
				.password(hashedActual)
				.salt(salt)
				.build();
			ReflectionTestUtils.setField(user, "userId", 1L);

			UpdatePasswordDto dto = new UpdatePasswordDto();
			ReflectionTestUtils.setField(dto, "oldPassword", wrongOldPassword);
			ReflectionTestUtils.setField(dto, "newPassword", "newPass123!");

			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));

			ApiException e = assertThrows(ApiException.class, () ->
				updateUserInfoService.updatePasswordByAuthentication(1L, dto)
			);

			assertEquals(ErrorCode.BAD_REQUEST_INVALID_PASSWORD, e.getErrorCode());
			verify(userMapper, never()).updatePasswordByUserId(anyLong(), any(), any());
		}
	}

	@Nested
	@DisplayName("닉네임 변경 테스트")
	class UpdateUserNicknameTest {

		@Test
		@DisplayName("성공")
		void success() {
			User user = User.builder().nickname("oldNickname").build();
			ReflectionTestUtils.setField(user, "userId", 1L);
			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));

			updateUserInfoService.updateUserNickname(1L, "newNickname");

			verify(userMapper).updateNicknameByUserId(1L, "newNickname");
		}

		@Test
		@DisplayName("실패 - 기존 닉네임과 같을 경우")
		void fail_1() {
			User user = User.builder().nickname("sameNickname").build();
			ReflectionTestUtils.setField(user, "userId", 1L);
			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));

			ApiException e = assertThrows(ApiException.class, () ->
				updateUserInfoService.updateUserNickname(1L, "sameNickname"));

			assertEquals(ErrorCode.BAD_REQUEST_SAME_UPDATE, e.getErrorCode());
			verify(userMapper, never()).updateNicknameByUserId(anyLong(), any());
		}
	}

	@Nested
	@DisplayName("유저명 변경 테스트")
	class UpdateUserNameTest {

		@Test
		@DisplayName("성공")
		void success() {
			User user = User.builder()
				.userName("oldName")
				.roleId(1L)
				.build();
			ReflectionTestUtils.setField(user, "userId", 1L);

			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));
			when(jwtService.generateAccessTokenAndRefreshToken(1L, "newName", 1L))
				.thenReturn(new AuthTokenDto("access", "refresh"));

			AuthTokenDto token = updateUserInfoService.updateUserName(1L, "newName");

			assertEquals("access", token.accessToken());
			assertEquals("refresh", token.refreshToken());
			verify(userMapper).updateUserNameByUserId(1L, "newName");
		}

		@Test
		@DisplayName("실패 - 기존 유저명과 같을 경우")
		void fail_1() {
			User user = User.builder()
				.userName("sameName")
				.build();
			ReflectionTestUtils.setField(user, "userId", 1L);

			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));

			ApiException e = assertThrows(ApiException.class, () ->
				updateUserInfoService.updateUserName(1L, "sameName"));

			assertEquals(ErrorCode.BAD_REQUEST_SAME_UPDATE, e.getErrorCode());
			verify(userMapper, never()).updateUserNameByUserId(anyLong(), any());
		}
	}

	@Nested
	@DisplayName("이메일 변경 테스트")
	class UpdateEmailTest {

		@Test
		@DisplayName("성공")
		void success() {
			User user = User.builder()
				.email("old@mail.com")
				.build();
			ReflectionTestUtils.setField(user, "userId", 1L);

			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));

			DecodedJWT mockJwt = mock(DecodedJWT.class);
			when(jwtService.verifyJwt("token")).thenReturn(mockJwt);
			when(mockJwt.getSubject()).thenReturn("new@mail.com");

			updateUserInfoService.updateEmail(1L, "token");

			verify(userMapper).updateEmailByUserId(1L, "new@mail.com");
		}

		@Test
		@DisplayName("실패 - 기존 이메일과 같을 경우")
		void fail_1() {
			User user = User.builder()
				.email("same@mail.com")
				.build();
			ReflectionTestUtils.setField(user, "userId", 1L);

			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));

			DecodedJWT mockJwt = mock(DecodedJWT.class);
			when(jwtService.verifyJwt("token")).thenReturn(mockJwt);
			when(mockJwt.getSubject()).thenReturn("same@mail.com");

			ApiException e = assertThrows(ApiException.class, () ->
				updateUserInfoService.updateEmail(1L, "token"));

			assertEquals(ErrorCode.BAD_REQUEST_SAME_UPDATE, e.getErrorCode());
			verify(userMapper, never()).updateEmailByUserId(anyLong(), any());
		}
	}
}