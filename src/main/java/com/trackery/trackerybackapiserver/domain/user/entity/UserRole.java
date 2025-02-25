package com.trackery.trackerybackapiserver.domain.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.entity
 * fileName       : UserRole
 * author         : durururuk
 * date           : 25. 2. 19.
 * description    : 사용자와 권한 정보를 연결하는 중간 테이블용 엔티티 클래스입니다.
 * 					한 사용자가 여러 권한을 가질 수 있고, 한 권한은 여러 사용자에게 할당될 수 있습니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 19.        durururuk       최초 생성
 * 25. 2. 24.        inari           상세 주석 추가
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole {

	/**
	 * 사용자-권한 관계의 고유 식별자
	 */
	private Long userRoleId;

	/**
	 * 사용자 고유 식별자
	 */
	private Long userId;

	/**
	 * 권한 고유 식별자
	 */
	private Long roleId;

	@Builder
	public UserRole(Long userId, Long roleId) {
		this.userId = userId;
		this.roleId = roleId;
	}
}
