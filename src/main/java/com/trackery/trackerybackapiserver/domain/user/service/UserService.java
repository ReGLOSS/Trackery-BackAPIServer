package com.trackery.trackerybackapiserver.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.JwtUserInfoDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.dto.DetailedUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserNameAvailabilityResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.OAuth;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;
import com.trackery.trackerybackapiserver.domain.user.mapper.OAuthMapper;
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
 * 25. 2. 24.        inari           주석 추가
 * 25. 2. 25.        durururuk       로그인 메서드 추가
 * 25. 2. 26.        durururuk       로그인 시 비활성유저인지 확인하는 로직 추가
 * 25. 4. 09.		 durururuk		 유저 상세 정보를 조회할 수 있는 메서드 추가
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
	private final UserMapper userMapper;
	private final JwtService jwtService;
	private final UserRoleMapper userRoleMapper;
	private final OAuthMapper oAuthMapper;

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
			.startDate(LocalDateTime.now())
			.status(1)
			.lastLogin(LocalDateTime.now())
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
			ErrorCode.BAD_REQUEST_INVALID_CREDENTIALS));

		if (!PasswordUtil.hashPassword(userLoginDto.getPassword(), user.getSalt()).equals(user.getPassword())) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_CREDENTIALS);
		}

		return jwtService.generateAccessTokenAndRefreshToken(user.getUserId(), user.getUserName(),
			user.getRoleId());
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
	 * 액세스 토큰 발급을 위한 유저 정보를 DB에서 조회 후 DTO로 반환하는 메서드입니다.
	 *
	 * @param userId : 유저 ID
	 * @return : userId, userName, userRole이 담긴 DTO
	 */
	public JwtUserInfoDto getUserInfoById(Long userId) {
		User user = userMapper.findByUserId(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND_USER));
		return new JwtUserInfoDto(userId, user.getUserName(), user.getRoleId());
	}

	/**
	 * 유저의 상세 정보를 조회하는 메서드입니다.
	 * 유저의 기본 정보, 간편로그인 연동 정보를 담은 DTO를 반환합니다.
	 * @param userId : 찾을 userId
	 * @return DTO
	 */
	//TODO 앨범 기능 구현 후 공개 앨범 정보도 조회할 수 있게 수정
	public DetailedUserInfoDto getDetailedUserInfoByUserId(Long userId) {
		User user = userMapper.findByUserId(userId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_USER));
		List<OAuth> oAuthList = oAuthMapper.findByUserId(userId);

		return new DetailedUserInfoDto(user.getUserId(), user.getRoleId(), user.getUserName(), user.getNickname(),
			user.getEmail(), oAuthList);
	}
}
