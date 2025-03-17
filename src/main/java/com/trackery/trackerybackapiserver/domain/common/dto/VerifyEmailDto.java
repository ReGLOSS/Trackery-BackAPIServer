package com.trackery.trackerybackapiserver.domain.common.dto;

import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : VerifyEmailDto
 * author         : durururuk
 * date           : 25. 3. 4.
 * description    : 이메일 인증을 위한 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 4.        durururuk      최초 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerifyEmailDto {
	@Email
	private String email;
	private String authNumber;
}
