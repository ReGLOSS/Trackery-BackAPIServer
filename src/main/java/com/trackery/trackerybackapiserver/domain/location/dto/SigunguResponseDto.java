package com.trackery.trackerybackapiserver.domain.location.dto;

import java.util.Objects;

import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;

import lombok.Builder;
import lombok.Value;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : CoordinateRequestDto
 * author         : inari
 * date           : 25. 5. 30.
 * description    : 시군구 정보 응답 DTO입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 13.        inari       최초 생성
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
