package com.trackery.trackerybackapiserver.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : ChangePasswordDto
 * author         : durururuk
 * date           : 25. 3. 11.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 11.        durururuk      최초 생성
 */
@Getter
@NoArgsConstructor
public class ChangePasswordDto {
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s])^\\S{16,}$",
		message = "비밀번호는 최소 16자리이며, 대문자, 소문자, 숫자, 밑줄(_)을 제외한 특수문자를 포함해야 합니다."
	)
	private String password;
}
