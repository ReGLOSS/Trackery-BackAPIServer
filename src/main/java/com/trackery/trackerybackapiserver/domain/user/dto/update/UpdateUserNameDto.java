package com.trackery.trackerybackapiserver.domain.user.dto.update;

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
 */
@Getter
@NoArgsConstructor
public class UpdateUserNameDto {
	private String userName;
}
