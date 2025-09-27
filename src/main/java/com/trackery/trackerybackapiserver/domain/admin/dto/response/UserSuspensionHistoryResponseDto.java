package com.trackery.trackerybackapiserver.domain.admin.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.trackery.trackerybackapiserver.domain.admin.enums.SuspensionActionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.dto.response
 * fileName       : UserSuspensionHistoryResponseDto
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 사용자 정지 이력 응답 데이터를 담는 DTO 클래스입니다.
 * 					이 클래스는 관리자가 사용자의 정지 이력을 조회할 때 사용되는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSuspensionHistoryResponseDto {

	/**
	 * 정지 이력 ID
	 */
	private Long suspensionId;

	/**
	 * 대상 사용자 ID
	 */
	private Long userId;

	/**
	 * 대상 사용자명
	 */
	private String userName;

	/**
	 * 정지 유형 (2: 임시정지, 3: 영구정지)
	 */
	private Integer suspensionType;

	/**
	 * 정지 유형 설명
	 */
	private String suspensionTypeDescription;

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
	 * 처리한 관리자 ID (0: 시스템 자동)
	 */
	private Long adminId;

	/**
	 * 처리한 관리자 username (admin_id=0이면 "system")
	 */
	private String adminUsername;

	/**
	 * 작업 유형 (SUSPEND, UNSUSPEND)
	 */
	private SuspensionActionType actionType;

	/**
	 * 현재 활성 정지 여부
	 */
	private Boolean isActive;

	/**
	 * 생성 시간
	 */
	private LocalDateTime createdAt;
}
