package com.trackery.trackerybackapiserver.domain.admin.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.entity
 * fileName       : UserSuspension
 * author         : inari
 * date           : 25. 9. 18.
 * description    : 사용자 정지 이력을 관리하는 엔티티
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 18.		inari		최초 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSuspension {

	private Long suspensionId;
	private Long userId;
	private Integer suspensionType;     // 2: 임시정지, 3: 영구정지
	private LocalDate startDate;        // 정지 시작일 (날짜만)
	private LocalDate endDate;          // 정지 해제 예정일 (날짜만)
	private String reason;
	private Long adminId;               // 0: 시스템 자동
	private String actionType;          // SUSPEND, UNSUSPEND
	private Integer isActive;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public UserSuspension(Long userId, Integer suspensionType, LocalDate startDate,
		LocalDate endDate, String reason, Long adminId, String actionType) {
		this.userId = userId;
		this.suspensionType = suspensionType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.reason = reason;
		this.adminId = adminId;
		this.actionType = actionType;
		this.isActive = 1;
	}
}
