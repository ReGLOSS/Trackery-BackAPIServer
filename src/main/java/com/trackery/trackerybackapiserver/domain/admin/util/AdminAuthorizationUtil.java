package com.trackery.trackerybackapiserver.domain.admin.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.trackery.trackerybackapiserver.domain.admin.enums.AdminRole;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.util
 * fileName       : AdminAuthorizationUtil
 * author         : inari
 * date           : 25. 9. 19.
 * description    : 관리자 권한 검증을 위한 유틸리티 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 19.		inari		최초 생성
 * 25. 9. 28.		inari		최초 생성
 */
public class AdminAuthorizationUtil {

	private AdminAuthorizationUtil() {
		// 유틸리티 클래스이므로 인스턴스 생성 방지
	}

	/**
	 * ADMIN 권한을 검증하는 메서드
	 */
	public static void requireAdmin() {
		CustomUserDetails userDetails = getCurrentUser();
		if (userDetails.getRoleId() != AdminRole.ADMIN.getRoleId()) {
			throw new ApiException(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES);
		}
	}

	/**
	 * MANAGER 이상 권한을 검증하는 메서드
	 */
	public static void requireManager() {
		CustomUserDetails userDetails = getCurrentUser();
		if (userDetails.getRoleId() < AdminRole.MANAGER.getRoleId()) {
			throw new ApiException(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES);
		}
	}

	/**
	 * 현재 인증된 사용자 정보를 가져오는 메서드
	 */
	private static CustomUserDetails getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
			throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS_TOKEN_NOT_FOUND);
		}
		return (CustomUserDetails) authentication.getPrincipal();
	}

	/**
	 * 자기 자신을 관리하려는 시도를 방지
	 */
	public static void checkNotSelfManagement(Long adminUserId, Long targetUserId) {
		if (adminUserId.equals(targetUserId)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_CANNOT_MANAGE_SELF);
		}
	}
}
