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
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 25.        durururuk      최초 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginDto {
	@NotBlank
	private String userName;
	@NotBlank
	private String password;
}
