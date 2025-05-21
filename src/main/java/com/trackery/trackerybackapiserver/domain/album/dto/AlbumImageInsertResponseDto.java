package com.trackery.trackerybackapiserver.domain.album.dto;

import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto
 * fileName       : AlbumImageInsertResponseDto
 * author         : durururuk
 * date           : 25. 5. 21.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.		durururuk		최초 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumImageInsertResponseDto {
	private Long albumId;
	private int succeededImageCount;
	private int failedImageCount;
	private Set<Long> succeededImageIds;
	private Map<Long, String> failedImageIds;
}
