package com.trackery.trackerybackapiserver.domain.user.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.user.entity

 fileName       : Role
 author         : durururuk
 date           : 25. 2. 19.
 description    : 역할 (Role) 기본 정보 클래스
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 19.        durururuk       최초 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {
	private Long roleId;
	private String roleName;
}
