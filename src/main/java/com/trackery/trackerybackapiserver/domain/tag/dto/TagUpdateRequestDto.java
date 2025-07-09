package com.trackery.trackerybackapiserver.domain.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.dto
 * fileName       : TagUpdateRequestDto
 * author         : inari
 * date           : 25. 7. 2.
 * description    : 태그 수정 요청 데이터를 담는 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 2.        inari        최초 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TagUpdateRequestDto {

	private static final int MAX_TAG_NAME_LENGTH = 50;

	/**
	 * 수정할 새로운 태그명입니다.
	 */
	@NotBlank(message = "태그명은 필수입니다.")
	@Size(min = 1, max = MAX_TAG_NAME_LENGTH, message = "태그명은 1자 이상 50자 이하여야 합니다.")
	private String newTagName;

}