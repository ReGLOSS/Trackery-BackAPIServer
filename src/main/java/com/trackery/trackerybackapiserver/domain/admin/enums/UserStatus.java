package com.trackery.trackerybackapiserver.domain.admin.enums;

import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.enums
 * fileName       : UserStatus
 * author         : inari
 * date           : 25. 9. 19.
 * description    : 사용자 상태를 정의하는 열거형
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 19.		inari		최초 생성
 */
@Getter
public enum UserStatus {

	WITHDRAWN(0, "탈퇴"),
	ACTIVE(1, "정상"),
	TEMPORARILY_SUSPENDED(2, "임시정지"),
	PERMANENTLY_SUSPENDED(3, "영구정지");

	private final int code;
	private final String description;

	UserStatus(int code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * 코드 값으로 UserStatus를 찾는 메서드
	 */
	public static UserStatus fromCode(int code) {
		for (UserStatus status : UserStatus.values()) {
			if (status.code == code) {
				return status;
			}
		}
		throw new IllegalArgumentException("유효하지 않은 사용자 상태 코드: " + code);
	}

	/**
	 * 정지 상태인지 확인하는 메서드
	 */
	public boolean isSuspended() {
		return this == TEMPORARILY_SUSPENDED || this == PERMANENTLY_SUSPENDED;
	}

	/**
	 * 활성 상태인지 확인하는 메서드
	 */
	public boolean isActive() {
		return this == ACTIVE;
	}
}
