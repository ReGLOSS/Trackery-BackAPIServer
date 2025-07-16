package com.trackery.trackerybackapiserver.domain.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.enums
 * fileName       : SameSitePolicy
 * author         : Nari-Lee
 * date           : 25. 6. 27.
 * description    : HTTP 쿠키의 SameSite 정책을 정의한 enum 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 27.		Nari-Lee		최초 생성
 * 25. 6. 27.		Nari-Lee		쿠키 이름 및 정책 enum으로 수정
 */
@Getter
@RequiredArgsConstructor
public enum SameSitePolicy {
	STRICT("Strict"),
	LAX("Lax"),
	NONE("None");

	private final String value;

	/**
	 * SameSite 정책의 실제 문자열 값(예: "Strict", "Lax")을 반환합니다.
	 * <p>
	 * 이 메서드를 오버라이드함으로써, enum 상수를 문자열로 변환할 때
	 * 'STRICT'와 같은 열거형 이름 대신 실제 정책 값이 사용됩니다.
	 *
	 * @return "Strict", "Lax", 또는 "None"과 같은 정책의 문자열 값.
	 */
	// 롬복의 @ToString은 디버깅용 문자열(예: CookieName(value=refreshToken))을 생성하므로,
	// enum 상수를 바로 쿠키 이름 값처럼 사용하기 위해 직접 오버라이드합니다.
	@Override
	public String toString() {
		return value;
	}
}
