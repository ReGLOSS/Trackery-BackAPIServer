package com.trackery.trackerybackapiserver.domain.album.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto
 * fileName       : AlbumImageInsertRequestDto
 * author         : durururuk
 * date           : 25. 5. 21.
 * description    : 앨범 이미지 추가 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class AlbumImageInsertRequestDto {
	/**
	 * albumId 앨범 ID
	 * imageIdList 이미지 ID 리스트
	 */
	@NotNull
	private Long albumId;
	private List<Long> imageIdList;
}
