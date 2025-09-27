package com.trackery.trackerybackapiserver.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.dto.response
 * fileName       : RoleStatistics
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 역할별 사용자 통계 데이터를 담는 DTO 클래스입니다.
 * 					이 클래스는 관리자 대시보드에서 역할별 사용자 통계를 표시하는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleStatistics {

	/**
	 * 일반 사용자 수 (roleId = 1)
	 */
	private long regularUsers;

	/**
	 * 매니저 수 (roleId = 2)
	 */
	private long managers;

	/**
	 * 관리자 수 (roleId = 3)
	 */
	private long admins;
}
