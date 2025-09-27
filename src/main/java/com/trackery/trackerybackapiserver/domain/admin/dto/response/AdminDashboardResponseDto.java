package com.trackery.trackerybackapiserver.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.dto.response
 * fileName       : AdminDashboardResponseDto
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 관리자 대시보드 응답 데이터를 담는 DTO 클래스입니다.
 * 					이 클래스는 관리자 대시보드에서 전체 통계 정보를 표시하는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponseDto {

	/**
	 * 총 사용자 수
	 */
	private long totalUsers;

	/**
	 * 총 이미지 수
	 */
	private long totalImages;

	/**
	 * 총 앨범 수
	 */
	private long totalAlbums;

	/**
	 * 총 태그 수
	 */
	private long totalTags;

	/**
	 * 사용자 상태별 통계
	 */
	private UserStatusStatistics userStatusStats;

	/**
	 * 역할별 사용자 통계
	 */
	private RoleStatistics roleStats;
}
