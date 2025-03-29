package com.trackery.trackerybackapiserver.domain.user.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.JwtUserInfoDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserNameAvailabilityResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserRoleMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.service
 * fileName       : UserMapper
 * author         : durururuk
 * date           : 25. 2. 12.
 * description    : 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.        durururuk       최초 생성
 * 25. 2. 24.        inari         주석 추가
 * 25. 2. 25.        durururuk       로그인 메서드 추가
 * 25. 2. 26.        durururuk       로그인 시 비활성유저인지 확인하는 로직 추가
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
	private final UserMapper userMapper;
	private final JwtService jwtService;
	private final UserRoleMapper userRoleMapper;

	/**
	 * 회원가입 정보를 담아서 db에 인서트하는 메서드입니다.
	 * 회원가입 후 유저 역할 (1L) 을 같이 인서트합니다.
	 *
	 * @param userRegisterDto : 회원 가입 정보를 담은 DTO
	 */
	public AuthTokenDto registerUser(String emailToken, String userNameToken, UserRegisterDto userRegisterDto) {
		DecodedJWT decodedEmailToken = jwtService.verifyJwt(emailToken);
		DecodedJWT decodedUserNameToken = jwtService.verifyJwt(userNameToken);

		String email = decodedEmailToken.getSubject();
		String userName = decodedUserNameToken.getSubject();

		String salt = PasswordUtil.generateSalt();
		String hashedPassword = PasswordUtil.hashPassword(userRegisterDto.getPassword(), salt);

		User user = User.builder()
			.email(email)
			.userName(userName)
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

		return jwtService.generateAccessTokenAndRefreshToken(user.getUserId(), user.getUserName(),
			userRole.getRoleId());
	}

	/**
	 * 로그인 정보 DTO 받아서 인증 후 jwt 토큰을 반환하는 메서드
	 *
	 * @param userLoginDto : username, password 받는 DTO
	 * @return : 인증된 유저의 정보를 담고있는 jwt
	 */
	public AuthTokenDto login(UserLoginDto userLoginDto) {
		User user = userMapper.findByUserName(userLoginDto.getUserName()).orElseThrow(() -> new ApiException(
			ErrorCode.UNAUTHORIZED_INVALID_CREDENTIALS));

		if (!PasswordUtil.hashPassword(userLoginDto.getPassword(), user.getSalt()).equals(user.getPassword())) {
			throw new ApiException(ErrorCode.UNAUTHORIZED_INVALID_CREDENTIALS);
		}

		UserRole userRole = userRoleMapper.findByUserId(user.getUserId())
			.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

		return jwtService.generateAccessTokenAndRefreshToken(user.getUserId(), user.getUserName(),
			userRole.getRoleId());
	}

	/**
	 * 유저명 중복체크를 하여 토큰 발급하는 메서드
	 * @param userName : 중복체크할 유저명
	 * @return : boolean, jwt 토큰을 담은 DTO
	 */
	public UserNameAvailabilityResponseDto checkUsernameAvailability(String userName) {
		if (!userMapper.isExistsUserName(userName)) {
			String jwt = jwtService.generateTokenWithSubject(userName);
			return new UserNameAvailabilityResponseDto(true, jwt);
		} else {
			return new UserNameAvailabilityResponseDto(false, null);
		}
	}

	/**
	 * 비밀번호를 변경하는 메서드
	 * @param emailToken : 변경할 유저의 이메일
	 * @param password : 새로 변경될 비밀번호
	 */
	public void changePassword(String emailToken, String password) {
		DecodedJWT jwt;
		try {
			jwt = jwtService.verifyJwt(emailToken);
		} catch (JWTVerificationException e) {
			log.error(e.getMessage());
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		String email = jwt.getSubject();

		if (!userMapper.isExistsEmail(email)) {
			throw new ApiException(ErrorCode.NOT_FOUND);
		}

		String salt = PasswordUtil.generateSalt();
		String hashedPassword = PasswordUtil.hashPassword(password, salt);

		userMapper.updatePassword(email, hashedPassword, salt);
	}

	public JwtUserInfoDto getUserInfoById(Long userId) {
		User user = userMapper.findByUserId(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		UserRole userRole = userRoleMapper.findByUserId(userId)
			.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));
		return new JwtUserInfoDto(userId, user.getUserName(), userRole.getRoleId());
	}

}
