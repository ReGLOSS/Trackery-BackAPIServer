package com.trackery.trackerybackapiserver.domain.admin.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.admin.entity.UserSuspension;
import com.trackery.trackerybackapiserver.domain.admin.enums.SuspensionActionType;
import com.trackery.trackerybackapiserver.domain.admin.enums.UserStatus;
import com.trackery.trackerybackapiserver.domain.admin.mapper.UserSuspensionMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.service
 * fileName       : UserSuspensionService
 * author         : inari
 * date           : 25. 9. 27.
 * description    : 사용자 정지/해제 비즈니스 로직을 처리하는 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 27.		inari		최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSuspensionService {

	private final UserSuspensionMapper userSuspensionMapper;
	private final UserMapper userMapper;

	/**
	 * 사용자를 정지시킵니다.
	 *
	 * @param userId 정지할 사용자 ID
	 * @param suspensionType 정지 유형 (2: 임시정지, 3: 영구정지)
	 * @param endDate 정지 해제 예정일 (임시정지인 경우만)
	 * @param reason 정지 사유
	 * @param adminId 처리한 관리자 ID
	 * @throws ApiException 이미 정지된 사용자이거나 유효하지 않은 요청인 경우
	 */
	@Transactional
	public void suspendUser(Long userId, Integer suspensionType, LocalDate endDate, String reason, Long adminId) {
		// 사용자 존재 여부 확인
		if (!userMapper.existsByUserId(userId)) {
			throw new ApiException(ErrorCode.NOT_FOUND_USER);
		}

		// 이미 정지된 사용자인지 확인
		UserSuspension activeSuspension = userSuspensionMapper.findActiveSuspensionByUserId(userId);
		if (activeSuspension != null) {
			throw new ApiException(ErrorCode.BAD_REQUEST_ALREADY_SUSPENDED);
		}

		// 임시정지인 경우 종료일 필수 검증
		if (suspensionType == UserStatus.TEMPORARILY_SUSPENDED.getCode() && endDate == null) {
			throw new ApiException(ErrorCode.BAD_REQUEST_SUSPENSION_END_DATE_REQUIRED);
		}

		// 영구정지인 경우 종료일 null로 설정
		if (suspensionType == UserStatus.PERMANENTLY_SUSPENDED.getCode()) {
			endDate = null;
		}

		// 정지 이력 생성 및 저장
		UserSuspension suspension = UserSuspension.builder()
			.userId(userId)
			.suspensionType(suspensionType)
			.startDate(LocalDate.now())
			.endDate(endDate)
			.reason(reason)
			.adminId(adminId)
			.actionType(SuspensionActionType.SUSPEND.getActionType())
			.build();

		userSuspensionMapper.insertSuspension(suspension);

		// 사용자 상태 업데이트
		userMapper.updateUserStatus(userId, suspensionType);
	}

	/**
	 * 사용자 정지를 해제합니다.
	 *
	 * @param userId 정지 해제할 사용자 ID
	 * @param adminId 처리한 관리자 ID
	 * @throws ApiException 정지되지 않은 사용자인 경우
	 */
	@Transactional
	public void unsuspendUser(Long userId, Long adminId) {
		// 사용자 존재 여부 확인
		if (!userMapper.existsByUserId(userId)) {
			throw new ApiException(ErrorCode.NOT_FOUND_USER);
		}

		// 현재 정지 상태인지 확인
		UserSuspension activeSuspension = userSuspensionMapper.findActiveSuspensionByUserId(userId);
		if (activeSuspension == null) {
			throw new ApiException(ErrorCode.BAD_REQUEST_ALREADY_ACTIVE);
		}

		// 모든 활성 정지 비활성화
		userSuspensionMapper.deactivateAllSuspensionsByUserId(userId);

		// 정지 해제 로그 생성
		UserSuspension unsuspendLog = UserSuspension.builder()
			.userId(userId)
			.suspensionType(1)  // 해제
			.startDate(LocalDate.now())
			.adminId(adminId)
			.actionType(SuspensionActionType.UNSUSPEND.getActionType())
			.build();
		userSuspensionMapper.insertSuspension(unsuspendLog);

		// 사용자 상태를 정상으로 변경
		userMapper.updateUserStatus(userId, UserStatus.ACTIVE.getCode());
	}

	/**
	 * 사용자의 현재 활성 정지 정보를 조회합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 활성 정지 정보 (없으면 null)
	 */
	public UserSuspension findActiveSuspensionByUserId(Long userId) {
		return userSuspensionMapper.findActiveSuspensionByUserId(userId);
	}

	/**
	 * 사용자별 정지 이력을 조회합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 정지 이력 목록 (최신순)
	 */
	public List<UserSuspension> findSuspensionHistoryByUserId(Long userId) {
		return userSuspensionMapper.findSuspensionHistoryByUserId(userId);
	}

	/**
	 * 관리자별 작업 로그를 조회합니다.
	 *
	 * @param adminId 관리자 ID (null이면 전체 조회)
	 * @return 관리자 작업 로그 목록 (최신순)
	 */
	public List<UserSuspension> findActionLogsByAdminId(Long adminId) {
		return userSuspensionMapper.findActionLogsByAdminId(adminId);
	}

	/**
	 * 정지 이력 통계를 조회합니다.
	 *
	 * @return 총 정지 횟수
	 */
	public Long countTotalSuspensions() {
		return userSuspensionMapper.countTotalSuspensions();
	}

	/**
	 * 현재 정지 중인 사용자 수를 조회합니다.
	 *
	 * @return 정지 중인 사용자 수
	 */
	public Long countCurrentSuspendedUsers() {
		return userSuspensionMapper.countCurrentSuspendedUsers();
	}
}
