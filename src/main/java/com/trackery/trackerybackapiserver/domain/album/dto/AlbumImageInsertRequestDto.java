package com.trackery.trackerybackapiserver.domain.album.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto
 * fileName       : AlbumImageInsertRequestDto
 * author         : durururuk
 * date           : 25. 5. 21.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class AlbumImageInsertRequestDto {
	@NotNull
	private Long albumId;
	private List<Long> imageIdList;
}
