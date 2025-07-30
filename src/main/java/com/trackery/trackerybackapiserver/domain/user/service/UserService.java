package com.trackery.trackerybackapiserver.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.image.service.ImageS3Service;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.JwtUserInfoDto;
import com.trackery.trackerybackapiserver.domain.jwt.enums.JwtExpirationTime;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtRedisService;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.dto.DetailedUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserNameAvailabilityResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserProfileDto;
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
 * fileName       : UserService
 * author         : durururuk
 * date           : 25. 2. 12.
 * description    : 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.		durururuk		최초 생성
 * 25. 2. 12.		durururuk		기본 회원가입 기능 구현
 * 25. 2. 12.		durururuk		네이버 체크 스타일에 맞게 서식 수정
 * 25. 2. 14.		durururuk		User 생성 방식 빌더 패턴으로 변경
 * 25. 2. 14.		durururuk		회원가입, 닉네임 중복체크 api 퍼블릭으로 허용, 컨트롤러, 서비스에 주석 추가
 * 25. 2. 14.		durururuk		회원가입 할 때 비밀번호를 해싱해서 저장하게 수정
 * 25. 2. 14.		durururuk		userName -> username 오타 수정
 * 25. 2. 17.		durururuk		userName -> username 오타 수정
 * 25. 2. 17.		durururuk		username 중복 체크 SQL count(*) -> EXISTS()로 수정
 * 25. 2. 17.		durururuk		Java 컨벤션에 맞게 username -> userName 수정
 * 25. 2. 18.		inari		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 19.		durururuk		회원가입 시 UserRole에 기본값 인서트되게 기능 추가
 * 25. 2. 20.		durururuk		회원가입 시 JWT를 담은 헤더를 같이 반환하도록 추가
 * 25. 2. 21.		durururuk		사용자명 중복체크 메서드명 더 명확하게 수정, 반대로 작동하던 로직 수정
 * 25. 2. 21.		durururuk		사용자명 중복체크 예외 처리 추가
 * 25. 2. 21.		durururuk		중복체크 실패 시 예외처리 -> data : false로 다시 변경
 * 25. 2. 24.		inari		자바독 주석 추가
 * 25. 2. 24.		durururuk		회원가입 시 인증 헤더 -> http-only 쿠키 방식으로 변경
 * 25. 2. 25.		durururuk		로그인 서비스, 컨트롤러 추가
 * 25. 2. 26.		durururuk		로그인 서비스 단위테스트 작성
 * 25. 2. 26.		durururuk		로그인 시 비활성유저인지 확인하는 로직 추가
 * 25. 3. 4.		durururuk		이메일 인증 요청 기능 추가
 * 25. 3. 4.		durururuk		이메일 요청 검증 기능 추가
 * 25. 3. 5.		durururuk		이메일 관련 기능 user 도메인에서 분리
 * 25. 3. 11.		durururuk		비밀번호 변경 기능 작성
 * 25. 3. 11.		durururuk		이메일 전송 로직 분리
 * 25. 3. 11.		durururuk		비밀번호 변경 컨트롤러 작성
 * 25. 3. 14.		durururuk		비밀번호 찾기 기능 리팩터링
 * 25. 3. 14.		durururuk		예외처리 필터 작성
 * 25. 3. 14.		durururuk		리팩터링
 * 25. 3. 14.		durururuk		유저명 사용가능할 시 userNameToken 쿠키에 추가
 * 25. 3. 14.		durururuk		주석 수정
 * 25. 3. 14.		durururuk		수정된 로직에 맞게 테스트 코드 수정
 * 25. 3. 28.		durururuk		리프레시 토큰 레디스 저장 기능 구현
 * 25. 3. 28.		durururuk		회원가입, 로그인에서 토큰 관련 로직 분리
 * 25. 3. 28.		durururuk		userService에 있던 토큰 관련 로직 JwtService로 이동
 * 25. 3. 28.		durururuk		리프레시 토큰을 통한 액세스 토큰 재발급 기능 구현
 * 25. 3. 29.		durururuk		코드 가독성을 위해 List.of(accessToken, refreshToken) 구조에서 DTO 방식으로 변경
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 * 25. 3. 29.		durururuk		JwtRedisService 테스트 코드 작성
 * 25. 3. 31.		Durururuk		User Entity에 잘못 설정돼있던 타입 timeStamp를 dateTime으로 수정
 * 25. 4. 1.		durururuk		Bean 순환 문제 해결
 * 25. 4. 9.		durururuk		상세 정보 조회 API 추가
 * 25. 4. 9.		durururuk		에러코드 상세하게 변경
 * 25. 4. 10.		durururuk		이메일 토큰 기반 비밀번호 변경 url 변경, 인증 기반 비밀번호 변경 기능 구현
 * 25. 4. 10.		durururuk		이메일 기반 비밀번호 변경 변경된 로직에 맞게 테스트 코드 수정
 * 25. 4. 12.		durururuk		닉네임 변경 기능 구현
 * 25. 4. 12.		durururuk		유저명 변경 기능 구현
 * 25. 4. 28.		inari		완성
 * 25. 4. 29.		durururuk		유저 프로필 조회 기능에서 프로필사진 객체명이 아닌 s3 presigned url을 요청해서 반환합니다.
 * 25. 6. 19.		durururuk		기존 액세스 토큰의 시간 1시간을 그대로 가져오던 이메일 인증 토큰, 유저명 중복 확인 토큰을 각각 처리하게 수정
 * 25. 6. 25.		inari		로그아웃 기능 추가
 * 25. 6. 26.		inari		회원 탈퇴 기능 작성
 * 25. 6. 26.		inari		탈퇴시 간편로그인 삭제
 * 25. 6. 26.		inari		탈퇴시 서비스에서 컨트롤러로 쿠키삭제 처리 피드백 반영
 * 25. 6. 27.		inari		로그인시 마지막 로그인 갱신되도록 수정
 * 25. 7. 1.		durururuk		다른 서비스 클래스에서도 변경된 로직에 맞게끔 수정
 * 25. 7. 30.		durururuk		프로필 이미지 관련 null 체크 추가
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

	private final UserMapper userMapper;
	private final JwtService jwtService;
	private final JwtRedisService jwtRedisService;
	private final UserRoleMapper userRoleMapper;
	private final OAuthMapper oAuthMapper;
	private final ImageS3Service imageS3Service;

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

		// 로그인 시간 업데이트
		userMapper.updateLastLoginByUserId(user.getUserId(), LocalDateTime.now());

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
			String jwt = jwtService.generateTokenWithSubject(userName, JwtExpirationTime.USER_NAME_VERIFICATION_TOKEN);
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
	public DetailedUserInfoDto getDetailedUserInfoByUserId(Long userId) {
		User user = userMapper.findByUserId(userId).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND_USER));
		List<OAuth> oAuthList = oAuthMapper.findByUserId(userId);
		String profileImageUrl = null;
		if (user.getUserProfile() != null) {
			profileImageUrl = imageS3Service.generatePreSignedGetUrl(user.getUserProfile(), userId, "thumbnail");
		}
		return new DetailedUserInfoDto(user.getUserId(), user.getRoleId(), user.getUserName(), user.getNickname(),
			user.getEmail(), profileImageUrl, oAuthList);
	}

	/**
	 * 사이트 헤더에 사용될 유저 프로필 기본 정보를 반환하는 메서드입니다.
	 * 유저가 등록해둔 프로필 사진이 있다면 S3에 Presigned URL을 요청해서 userProfilePic에 할당하고,
	 * 그렇지 않다면 userProfilePic은 null을 할당합니다.
	 * @param userId  유저 ID
	 * @return 유저 프로필 정보를 담은 DTO
	 */
	public UserProfileDto getUserProfile(Long userId) {
		User user = userMapper.findByUserId(userId)
			.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

		String userProfilePicPresignedUrl = null;

		if (user.getUserProfile() != null) {
			userProfilePicPresignedUrl = imageS3Service.generatePreSignedGetUrl(user.getUserProfile(), userId,
				"thumbnail");
		}

		return new UserProfileDto(
			user.getUserId(),
			user.getUserName(),
			user.getNickname(),
			userProfilePicPresignedUrl
		);
	}

	/**
	 * 로그아웃 처리를 수행하는 메서드입니다.
	 * 액세스 토큰을 블랙리스트에 추가하고, 리프레시 토큰을 Redis에서 삭제합니다.
	 * @param accessToken 액세스 토큰
	 * @param refreshToken 리프레시 토큰
	 */
	public void logout(String accessToken, String refreshToken) {
		DecodedJWT decodedAccessToken = jwtService.verifyJwt(accessToken);
		String jti = decodedAccessToken.getId();
		long expirationTime = decodedAccessToken.getExpiresAt().getTime() / 1000 - System.currentTimeMillis() / 1000;
		if (expirationTime > 0) {
			jwtRedisService.addAccessTokenToBlacklist(jti, expirationTime);
		}
		jwtRedisService.deleteRefreshToken(refreshToken);
	}

	/**
	 * 회원탈퇴 처리를 수행하는 메서드입니다.
	 * 사용자의 개인정보를 익명화하고 상태를 탈퇴(0)로 변경합니다.
	 * OAuth 연동 정보도 모두 삭제하며, 해당 사용자의 모든 JWT 토큰을 무효화합니다.
	 * @param userId 탈퇴할 사용자 ID
	 * @param accessToken 현재 액세스 토큰
	 * @param refreshToken 현재 리프레시 토큰
	 */
	public void deleteUser(Long userId, String accessToken, String refreshToken) {
		if (!userMapper.existsByUserId(userId)) {
			throw new ApiException(ErrorCode.NOT_FOUND_USER);
		}

		String randomIdentifier = UUID.randomUUID().toString().substring(0, 8);
		String anonymizedEmail = "deleted_user_" + randomIdentifier + "@deleted.com";
		String anonymizedUserName = "deleted_user_" + randomIdentifier;
		String anonymizedNickname = "탈퇴한 사용자_" + randomIdentifier;
		String randomPassword = UUID.randomUUID().toString();
		String randomSalt = UUID.randomUUID().toString();

		userMapper.deleteUserByUserId(userId, anonymizedEmail, anonymizedUserName,
			anonymizedNickname, randomPassword, randomSalt);

		oAuthMapper.deleteByUserId(userId);

		DecodedJWT decodedAccessToken = jwtService.verifyJwt(accessToken);
		String jti = decodedAccessToken.getId();
		long expirationTime = decodedAccessToken.getExpiresAt().getTime() / 1000 - System.currentTimeMillis() / 1000;
		if (expirationTime > 0) {
			jwtRedisService.addAccessTokenToBlacklist(jti, expirationTime);
		}
		jwtRedisService.deleteRefreshToken(refreshToken);
	}
}
