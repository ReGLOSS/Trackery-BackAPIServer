package com.trackery.trackerybackapiserver.domain.location.dto;

import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;

import lombok.Builder;
import lombok.Value;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : SigunguResponseDto
 * author         : Nari-Lee
 * date           : 25. 6. 13.
 * description    : 시군구 정보 응답 DTO입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 13.		Nari-Lee		최초 생성
 * 25. 6. 13.		Nari-Lee		시군구 조회 기능 추가
 * 25. 6. 15.		Nari-Lee		체크스타일 수정
 */
@Value
@Builder
public class SigunguResponseDto {

	Long sigunguId;
	String sigunguName;
	Long sidoId;
	String sidoName;

	public static SigunguResponseDto from(JusoSigungu sigungu) {
		return SigunguResponseDto.builder()
			.sigunguId(sigungu.getSigunguId())
			.sigunguName(sigungu.getSigunguName())
			.sidoId(sigungu.getSido().getSidoId())
			.sidoName(sigungu.getSido().getSidoName())
			.build();
	}
}
