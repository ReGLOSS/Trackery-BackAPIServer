package com.trackery.trackerybackapiserver.domain.album.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto.response
 * fileName       : MyAlbumResponseDto
 * author         : durururuk
 * date           : 25. 5. 29.
 * description    : 내 앨범 조회 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 29.		durururuk		최초 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyAlbumResponseDto {
	private Long userId;
	private Integer albumCount;
	private List<AlbumSimpledResponseDto> albumList;
}
