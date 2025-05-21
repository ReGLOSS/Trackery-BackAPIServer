package com.trackery.trackerybackapiserver.domain.album.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto
 * fileName       : AlbumCreateResponseDto
 * author         : durururuk
 * date           : 25. 5. 20.
 * description    : 앨범 생성 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 20.		durururuk		최초 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumCreateResponseDto {
	/**
	 * albumId 앨범 ID
	 * albumTitle 앨범 제목
	 * albumDescription 앨범 설명
	 */
	private Long albumId;
	private String albumTitle;
	private String albumDescription;
}
