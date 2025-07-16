package com.trackery.trackerybackapiserver.domain.user.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto.update
 * fileName       : UpdatePasswordDto
 * author         : durururuk
 * date           : 25. 3. 11.
 * description    : 비밀번호 업데이트 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 11.		durururuk		최초 생성
 * 25. 3. 11.		durururuk		비밀번호 변경 컨트롤러 작성
 * 25. 3. 14.		durururuk		비밀번호 찾기 기능 리팩터링
 * 25. 3. 14.		durururuk		리팩터링
 * 25. 4. 10.		durururuk		이메일 토큰 기반 비밀번호 변경 url 변경, 인증 기반 비밀번호 변경 기능 구현
 * 25. 4. 12.		durururuk		유저명 변경 기능 구현
 */
@Getter
@NoArgsConstructor
public class UpdatePasswordDto {
	private String oldPassword;

	@NotBlank
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s])^\\S{16,}$",
		message = "비밀번호는 최소 16자리이며, 대문자, 소문자, 숫자, 밑줄(_)을 제외한 특수문자를 포함해야 합니다."
	)
	private String newPassword;
}
