package com.trackery.trackerybackapiserver.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.dto.update.UpdatePasswordDto;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.service
 * fileName       : UpdateUserInfoService
 * author         : durururuk
 * date           : 25. 4. 12.
 * description    : 유저 정보 업데이트 관련 메서드
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 12.		durururuk		최초 생성
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateUserInfoService {
	private final UserMapper userMapper;
	private final JwtService jwtService;

	/**
	 * 이메일 토큰을 기반으로 비밀번호를 변경하는 메서드
	 * @param emailToken : 변경할 유저의 이메일 토큰
	 * @param password : 새로 변경될 비밀번호
	 */
	public void updatePasswordByEmailToken(String emailToken, String password) {
		DecodedJWT jwt = jwtService.verifyJwt(emailToken);
		String email = jwt.getSubject();

		User user = userMapper.findByEmail(email).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_USER));

		String salt = PasswordUtil.generateSalt();
		String hashedPassword = PasswordUtil.hashPassword(password, salt);

		userMapper.updatePasswordByUserId(user.getUserId(), hashedPassword, salt);
	}

	/**
	 * 인증을 기반으로 비밀번호를 변경하는 메서드
	 *
	 * @param userId : 비밀번호를 변경할 유저의 ID
	 * @param updatePasswordDto : 기존 비밀번호와 새 비밀번호 정보가 포함된 DTO
	 */
	public void updatePasswordByAuthentication(Long userId, UpdatePasswordDto updatePasswordDto) {
		User user = findUserByUserIdOrElseThrowApiException(userId);

		String hashedInputtedPassword = PasswordUtil.hashPassword(updatePasswordDto.getOldPassword(), user.getSalt());

		if (!hashedInputtedPassword.equals(user.getPassword())) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_PASSWORD);
		}

		String newSalt = PasswordUtil.generateSalt();

		String newHashedPassword = PasswordUtil.hashPassword(updatePasswordDto.getNewPassword(), newSalt);

		userMapper.updatePasswordByUserId(user.getUserId(), newHashedPassword, newSalt);
	}

	/**
	 * 유저의 닉네임을 변경하는 메서드
	 *
	 * @param userId : 닉네임을 변경할 유저의 ID
	 * @param nickname : 새로 변경할 닉네임
	 */
	public void updateUserNickname(Long userId, String nickname) {
		User user = findUserByUserIdOrElseThrowApiException(userId);

		if (user.getNickname().equals(nickname)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_SAME_UPDATE);
		}

		userMapper.updateNicknameByUserId(userId, nickname);
	}

	/**
	 * 유저의 이름을 변경하고 새 Access Token과 Refresh Token을 반환하는 메서드
	 *
	 * @param userId : 이름을 변경할 유저의 ID
	 * @param userName : 새로 변경할 유저 이름
	 * @return AuthTokenDto : 새로운 Access Token과 Refresh Token 정보
	 */
	public AuthTokenDto updateUserName(Long userId, String userName) {
		User user = findUserByUserIdOrElseThrowApiException(userId);

		if (user.getUserName().equals(userName)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_SAME_UPDATE);
		}

		userMapper.updateUserNameByUserId(userId, userName);

		return jwtService.generateAccessTokenAndRefreshToken(userId, userName, user.getRoleId());
	}

	/**
	 * 이메일 토큰을 기반으로 유저의 이메일을 변경하는 메서드
	 *
	 * @param userId : 이메일을 변경할 유저의 ID
	 * @param emailToken : 변경할 새 이메일의 토큰
	 */
	public void updateEmail(Long userId, String emailToken) {
		User user = findUserByUserIdOrElseThrowApiException(userId);

		String email = jwtService.verifyJwt(emailToken).getSubject();

		if (user.getEmail().equals(email)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_SAME_UPDATE);
		}

		userMapper.updateEmailByUserId(userId, email);
	}

	/**
	 * UserMapper에서 Optional로 User를 가져올 때 예외처리를 해서 User 반환
	 *
	 * @param userId 조회할 UserId
	 * @return User 엔티티 객체
	 */
	private User findUserByUserIdOrElseThrowApiException(Long userId) {
		return userMapper.findByUserId(userId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_USER));
	}
}
