package com.trackery.trackerybackapiserver.domain.tag.dto;

import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.dto
 * fileName       : TagDefaultRequestDto
 * author         : inari
 * date           : 25. 7. 10.
 * description    : 기본 태그 생성 요청 DTO 클래스입니다. 날짜 기반 계절 태그와 좌표 기반 지역 태그를 통합 처리합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 10.        inari       최초 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TagDefaultRequestDto {

	@NotBlank(message = "날짜는 필수입니다.")
	private String date;

	@Valid
	@NotNull(message = "좌표는 필수입니다.")
	private CoordinateDto coordinate;
}
