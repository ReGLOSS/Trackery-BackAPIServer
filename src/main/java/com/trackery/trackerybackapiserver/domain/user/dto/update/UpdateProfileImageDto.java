package com.trackery.trackerybackapiserver.domain.user.dto.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto.update
 * fileName       : UpdateProfileImageDto
 * author         : durururuk
 * date           : 25. 7. 29.
 * description    : 프로필 이미지 업데이트에 사용될 dto
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 29.      durururuk       최초 생성
 */
@Getter
@NoArgsConstructor
public class UpdateProfileImageDto {
	@NotBlank
	private String imageName;
}
