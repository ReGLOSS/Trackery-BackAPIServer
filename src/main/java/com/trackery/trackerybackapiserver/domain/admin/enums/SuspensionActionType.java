package com.trackery.trackerybackapiserver.domain.admin.enums;

import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.enums
 * fileName       : SuspensionActionType
 * author         : inari
 * date           : 25. 9. 19.
 * description    : 정지 작업 유형을 정의하는 열거형
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 19.		inari		최초 생성
 */
@Getter
public enum SuspensionActionType {

	SUSPEND("SUSPEND", "정지"),
	UNSUSPEND("UNSUSPEND", "정지 해제");

	private final String actionType;
	private final String description;

	SuspensionActionType(String actionType, String description) {
		this.actionType = actionType;
		this.description = description;
	}

	/**
	 * 문자열로 SuspensionActionType을 찾는 메서드
	 */
	public static SuspensionActionType fromString(String actionType) {
		for (SuspensionActionType type : SuspensionActionType.values()) {
			if (type.actionType.equals(actionType)) {
				return type;
			}
		}
		throw new IllegalArgumentException("유효하지 않은 정지 작업 유형: " + actionType);
	}

	/**
	 * 정지 작업인지 확인하는 메서드
	 */
	public boolean isSuspend() {
		return this == SUSPEND;
	}

	/**
	 * 정지 해제 작업인지 확인하는 메서드
	 */
	public boolean isUnsuspend() {
		return this == UNSUSPEND;
	}
}