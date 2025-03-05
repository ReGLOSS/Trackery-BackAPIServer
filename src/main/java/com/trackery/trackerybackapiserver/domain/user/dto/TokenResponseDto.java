package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : TokenResponseDto
 * author         : inari
 * date           : 25. 2. 28.
 * description    : 간편 로그인 인증 과정에서 제공자로부터 받은 액세스 토큰 정보를 담는 클래스입니다.
 *					이 클래스는 간편 로그인에서 발급받은 액세스 토큰을 저장하고 전달하는 역활을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 28.        inari       최초 생성
 * 25. 2. 28.        inari       리프레시 토큰 제거
 * 25. 3. 03.        inari       경로 수정
 */
@Getter
@AllArgsConstructor
public class TokenResponseDto {

	/**
	 * OAuth 제공자로부터 발급받은 액세스 토큰입니다.
	 * 이 토큰은 사용자 정보를 요청하기 위해 OAuth 제공자의 API에 접근할 때 사용됩니다.
	 */
	private String accessToken;
}
