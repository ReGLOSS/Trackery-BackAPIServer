package com.trackery.trackerybackapiserver.domain.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.enums
 * fileName       : CookieName
 * author         : inari
 * date           : 25. 6. 27.
 * description    : HTTP 쿠키 이름을 정의한 enum 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 27.		inari		최초 생성
 * 25. 6. 27.		inari		쿠키 이름 및 정책 enum으로 수정
 */
@Getter
@RequiredArgsConstructor
public enum CookieName {
	REFRESH_TOKEN("refreshToken"),
	ACCESS_TOKEN("accessToken"),
	SESSION("SESSION"),
	EMAIL_TOKEN("emailToken"),
	USERNAME_TOKEN("userNameToken");

	private final String value;

	/**
	 * 쿠키의 실제 이름(key)에 해당하는 문자열 값을 반환합니다.
	 * <p>
	 * 이 메서드를 오버라이드함으로써, enum 상수를 문자열로 변환할 때
	 * 'REFRESH_TOKEN'과 같은 열거형 이름 대신 "refreshToken"과 같은
	 * 실제 쿠키 이름 값이 사용됩니다.
	 *
	 * @return 쿠키의 이름으로 사용될 문자열
	 */
	// 롬복의 @ToString은 디버깅용 문자열(예: CookieName(value=refreshToken))을 생성하므로,
	// enum 상수를 바로 쿠키 이름 값처럼 사용하기 위해 직접 오버라이드합니다.
	@Override
	public String toString() {
		return value;
	}
}
