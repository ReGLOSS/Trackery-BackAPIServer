package com.trackery.trackerybackapiserver.domain.admin.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.admin.enums.AdminRole;
import com.trackery.trackerybackapiserver.domain.admin.enums.UserStatus;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserRoleMapper;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.service
 * fileName       : AdminUserService
 * author         : inari
 * date           : 25. 9. 27.
 * description    : 관리자용 사용자 관리 비즈니스 로직을 처리하는 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 27.		inari		최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

	// 페이지 크기 제한
	private static final int MIN_PAGE_SIZE = 1;
	private static final int MAX_PAGE_SIZE = 100;

	// 역할 ID 상수
	private static final Long USER_ROLE_ID = 1L;
	private static final Long MANAGER_ROLE_ID = 2L;
	private static final Long ADMIN_ROLE_ID = 3L;

	private final UserMapper userMapper;
	private final UserRoleMapper userRoleMapper;

	/**
	 * 관리자 권한에 따른 사용자 목록을 조회합니다.
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @param pageNum 페이지 번호
	 * @param pageSize 페이지 크기
	 * @return 사용자 목록 (페이지네이션)
	 */
	public PageInfo<User> getUserList(Long adminRoleId, int pageNum, int pageSize) {
		// 페이지 크기 제한
		pageSize = Math.max(MIN_PAGE_SIZE, Math.min(MAX_PAGE_SIZE, pageSize));

		// 관리자 역할에 따른 조회 가능한 역할 ID 목록 결정
		List<Long> allowedRoleIds = determineAllowedRoleIds(adminRoleId);

		// 페이지네이션 설정
		PageHelper.startPage(pageNum, pageSize);

		// 사용자 목록 조회
		List<User> users = userMapper.findUsersForAdmin(allowedRoleIds);

		return new PageInfo<>(users);
	}

	/**
	 * 사용자 상세 정보를 조회합니다.
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @param userId 조회할 사용자 ID
	 * @return 사용자 상세 정보
	 * @throws ApiException 사용자를 찾을 수 없거나 권한이 없는 경우
	 */
	public User getUserDetail(Long adminRoleId, Long userId) {
		// 사용자 존재 여부 확인
		Optional<User> userOpt = userMapper.findByUserId(userId);
		if (userOpt.isEmpty()) {
			throw new ApiException(ErrorCode.NOT_FOUND_USER);
		}

		User user = userOpt.get();

		// 관리자가 해당 사용자를 조회할 권한이 있는지 확인
		validateUserAccessPermission(adminRoleId, user);

		return user;
	}

	/**
	 * 사용자 역할을 변경합니다 (ADMIN 전용)
	 *
	 * @param adminUserId 관리자 사용자 ID
	 * @param adminRoleId 관리자의 역할 ID
	 * @param targetUserId 대상 사용자 ID
	 * @param newRoleId 새로운 역할 ID
	 * @throws ApiException 권한이 없거나 유효하지 않은 요청인 경우
	 */
	@Transactional
	public void changeUserRole(Long adminUserId, Long adminRoleId, Long targetUserId, Long newRoleId) {
		// ADMIN 권한 확인
		validateAdminPermission(adminRoleId);

		// 자기 자신의 역할 변경 방지
		validateNotSelfRoleChange(adminUserId, targetUserId);

		// 대상 사용자 존재 여부 확인
		validateUserExists(targetUserId);

		// 새로운 역할 ID 유효성 검증
		validateRoleId(newRoleId);

		// 사용자 역할 업데이트
		updateUserRole(targetUserId, newRoleId);
	}

	/**
	 * 사용자 통계 정보를 조회합니다.
	 *
	 * @return 사용자 통계 정보
	 */
	public UserStatistics getUserStatistics() {
		Long totalUsers = userMapper.countAllUsers();
		Long regularUsers = userMapper.countUsersByRole(USER_ROLE_ID);
		Long managers = userMapper.countUsersByRole(MANAGER_ROLE_ID);
		Long admins = userMapper.countUsersByRole(ADMIN_ROLE_ID);

		Long activeUsers = userMapper.countUsersByStatus(UserStatus.ACTIVE.getCode());
		Long suspendedUsers = calculateSuspendedUsers();
		Long withdrawnUsers = userMapper.countUsersByStatus(UserStatus.WITHDRAWN.getCode());

		return buildUserStatistics(totalUsers, regularUsers, managers, admins,
				activeUsers, suspendedUsers, withdrawnUsers);
	}

	/**
	 * 관리자 역할에 따라 조회 가능한 역할 ID 목록을 결정합니다.
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @return 조회 가능한 역할 ID 목록
	 */
	private List<Long> determineAllowedRoleIds(Long adminRoleId) {
		AdminRole adminRole = AdminRole.fromRoleId(adminRoleId.intValue());

		if (adminRole == AdminRole.MANAGER) {
			// MANAGER는 일반 사용자만 조회 가능
			return List.of(USER_ROLE_ID);
		} else if (adminRole == AdminRole.ADMIN) {
			// ADMIN은 모든 사용자 조회 가능
			return List.of(USER_ROLE_ID, MANAGER_ROLE_ID, ADMIN_ROLE_ID);
		}

		throw new ApiException(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES);
	}

	/**
	 * 사용자 접근 권한을 검증합니다.
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @param user 확인할 사용자
	 * @throws ApiException 권한이 없는 경우
	 */
	private void validateUserAccessPermission(Long adminRoleId, User user) {
		AdminRole adminRole = AdminRole.fromRoleId(adminRoleId.intValue());
		if (!adminRole.canManageUser(user.getRoleId().intValue())) {
			throw new ApiException(ErrorCode.FORBIDDEN_CANNOT_MANAGE_HIGHER_ROLE);
		}
	}

	/**
	 * 관리자 권한을 검증합니다.
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @throws ApiException ADMIN 권한이 없는 경우
	 */
	private void validateAdminPermission(Long adminRoleId) {
		AdminRole adminRole = AdminRole.fromRoleId(adminRoleId.intValue());
		if (!adminRole.isAdmin()) {
			throw new ApiException(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES);
		}
	}

	/**
	 * 자기 자신의 역할 변경을 방지합니다.
	 *
	 * @param adminUserId 관리자 사용자 ID
	 * @param targetUserId 대상 사용자 ID
	 * @throws ApiException 자기 자신의 역할을 변경하려는 경우
	 */
	private void validateNotSelfRoleChange(Long adminUserId, Long targetUserId) {
		if (adminUserId.equals(targetUserId)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_CANNOT_CHANGE_OWN_ROLE);
		}
	}

	/**
	 * 사용자 존재 여부를 확인합니다.
	 *
	 * @param targetUserId 대상 사용자 ID
	 * @throws ApiException 사용자가 존재하지 않는 경우
	 */
	private void validateUserExists(Long targetUserId) {
		if (!userMapper.existsByUserId(targetUserId)) {
			throw new ApiException(ErrorCode.NOT_FOUND_USER);
		}
	}

	/**
	 * 역할 ID 유효성을 검증합니다.
	 *
	 * @param roleId 확인할 역할 ID
	 * @throws ApiException 유효하지 않은 역할 ID인 경우
	 */
	private void validateRoleId(Long roleId) {
		if (!isValidUserRole(roleId)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_ROLE);
		}
	}

	/**
	 * 역할 ID가 일반 사용자에게 할당 가능한 유효한 역할인지 확인합니다.
	 *
	 * @param roleId 확인할 역할 ID
	 * @return 유효한 역할인 경우 true
	 */
	private boolean isValidUserRole(Long roleId) {
		return USER_ROLE_ID.equals(roleId) || MANAGER_ROLE_ID.equals(roleId);
	}

	/**
	 * 사용자 역할을 업데이트합니다.
	 *
	 * @param targetUserId 대상 사용자 ID
	 * @param newRoleId 새로운 역할 ID
	 */
	private void updateUserRole(Long targetUserId, Long newRoleId) {
		userRoleMapper.updateUserRole(targetUserId, newRoleId);
		userMapper.updateUserRoleId(targetUserId, newRoleId);
	}

	/**
	 * 정지된 사용자 수를 계산합니다.
	 *
	 * @return 정지된 사용자 수
	 */
	private Long calculateSuspendedUsers() {
		return userMapper.countUsersByStatus(UserStatus.TEMPORARILY_SUSPENDED.getCode())
				+ userMapper.countUsersByStatus(UserStatus.PERMANENTLY_SUSPENDED.getCode());
	}

	/**
	 * 사용자 통계 정보 객체를 생성합니다.
	 *
	 * @param totalUsers 전체 사용자 수
	 * @param regularUsers 일반 사용자 수
	 * @param managers 매니저 수
	 * @param admins 관리자 수
	 * @param activeUsers 활성 사용자 수
	 * @param suspendedUsers 정지된 사용자 수
	 * @param withdrawnUsers 탈퇴한 사용자 수
	 * @return 사용자 통계 정보
	 */
	private UserStatistics buildUserStatistics(Long totalUsers, Long regularUsers,
			Long managers, Long admins, Long activeUsers, Long suspendedUsers, Long withdrawnUsers) {
		return new UserStatistics(
			totalUsers,
			regularUsers,
			managers,
			admins,
			activeUsers,
			suspendedUsers,
			withdrawnUsers
		);
	}

	/**
	 * 사용자 통계 정보를 담는 레코드
	 */
	public record UserStatistics(
		Long totalUsers,
		Long regularUsers,
		Long managers,
		Long admins,
		Long activeUsers,
		Long suspendedUsers,
		Long withdrawnUsers
	) { }
}
