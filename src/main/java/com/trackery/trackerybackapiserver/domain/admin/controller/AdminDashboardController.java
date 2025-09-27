package com.trackery.trackerybackapiserver.domain.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.admin.dto.response.AdminDashboardResponseDto;
import com.trackery.trackerybackapiserver.domain.admin.dto.response.RoleStatistics;
import com.trackery.trackerybackapiserver.domain.admin.dto.response.UserStatusStatistics;
import com.trackery.trackerybackapiserver.domain.admin.service.AdminDashboardService;
import com.trackery.trackerybackapiserver.domain.admin.util.AdminAuthorizationUtil;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.controller
 * fileName       : AdminDashboardController
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 관리자 대시보드 HTTP 요청을 처리하는 컨트롤러 클래스입니다.
 * 					기본 통계 데이터 조회 기능을 제공합니다. (ADMIN 전용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 * 25. 9. 27.		inari		관리자 대시보드 API 구현
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

	private final AdminDashboardService adminDashboardService;

	/**
	 * 관리자 대시보드 기본 통계를 조회하는 API (ADMIN 전용)
	 * 총 사용자 수, 총 이미지 수, 총 앨범 수, 총 태그 수의 기본 통계와
	 * 역할별 사용자 수, 계정 상태별 분포를 제공합니다.
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @return 대시보드 통계 정보
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<AdminDashboardResponseDto>> getDashboard(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		// ADMIN 권한 확인
		AdminAuthorizationUtil.requireAdmin();

		// 대시보드 통계 조회
		AdminDashboardService.DashboardStatistics dashboardStats
			= adminDashboardService.getDashboardStatistics(userDetails.getRoleId());

		// DTO 변환
		AdminDashboardResponseDto responseDto = convertToDashboardResponseDto(dashboardStats);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, responseDto));
	}

	/**
	 * DashboardStatistics를 AdminDashboardResponseDto로 변환합니다.
	 *
	 * @param dashboardStats 대시보드 통계 정보
	 * @return 대시보드 응답 DTO
	 */
	private AdminDashboardResponseDto convertToDashboardResponseDto(
			AdminDashboardService.DashboardStatistics dashboardStats) {

		// 사용자 상태별 통계 변환
		UserStatusStatistics userStatusStats = UserStatusStatistics.builder()
			.activeUsers(dashboardStats.userStatusStats().activeUsers())
			.suspendedUsers(dashboardStats.userStatusStats().totalSuspendedUsers())
			.withdrawnUsers(dashboardStats.userStatusStats().withdrawnUsers())
			.build();

		// 역할별 통계 변환
		RoleStatistics roleStats = RoleStatistics.builder()
			.regularUsers(dashboardStats.roleStats().regularUsers())
			.managers(dashboardStats.roleStats().managers())
			.admins(dashboardStats.roleStats().admins())
			.build();

		return AdminDashboardResponseDto.builder()
			.totalUsers(dashboardStats.totalUsers())
			.totalImages(dashboardStats.totalImages())
			.totalAlbums(dashboardStats.totalAlbums())
			.totalTags(dashboardStats.totalTags())
			.userStatusStats(userStatusStats)
			.roleStats(roleStats)
			.build();
	}
}
