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
 * 25. 5. 29.		durururuk		내 앨범 간단 조회 기능 구현
 * 25. 6. 11.		durururuk		앨범 이미지 삭제 기능 구현
 * 25. 6. 23.		durururuk		앨범 목록 조회 시 썸네일도 함께 조회할 수 있도록 수정
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
