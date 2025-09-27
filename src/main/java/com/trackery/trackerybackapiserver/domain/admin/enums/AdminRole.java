package com.trackery.trackerybackapiserver.domain.admin.enums;

import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.enums
 * fileName       : AdminRole
 * author         : inari
 * date           : 25. 9. 19.
 * description    : 관리자 역할을 정의하는 열거형
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 19.		inari		최초 생성
 */
@Getter
public enum AdminRole {

	MANAGER(2, "MANAGER", "일반 사용자 관리"),
	ADMIN(3, "ADMIN", "전체 시스템 관리");

	private final int roleId;
	private final String roleName;
	private final String description;

	AdminRole(int roleId, String roleName, String description) {
		this.roleId = roleId;
		this.roleName = roleName;
		this.description = description;
	}

	/**
	 * roleId로 AdminRole을 찾는 메서드
	 */
	public static AdminRole fromRoleId(int roleId) {
		for (AdminRole role : AdminRole.values()) {
			if (role.roleId == roleId) {
				return role;
			}
		}
		throw new IllegalArgumentException("유효하지 않은 관리자 역할 ID: " + roleId);
	}

	/**
	 * ADMIN 권한이 있는지 확인하는 메서드
	 */
	public boolean isAdmin() {
		return this == ADMIN;
	}

	/**
	 * MANAGER 이상 권한이 있는지 확인하는 메서드
	 */
	public boolean isManagerOrAbove() {
		return this.roleId >= MANAGER.roleId;
	}

	/**
	 * 다른 역할을 관리할 수 있는지 확인하는 메서드
	 */
	public boolean canManage(AdminRole targetRole) {
		if (this == MANAGER) {
			// MANAGER는 일반 사용자(roleId=1)만 관리 가능, 다른 관리자는 관리 불가
			return false;
		}
		// ADMIN은 모든 역할 관리 가능
		return this == ADMIN;
	}

	/**
	 * 특정 roleId의 사용자를 관리할 수 있는지 확인하는 메서드
	 */
	public boolean canManageUser(int targetRoleId) {
		if (this == MANAGER) {
			// MANAGER는 일반 사용자(roleId=1)만 관리 가능
			return targetRoleId == 1;
		}
		// ADMIN은 모든 사용자 관리 가능
		return this == ADMIN;
	}
}
