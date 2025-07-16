package com.trackery.trackerybackapiserver.domain.user.dto.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto.update
 * fileName       : UpdateNicknameDto
 * author         : durururuk
 * date           : 25. 4. 11.
 * description    : 유저명 업데이트 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 11.		durururuk		최초 생성
 * 25. 4. 12.		durururuk		닉네임 변경 기능 구현
 * 25. 4. 12.		durururuk		유저명 변경 기능 구현
 * 25. 4. 14.		durururuk		UpdateUserInfo Controller, Serivce 단위테스트 작성
 */
@Getter
@NoArgsConstructor
public class UpdateNicknameDto {
	@NotBlank
	private String nickname;
}
