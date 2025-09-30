package com.trackery.trackerybackapiserver.domain.user.enums;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.enums
 * fileName       : UserRole
 * author         : inari
 * date           : 25. 9. 30.
 * description    : 시스템의 모든 사용자 역할을 정의하는 열거형입니다.
 * 					일반 사용자, 매니저, 관리자의 권한과 역할을 관리합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 30.		inari		admin에서 이동
 */
@Getter
public enum UserRole {

	USER(1L, "USER", "일반 사용자"),
	MANAGER(2L, "MANAGER", "매니저 - 일반 사용자 관리"),
	ADMIN(3L, "ADMIN", "관리자 - 전체 시스템 관리");

	private final Long roleId;
	private final String roleName;
	private final String description;

	UserRole(Long roleId, String roleName, String description) {
		this.roleId = roleId;
		this.roleName = roleName;
		this.description = description;
	}

	/**
	 * roleId로 UserRole을 찾는 메서드
	 *
	 * @param roleId 역할 ID
	 * @return 해당하는 UserRole
	 * @throws ApiException 유효하지 않은 역할 ID인 경우
	 */
	public static UserRole fromRoleId(Long roleId) {
		if (roleId == null) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_ROLE);
		}
		for (UserRole role : UserRole.values()) {
			if (role.roleId.equals(roleId)) {
				return role;
			}
		}
		throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_ROLE);
	}

	/**
	 * 일반 사용자인지 확인하는 메서드
	 *
	 * @return 일반 사용자 여부
	 */
	public boolean isUser() {
		return this == USER;
	}

	/**
	 * 매니저 권한이 있는지 확인하는 메서드
	 *
	 * @return 매니저 권한 여부
	 */
	public boolean isManager() {
		return this == MANAGER;
	}

	/**
	 * 관리자 권한이 있는지 확인하는 메서드
	 *
	 * @return 관리자 권한 여부
	 */
	public boolean isAdmin() {
		return this == ADMIN;
	}

	/**
	 * 매니저 이상의 권한이 있는지 확인하는 메서드
	 *
	 * @return 매니저 또는 관리자 권한 여부
	 */
	public boolean isManagerOrAbove() {
		return this == MANAGER || this == ADMIN;
	}

	/**
	 * 특정 역할의 사용자를 관리할 수 있는지 확인하는 메서드
	 *
	 * @param targetRole 대상 사용자의 역할
	 * @return 관리 가능 여부
	 */
	public boolean canManageUser(UserRole targetRole) {
		if (this == MANAGER) {
			// MANAGER는 일반 사용자만 관리 가능
			return targetRole == USER;
		}
		// ADMIN은 모든 사용자 관리 가능
		return this == ADMIN;
	}
}
