package com.trackery.trackerybackapiserver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : UserLoginDto
 * author         : durururuk
 * date           : 25. 2. 25.
 * description    : 로그인 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 25.		durururuk		최초 생성
 * 25. 2. 25.		durururuk		로그인 서비스, 컨트롤러 추가
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginDto {
	@NotBlank
	private String userName;
	@NotBlank
	private String password;
}
