package com.trackery.trackerybackapiserver.domain.album.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto
 * fileName       : AlbumImageListDto
 * author         : durururuk
 * date           : 25. 5. 14.
 * description    : 앨범 이미지 리스트 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class AlbumImageListDto {
	private List<Long> imageIdList;
	private Long thumbnailImageId;
}
