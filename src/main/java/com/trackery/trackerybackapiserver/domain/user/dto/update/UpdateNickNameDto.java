package com.trackery.trackerybackapiserver.domain.user.dto.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : ChangeNicknameDto
 * author         : durururuk
 * date           : 25. 4. 11.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 11.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class UpdateNickNameDto {
	@NotBlank
	private String nickname;
}
