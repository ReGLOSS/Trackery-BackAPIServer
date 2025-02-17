package com.trackery.trackerybackapiserver.domain.testFeature;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.testFeature

 fileName       : ValidationTestDto
 author         : durururuk
 date           : 25. 2. 17.
 description    : 예외 발생 테스트용 DTO
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 17.        durururuk       최초 생성*/
@Getter
@Setter
@NoArgsConstructor
public class ValidationTestDto {
	@NotBlank
	private String string;
}
