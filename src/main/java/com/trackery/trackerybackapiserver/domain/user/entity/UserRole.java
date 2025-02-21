package com.trackery.trackerybackapiserver.domain.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.user.entity

 fileName       : UserRole
 author         : durururuk
 date           : 25. 2. 19.
 description    : 유저-역할 중간 테이블 정보 클래스
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 19.        durururuk       최초 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole {
	private Long userRoleId;
	private Long userId;
	private Long roleId;

	@Builder
	public UserRole(Long userId, Long roleId) {
		this.userId = userId;
		this.roleId = roleId;
	}
}
