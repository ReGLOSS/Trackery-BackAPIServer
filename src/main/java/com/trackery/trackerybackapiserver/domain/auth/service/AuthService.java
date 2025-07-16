package com.trackery.trackerybackapiserver.domain.auth.service;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.auth.dto.AuthUserDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.auth.service
 * fileName       : AuthService
 * author         : durururuk
 * date           : 25. 3. 27.
 * description    : 인증 api에서 사용되는 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 27.		durururuk		최초 생성
 * 25. 3. 27.		durururuk		/api/auth/me api userId, userName, userRoleId도 같이 응답하도록 수정
 * 25. 3. 27.		durururuk		인증 api 단위테스트 수정
 * 25. 3. 27.		durururuk		주석 작성
 */
@Service
public class AuthService {
	/**
	 * CustomDetails에서 유저 인증 정보를 받아서 dto로 변환해주는 메서드
	 *
	 * @param customUserDetails SecurityContextHolder에 들어있던 유저 인증 정보
	 * @return : 변환된 DTO
	 */
	public AuthUserDto toAuthUserDto(CustomUserDetails customUserDetails) {
		return new AuthUserDto(customUserDetails.getUserId(), customUserDetails.getUsername(),
			customUserDetails.getRoleId());
	}
}
