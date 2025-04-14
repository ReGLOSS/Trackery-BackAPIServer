package com.trackery.trackerybackapiserver.domain.user.dto.update;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto.update
 * fileName       : UpdateEmailDto
 * author         : durururuk
 * date           : 25. 4. 14.
 * description    : 새 이메일을 받을 DTO입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 14.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class UpdateEmailDto {
	@Email
	private String email;
}
