package com.trackery.trackerybackapiserver.domain.user.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.entity
 * fileName       : Role
 * author         : durururuk
 * date           : 25. 2. 19.
 * description    : 시스템에서 사용되는 권한 정보를 담는 엔티티 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 19.		durururuk		최초 생성
 * 25. 2. 19.		durururuk		Role, UserRole 엔티티 추가, User 클래스 생성 정보 추가
 * 25. 2. 24.		Nari-Lee		자바독 주석 추가
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

	/**
	 * 권한 고유 식별자
	 */
	private Long roleId;

	/**
	 * 권한명
	 */
	private String roleName;
}
