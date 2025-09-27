package com.trackery.trackerybackapiserver.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.dto.response
 * fileName       : UserStatusStatistics
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 사용자 상태별 통계 데이터를 담는 DTO 클래스입니다.
 * 					이 클래스는 관리자 대시보드에서 사용자 상태별 통계를 표시하는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusStatistics {

	/**
	 * 정상 상태 사용자 수 (status = 1)
	 */
	private long activeUsers;

	/**
	 * 정지된 사용자 수 (status = 2 or 3)
	 */
	private long suspendedUsers;

	/**
	 * 탈퇴한 사용자 수 (status = 0)
	 */
	private long withdrawnUsers;
}
