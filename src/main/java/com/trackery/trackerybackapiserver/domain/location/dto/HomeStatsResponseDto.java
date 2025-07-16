package com.trackery.trackerybackapiserver.domain.location.dto;

import java.util.List;

import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : HomeStatsResponseDto
 * author         : inari
 * date           : 25. 6. 14.
 * description    : 홈 화면에서 시도 지도와 사용자 통계를 함께 응답하는 DTO입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 14.		inari		최초 생성
 * 25. 6. 14.		inari		전국지도용 통계 추가
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomeStatsResponseDto {
	private List<JusoSido> sidoList;
	private UserStatsDto stats;
}
