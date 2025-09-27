package com.trackery.trackerybackapiserver.domain.admin.dto.response;

import java.time.LocalDateTime;

import com.trackery.trackerybackapiserver.domain.admin.enums.AdminRole;
import com.trackery.trackerybackapiserver.domain.admin.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.dto.response
 * fileName       : AdminUserListResponseDto
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 관리자용 사용자 목록 응답 데이터를 담는 DTO 클래스입니다.
 * 					이 클래스는 관리자 페이지에서 사용자 목록을 조회할 때 사용되는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserListResponseDto {

	/**
	 * 사용자 ID
	 */
	private Long userId;

	/**
	 * 사용자명
	 */
	private String userName;

	/**
	 * 이메일
	 */
	private String email;

	/**
	 * 닉네임
	 */
	private String nickname;

	/**
	 * 사용자 상태
	 */
	private UserStatus status;

	/**
	 * 사용자 역할 (roleId가 1인 경우 null, 2 이상인 경우 AdminRole)
	 */
	private AdminRole role;

	/**
	 * 마지막 로그인 시간
	 */
	private LocalDateTime lastLogin;

	/**
	 * 계정 생성일
	 */
	private LocalDateTime createdAt;

	/**
	 * 현재 정지 정보 (정지 상태인 경우만)
	 */
	private UserSuspensionInfo currentSuspension;
}
