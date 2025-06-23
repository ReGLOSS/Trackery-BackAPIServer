package com.trackery.trackerybackapiserver.domain.album.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto.response
 * fileName       : AlbumSimpledResponseDto
 * author         : durururuk
 * date           : 25. 5. 29.
 * description    : 간단 앨범 정보 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 29.		durururuk		최초 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumSimpledResponseDto {
	private Long albumId;
	private String albumTitle;
	private Integer albumImageCount;
	private String albumThumbnailUrl;
	private Integer isPublic;
}
