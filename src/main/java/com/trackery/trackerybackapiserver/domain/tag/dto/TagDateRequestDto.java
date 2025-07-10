package com.trackery.trackerybackapiserver.domain.tag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.dto
 * fileName       : TagDateRequestDto
 * author         : inari
 * date           : 25. 7. 10.
 * description    : 날짜 기반 기본 태그 생성 요청 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 10.        inari       최초 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TagDateRequestDto {

	@NotBlank(message = "날짜는 필수입니다.")
	private String date;
}
