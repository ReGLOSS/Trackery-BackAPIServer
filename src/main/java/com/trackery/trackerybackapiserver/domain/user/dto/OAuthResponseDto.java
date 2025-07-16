package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : OAuthResponseDto
 * author         : inari
 * date           : 25. 2. 25.
 * description    : 간편 로그인 처리 결과를 담는 응답 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 25.		inari		최초 생성
 * 25. 2. 28.		inari		구현중
 * 25. 3. 14.		inari		프론트연동을 위한 추가 및 코드스멜 수정
 * 25. 3. 17.		inari		주석추가
 * 25. 3. 17.		inari		프론트연결을 위한 수정
 * 25. 7. 1.		inari		신규가입 구분할 isNewUser 파라미터 추가
 * 25. 7. 2.		inari		주석 추가
 */
@Getter
@Builder
public class OAuthResponseDto {

	/**
	 * 기존 이메일과 연동된 계정인지 확인합니다.
	 * 이미 데이터베이스에 존재하는 이메일이면 true, 존재하지 않으면 false를 리턴합니다.
	 */
	private boolean isExistingEmail;

	/**
	 * 사용자 이메일입니다.
	 */
	private String email;

	/**
	 * 신규 사용자 여부를 나타냅니다.
	 * 신규 회원가입이면 true, 기존 계정 연동이면 false를 리턴합니다.
	 */
	private boolean isNewUser;
}
