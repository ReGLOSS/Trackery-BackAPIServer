package com.trackery.trackerybackapiserver.domain.album.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto
 * fileName       : AlbumUpdateDto
 * author         : durururuk
 * date           : 25. 5. 22.
 * description    : 앨범 정보 업데이트에 사용될 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 22.		durururuk		최초 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumUpdateRequestDto {
	/*
	albumId 앨범 ID(필수)
	albumTitle 앨범 제목
	albumDescription 앨범 설명
	isPublic 공개 여부
	 */
	@NotBlank
	private Long albumId;
	private String albumTitle;
	private String albumDescription;
	private Integer isPublic;
}
