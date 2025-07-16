package com.trackery.trackerybackapiserver.domain.location.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : UserStatsDto
 * author         : inari
 * date           : 25. 6. 14.
 * description    : 사용자 통계 정보를 담는 DTO입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 14.		inari		최초 생성
 * 25. 6. 16.		inari		전국 지도용 통계 추가
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
	private Long imageCount;
	private Long albumCount;
	private Long sigunguCount;
}
