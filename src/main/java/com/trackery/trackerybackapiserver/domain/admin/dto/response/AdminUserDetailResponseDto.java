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
 * fileName       : AdminUserDetailResponseDto
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 관리자용 사용자 상세 정보 응답 데이터를 담는 DTO 클래스입니다.
 * 					이 클래스는 관리자가 특정 사용자의 상세 정보를 조회할 때 사용되는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDetailResponseDto {

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
	 * 프로필 이미지 URL
	 */
	private String profileImageUrl;

	/**
	 * 현재 정지 정보 (정지 상태인 경우만)
	 */
	private UserSuspensionInfo currentSuspension;

	/**
	 * 활동 통계
	 */
	private UserActivityStats activityStats;

	/**
	 * 사용자 활동 통계 내부 클래스
	 */
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserActivityStats {
		/**
		 * 총 업로드한 이미지 수
		 */
		private long totalImages;

		/**
		 * 총 생성한 앨범 수
		 */
		private long totalAlbums;

		/**
		 * 총 생성한 태그 수
		 */
		private long totalTags;
	}
}
