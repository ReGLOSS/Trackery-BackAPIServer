package com.trackery.trackerybackapiserver.domain.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.admin.enums.UserStatus;
import com.trackery.trackerybackapiserver.domain.admin.mapper.UserSuspensionMapper;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.tag.mapper.TagMapper;
import com.trackery.trackerybackapiserver.domain.user.enums.UserRole;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.service
 * fileName       : AdminDashboardService
 * author         : inari
 * date           : 25. 9. 27.
 * description    : 관리자 대시보드 비즈니스 로직을 처리하는 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 27.		inari		최초 생성
 * 25. 9. 30.		inari		UserRole enum 통합
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

	private final UserMapper userMapper;
	private final ImageMapper imageMapper;
	private final AlbumMapper albumMapper;
	private final TagMapper tagMapper;
	private final UserSuspensionMapper userSuspensionMapper;

	/**
	 * 관리자 대시보드 기본 통계를 조회합니다 (ADMIN 전용)
	 *
	 * @param adminRoleId 관리자의 역할 ID
	 * @return 대시보드 통계 정보
	 * @throws ApiException ADMIN 권한이 없는 경우
	 */
	public DashboardStatistics getDashboardStatistics(Long adminRoleId) {
		// ADMIN 권한 확인
		UserRole adminRole = UserRole.fromRoleId(adminRoleId);
		if (!adminRole.isAdmin()) {
			throw new ApiException(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES);
		}

		// 기본 통계 조회
		Long totalUsers = userMapper.countAllUsers();
		Long totalImages = imageMapper.countAllImages();
		Long totalAlbums = albumMapper.countAllAlbums();
		Long totalTags = tagMapper.countAllTags();

		// 사용자 현황 통계
		UserStatusStatistics userStatusStats = getUserStatusStatistics();
		RoleStatistics roleStats = getRoleStatistics();

		return new DashboardStatistics(
			totalUsers,
			totalImages,
			totalAlbums,
			totalTags,
			userStatusStats,
			roleStats
		);
	}

	/**
	 * 사용자 상태별 통계를 조회합니다.
	 *
	 * @return 사용자 상태 통계
	 */
	private UserStatusStatistics getUserStatusStatistics() {
		Long activeUsers = userMapper.countUsersByStatus(UserStatus.ACTIVE.getCode());
		Long temporarilySuspendedUsers = userMapper.countUsersByStatus(UserStatus.TEMPORARILY_SUSPENDED.getCode());
		Long permanentlySuspendedUsers = userMapper.countUsersByStatus(UserStatus.PERMANENTLY_SUSPENDED.getCode());
		Long withdrawnUsers = userMapper.countUsersByStatus(UserStatus.WITHDRAWN.getCode());

		Long totalSuspendedUsers = temporarilySuspendedUsers + permanentlySuspendedUsers;

		return new UserStatusStatistics(
			activeUsers,
			temporarilySuspendedUsers,
			permanentlySuspendedUsers,
			totalSuspendedUsers,
			withdrawnUsers
		);
	}

	/**
	 * 역할별 사용자 통계를 조회합니다.
	 *
	 * @return 역할별 통계
	 */
	private RoleStatistics getRoleStatistics() {
		Long regularUsers = userMapper.countUsersByRole(1L);  // USER
		Long managers = userMapper.countUsersByRole(2L);      // MANAGER
		Long admins = userMapper.countUsersByRole(3L);        // ADMIN

		return new RoleStatistics(
			regularUsers,
			managers,
			admins
		);
	}

	/**
	 * 대시보드 통계 정보를 담는 레코드
	 */
	public record DashboardStatistics(
		Long totalUsers,
		Long totalImages,
		Long totalAlbums,
		Long totalTags,
		UserStatusStatistics userStatusStats,
		RoleStatistics roleStats
	) { }

	/**
	 * 사용자 상태별 통계 정보를 담는 레코드
	 */
	public record UserStatusStatistics(
		Long activeUsers,           // status = 1
		Long temporarilySuspendedUsers,  // status = 2
		Long permanentlySuspendedUsers,  // status = 3
		Long totalSuspendedUsers,   // status = 2 + 3
		Long withdrawnUsers        // status = 0
	) { }

	/**
	 * 역할별 사용자 통계 정보를 담는 레코드
	 */
	public record RoleStatistics(
		Long regularUsers,     // roleId = 1
		Long managers,         // roleId = 2
		Long admins          // roleId = 3
	) { }
}
