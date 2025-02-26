package com.trackery.trackerybackapiserver.domain.user.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserRoleMapper;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.service
 * fileName       : UserMapper
 * author         : dururuk
 * date           : 25. 2. 12.
 * description    : 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.        dururuk       최초 생성
 * 25. 2. 24.        inari         주석 추가
 */
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserMapper userMapper;
	private final JwtUtil jwtUtil;
	private final UserRoleMapper userRoleMapper;

	/**
	 * 회원가입 정보를 담아서 db에 인서트하는 메서드입니다.
	 * 회원가입 후 유저 역할 (1L) 을 같이 인서트합니다.
	 *
	 * @param userRegisterDto : 회원 가입 정보를 담은 DTO
	 */
	public String registerUser(UserRegisterDto userRegisterDto) {
		String salt = PasswordUtil.generateSalt();
		String hashedPassword = PasswordUtil.hashPassword(userRegisterDto.getPassword(), salt);

		User user = User.builder()
			.email(userRegisterDto.getEmail())
			.userName(userRegisterDto.getUserName())
			.nickname(userRegisterDto.getNickname())
			.password(hashedPassword)
			.salt(salt)
			.startDate(Timestamp.valueOf(LocalDateTime.now()))
			.status(1)
			.lastLogin(Timestamp.valueOf(LocalDateTime.now()))
			.userProfile(userRegisterDto.getUserProfile())
			.build();

		userMapper.insertUser(user);

		UserRole userRole = UserRole.builder()
			.userId(user.getUserId())
			.roleId(1L)
			.build();

		userRoleMapper.insertUserRole(userRole);

		return jwtUtil.generateJwt(user.getUserId(), user.getUserName(), userRole.getRoleId());
	}

	/**
	 * 유저명이 이미 db에 존재하는지 체크하는 메서드
	 *
	 * @param username 조회할 유저명
	 * @return db에 존재하지 않을 경우 true, db에 존재할 경우 false 반환
	 */
	public boolean checkUsernameAvailability(String username) {
		return !userMapper.isExistsUserName(username);
	}

	/**
	 * 로그인 정보 DTO를 받아서 인증 후 jwt 토큰을 반환하는 메서드
	 *
	 * @param userLoginDto : username, password를 받는 DTO
	 * @return : 인증된 유저의 정보를 담고있는 jwt
	 */
	public String login(UserLoginDto userLoginDto) {
		User user = userMapper.findByUserName(userLoginDto.getUserName()).orElseThrow(() -> new ApiException(
			ErrorCode.UNAUTHORIZED_INVALID_CREDENTIALS));

		if (!PasswordUtil.hashPassword(userLoginDto.getPassword(), user.getSalt()).equals(user.getPassword())) {
			throw new ApiException(ErrorCode.UNAUTHORIZED_INVALID_CREDENTIALS);
		}

		UserRole userRole = userRoleMapper.findByUserId(user.getUserId())
			.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

		return jwtUtil.generateJwt(user.getUserId(), user.getUserName(), userRole.getRoleId());
	}
}
