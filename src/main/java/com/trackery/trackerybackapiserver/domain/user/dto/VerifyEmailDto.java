package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : VerifyEmailDto
 * author         : durururuk
 * date           : 25. 3. 4.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 4.        durururuk      최초 생성
 */
@Getter
@NoArgsConstructor
public class VerifyEmailDto {
	private String email;
	private String authNumber;
}
