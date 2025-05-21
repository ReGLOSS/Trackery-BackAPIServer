package com.trackery.trackerybackapiserver.domain.album.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto
 * fileName       : AlbumRegisterDto
 * author         : durururuk
 * date           : 25. 5. 14.
 * description    : 앨범 생성 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class AlbumCreateRequestDto {
	/**
	 * albumTitle : 앨범 제목
	 * albumDescription : 앨범 설명
	 * isPublic : 공개 여부
	 */
	@NotBlank
	private String albumTitle;
	private String albumDescription;
	@NotNull
	private Integer isPublic;
}