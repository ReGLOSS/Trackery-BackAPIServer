package com.trackery.trackerybackapiserver.domain.album.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto
 * fileName       : AlbumCreateResponseDto
 * author         : durururuk
 * date           : 25. 5. 20.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 20.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class AlbumCreateResponseDto {
	private Long albumId;
	private String albumTitle;
	private String albumDescription;
}
