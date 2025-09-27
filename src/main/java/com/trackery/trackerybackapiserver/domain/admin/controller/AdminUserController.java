package com.trackery.trackerybackapiserver.domain.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.admin.dto.request.UserRoleChangeRequestDto;
import com.trackery.trackerybackapiserver.domain.admin.dto.request.UserSuspensionRequestDto;
import com.trackery.trackerybackapiserver.domain.admin.dto.response.AdminUserDetailResponseDto;
import com.trackery.trackerybackapiserver.domain.admin.dto.response.AdminUserListResponseDto;
import com.trackery.trackerybackapiserver.domain.admin.dto.response.UserSuspensionHistoryResponseDto;
import com.trackery.trackerybackapiserver.domain.admin.dto.response.UserSuspensionInfo;
import com.trackery.trackerybackapiserver.domain.admin.entity.UserSuspension;
import com.trackery.trackerybackapiserver.domain.admin.enums.AdminRole;
import com.trackery.trackerybackapiserver.domain.admin.enums.SuspensionActionType;
import com.trackery.trackerybackapiserver.domain.admin.enums.UserStatus;
import com.trackery.trackerybackapiserver.domain.admin.service.AdminUserService;
import com.trackery.trackerybackapiserver.domain.admin.service.UserSuspensionService;
import com.trackery.trackerybackapiserver.domain.admin.util.AdminAuthorizationUtil;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.controller
 * fileName       : AdminUserController
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 관리자용 사용자 관리 HTTP 요청을 처리하는 컨트롤러 클래스입니다.
 * 					사용자 목록 조회, 상세 조회, 정지/해제, 역할 변경, 정지 이력 조회 기능을 제공합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 * 25. 9. 24.		inari		사용자 목록 조회 API 구현
 * 25. 9. 25.		inari		정지 이력 조회 API 구현
 * 25. 9. 26.		inari		관리자 작업 로그 조회 API 구현
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

	private final AdminUserService adminUserService;
	private final UserSuspensionService userSuspensionService;

	/**
	 * 사용자 목록을 조회하는 API
	 * MANAGER: roleId=1 사용자만 조회 가능
	 * ADMIN: 모든 사용자 조회 가능
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @param pageNum 페이지 번호 (기본값: 1)
	 * @param pageSize 페이지 크기 (기본값: 10, 최대: 100)
	 * @return 사용자 목록 (페이지네이션)
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<PageInfo<AdminUserListResponseDto>>> getUsers(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(defaultValue = "10") int pageSize) {

		AdminAuthorizationUtil.requireManager();

		int validatedPageSize = validatePageSize(pageSize);
		PageInfo<User> userPageInfo = adminUserService.getUserList(userDetails.getRoleId(), pageNum, validatedPageSize);
		PageInfo<AdminUserListResponseDto> responsePageInfo = convertToUserListPageInfo(userPageInfo);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, responsePageInfo));
	}

	/**
	 * 사용자 상세 정보를 조회하는 API
	 * 관리자 권한에 따라 조회 가능한 사용자가 제한됩니다.
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @param userId 조회할 사용자 ID
	 * @return 사용자 상세 정보
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<AdminUserDetailResponseDto>> getUserDetail(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long userId) {

		// MANAGER 이상 권한 확인
		AdminAuthorizationUtil.requireManager();

		// 사용자 상세 정보 조회
		User user = adminUserService.getUserDetail(userDetails.getRoleId(), userId);

		// 현재 정지 정보 조회
		UserSuspension currentSuspension = userSuspensionService.findActiveSuspensionByUserId(userId);

		// DTO 변환
		AdminUserDetailResponseDto responseDto = convertToUserDetailDto(user, currentSuspension);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, responseDto));
	}

	/**
	 * 사용자를 정지시키는 API
	 * MANAGER: roleId=1 사용자만 정지 가능
	 * ADMIN: 모든 사용자 정지 가능 (단, 자기 자신 제외)
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @param userId 정지할 사용자 ID
	 * @param request 정지 요청 정보
	 * @return 정지 처리 결과
	 */
	@PostMapping("/{userId}/suspend")
	public ResponseEntity<ApiResponse<Void>> suspendUser(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long userId,
			@Valid @RequestBody UserSuspensionRequestDto request) {

		// MANAGER 이상 권한 확인
		AdminAuthorizationUtil.requireManager();

		// 자기 자신 정지 방지
		AdminAuthorizationUtil.checkNotSelfManagement(userDetails.getUserId(), userId);

		// 사용자 정지 처리
		userSuspensionService.suspendUser(
			userId,
			request.getSuspensionType(),
			request.getEndDate(),
			request.getReason(),
			userDetails.getUserId()
		);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 사용자 정지를 해제하는 API
	 * MANAGER: roleId=1 사용자만 해제 가능
	 * ADMIN: 모든 사용자 해제 가능
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @param userId 정지 해제할 사용자 ID
	 * @return 정지 해제 처리 결과
	 */
	@PostMapping("/{userId}/unsuspend")
	public ResponseEntity<ApiResponse<Void>> unsuspendUser(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long userId) {

		// MANAGER 이상 권한 확인
		AdminAuthorizationUtil.requireManager();

		// 사용자 정지 해제 처리
		userSuspensionService.unsuspendUser(userId, userDetails.getUserId());

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 사용자 역할을 변경하는 API (ADMIN 전용)
	 * ADMIN만 사용자의 역할을 변경할 수 있습니다.
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @param userId 역할을 변경할 사용자 ID
	 * @param request 역할 변경 요청 정보
	 * @return 역할 변경 처리 결과
	 */
	@PutMapping("/{userId}/role")
	public ResponseEntity<ApiResponse<Void>> changeUserRole(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long userId,
			@Valid @RequestBody UserRoleChangeRequestDto request) {

		// ADMIN 권한 확인
		AdminAuthorizationUtil.requireAdmin();

		// 사용자 역할 변경 처리
		adminUserService.changeUserRole(
			userDetails.getUserId(),
			userDetails.getRoleId(),
			userId,
			request.getRoleId().longValue()
		);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 사용자별 정지 이력을 조회하는 API
	 * 해당 사용자의 모든 정지/해제 이력을 조회합니다.
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @param userId 조회할 사용자 ID
	 * @return 정지 이력 목록
	 */
	@GetMapping("/{userId}/suspension-history")
	public ResponseEntity<ApiResponse<List<UserSuspensionHistoryResponseDto>>> getSuspensionHistory(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long userId) {

		// MANAGER 이상 권한 확인
		AdminAuthorizationUtil.requireManager();

		// 정지 이력 조회
		List<UserSuspension> suspensionHistory = userSuspensionService.findSuspensionHistoryByUserId(userId);

		// DTO 변환
		List<UserSuspensionHistoryResponseDto> responseList = suspensionHistory.stream()
			.map(this::convertToSuspensionHistoryDto)
			.toList();

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, responseList));
	}

	/**
	 * 관리자 작업 로그를 조회하는 API
	 * 관리자별 또는 전체 관리자의 작업 로그를 조회합니다.
	 *
	 * @param userDetails 인증된 관리자 정보
	 * @param adminId 조회할 관리자 ID (없으면 전체 조회)
	 * @param pageNum 페이지 번호 (기본값: 1)
	 * @param pageSize 페이지 크기 (기본값: 10, 최대: 100)
	 * @return 관리자 작업 로그 목록 (페이지네이션)
	 */
	@GetMapping("/logs/admin-actions")
	public ResponseEntity<ApiResponse<PageInfo<UserSuspensionHistoryResponseDto>>> getAdminActionLogs(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(required = false) Long adminId,
			@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(defaultValue = "10") int pageSize) {

		AdminAuthorizationUtil.requireManager();

		int validatedPageSize = validatePageSize(pageSize);

		// 전체 데이터 조회
		List<UserSuspension> allActionLogs = userSuspensionService.findActionLogsByAdminId(adminId);

		// 수동 페이지네이션 적용
		int total = allActionLogs.size();
		int startIndex = (pageNum - 1) * validatedPageSize;
		int endIndex = Math.min(startIndex + validatedPageSize, total);

		List<UserSuspension> pagedActionLogs = startIndex < total
			? allActionLogs.subList(startIndex, endIndex)
			: List.of();

		List<UserSuspensionHistoryResponseDto> responseList = pagedActionLogs.stream()
			.map(this::convertToSuspensionHistoryDto)
			.toList();

		PageInfo<UserSuspensionHistoryResponseDto> responsePageInfo = new PageInfo<>(responseList);
		responsePageInfo.setTotal(total);
		responsePageInfo.setPages((total + validatedPageSize - 1) / validatedPageSize);
		responsePageInfo.setPageNum(pageNum);
		responsePageInfo.setPageSize(validatedPageSize);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, responsePageInfo));
	}

	/**
	 * User 엔티티를 AdminUserListResponseDto로 변환합니다.
	 *
	 * @param user 사용자 엔티티
	 * @return 사용자 목록 응답 DTO
	 */
	private AdminUserListResponseDto convertToUserListDto(User user) {
		// 현재 정지 정보 조회
		UserSuspension currentSuspension = userSuspensionService.findActiveSuspensionByUserId(user.getUserId());

		return AdminUserListResponseDto.builder()
			.userId(user.getUserId())
			.userName(user.getUserName())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.status(convertToUserStatus(user.getStatus()))
			.role(convertToAdminRole(user.getRoleId()))
			.lastLogin(user.getLastLogin())
			.createdAt(user.getStartDate())
			.currentSuspension(currentSuspension != null ? convertToSuspensionInfo(currentSuspension) : null)
			.build();
	}

	/**
	 * User 엔티티를 AdminUserDetailResponseDto로 변환합니다.
	 *
	 * @param user 사용자 엔티티
	 * @param currentSuspension 현재 정지 정보
	 * @return 사용자 상세 응답 DTO
	 */
	private AdminUserDetailResponseDto convertToUserDetailDto(User user, UserSuspension currentSuspension) {
		return AdminUserDetailResponseDto.builder()
			.userId(user.getUserId())
			.userName(user.getUserName())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.status(convertToUserStatus(user.getStatus()))
			.role(convertToAdminRole(user.getRoleId()))
			.lastLogin(user.getLastLogin())
			.createdAt(user.getStartDate())
			.profileImageUrl(user.getUserProfile())
			.currentSuspension(currentSuspension != null ? convertToSuspensionInfo(currentSuspension) : null)
			.activityStats(createDefaultActivityStats())
			.build();
	}

	/**
	 * UserSuspension 엔티티를 UserSuspensionHistoryResponseDto로 변환합니다.
	 *
	 * @param suspension 정지 이력 엔티티
	 * @return 정지 이력 응답 DTO
	 */
	private UserSuspensionHistoryResponseDto convertToSuspensionHistoryDto(UserSuspension suspension) {
		return UserSuspensionHistoryResponseDto.builder()
			.suspensionId(suspension.getSuspensionId())
			.userId(suspension.getUserId())
			.userName("user_" + suspension.getUserId())
			.suspensionType(suspension.getSuspensionType())
			.suspensionTypeDescription(getSuspensionTypeDescription(suspension.getSuspensionType()))
			.startDate(suspension.getStartDate())
			.endDate(suspension.getEndDate())
			.reason(suspension.getReason())
			.adminId(suspension.getAdminId())
			.adminUsername(formatAdminUsername(suspension.getAdminId()))
			.actionType(SuspensionActionType.fromString(suspension.getActionType()))
			.isActive(suspension.getIsActive() == 1)
			.createdAt(suspension.getCreatedAt())
			.build();
	}

	/**
	 * 페이지 크기를 검증하고 제한합니다.
	 *
	 * @param pageSize 요청된 페이지 크기
	 * @return 검증된 페이지 크기 (1-100 범위)
	 */
	private int validatePageSize(int pageSize) {
		return Math.max(1, Math.min(100, pageSize));
	}

	/**
	 * 사용자 목록 페이지 정보를 DTO로 변환합니다.
	 *
	 * @param userPageInfo 사용자 페이지 정보
	 * @return 변환된 DTO 페이지 정보
	 */
	private PageInfo<AdminUserListResponseDto> convertToUserListPageInfo(PageInfo<User> userPageInfo) {
		List<AdminUserListResponseDto> userList = userPageInfo.getList().stream()
			.map(this::convertToUserListDto)
			.toList();

		PageInfo<AdminUserListResponseDto> responsePageInfo = new PageInfo<>(userList);
		responsePageInfo.setTotal(userPageInfo.getTotal());
		responsePageInfo.setPages(userPageInfo.getPages());
		responsePageInfo.setPageNum(userPageInfo.getPageNum());
		responsePageInfo.setPageSize(userPageInfo.getPageSize());

		return responsePageInfo;
	}

	/**
	 * 기본 활동 통계 정보를 생성합니다.
	 *
	 * @return 기본 활동 통계 DTO
	 */
	private AdminUserDetailResponseDto.UserActivityStats createDefaultActivityStats() {
		return AdminUserDetailResponseDto.UserActivityStats.builder()
			.totalImages(0L)
			.totalAlbums(0L)
			.totalTags(0L)
			.build();
	}

	/**
	 * 관리자 ID를 사용자명으로 포맷팅합니다.
	 *
	 * @param adminId 관리자 ID
	 * @return 포맷팅된 관리자 사용자명
	 */
	private String formatAdminUsername(Long adminId) {
		return adminId == 0L ? "system" : "admin_" + adminId;
	}

	/**
	 * 정수 상태 코드를 UserStatus enum으로 변환합니다.
	 *
	 * @param statusCode 상태 코드
	 * @return UserStatus enum
	 */
	private UserStatus convertToUserStatus(Integer statusCode) {
		return UserStatus.fromCode(statusCode);
	}

	/**
	 * 역할 ID를 AdminRole enum으로 변환합니다.
	 *
	 * @param roleId 역할 ID
	 * @return AdminRole enum (일반 사용자인 경우 null)
	 */
	private AdminRole convertToAdminRole(Long roleId) {
		if (roleId == 1L) {
			return null; // 일반 사용자는 null
		}
		return AdminRole.fromRoleId(roleId.intValue());
	}

	/**
	 * UserSuspension을 UserSuspensionInfo로 변환합니다.
	 *
	 * @param suspension 정지 정보
	 * @return UserSuspensionInfo DTO
	 */
	private UserSuspensionInfo convertToSuspensionInfo(UserSuspension suspension) {
		return UserSuspensionInfo.builder()
			.startDate(suspension.getStartDate())
			.endDate(suspension.getEndDate())
			.reason(suspension.getReason())
			.adminUsername(formatAdminUsername(suspension.getAdminId()))
			.build();
	}

	/**
	 * 정지 유형 코드를 설명으로 변환합니다.
	 *
	 * @param suspensionType 정지 유형 코드
	 * @return 정지 유형 설명
	 */
	private String getSuspensionTypeDescription(Integer suspensionType) {
		if (suspensionType == null) {
			return "해제";
		}
		return switch (suspensionType) {
			case 2 -> "임시정지";
			case 3 -> "영구정지";
			default -> "해제";
		};
	}
}
