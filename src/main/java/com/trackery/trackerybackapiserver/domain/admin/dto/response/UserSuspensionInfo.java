package com.trackery.trackerybackapiserver.domain.admin.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.dto.response
 * fileName       : UserSuspensionInfo
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 사용자 정지 정보를 담는 DTO 클래스입니다.
 * 					이 클래스는 사용자의 현재 정지 상태 정보를 클라이언트에 전달하는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSuspensionInfo {

	/**
	 * 정지 시작일 (날짜만)
	 */
	private LocalDate startDate;

	/**
	 * 정지 해제 예정일 (날짜만, 임시정지인 경우만)
	 */
	private LocalDate endDate;

	/**
	 * 정지 사유
	 */
	private String reason;

	/**
	 * 처리한 관리자 username (admin_id=0이면 "system")
	 */
	private String adminUsername;
}
