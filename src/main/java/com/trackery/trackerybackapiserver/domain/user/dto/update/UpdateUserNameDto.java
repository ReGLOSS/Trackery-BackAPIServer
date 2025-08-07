package com.trackery.trackerybackapiserver.domain.user.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto.update
 * fileName       : UpdateUserNameDto
 * author         : durururuk
 * date           : 25. 4. 12.
 * description    : 유저명 수정 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 12.		durururuk		최초 생성
 * 25. 8. 7.		inari			아이디 패턴 추가
 */
@Getter
@NoArgsConstructor
public class UpdateUserNameDto {
	@NotBlank
	@Pattern(regexp = "^[a-zA-Z0-9_]{4,15}$", message = "아이디는 4~15자 길이에 영문 대소문자, 숫자, 밑줄(_)로 작성해주세요.")
	private String userName;
}
