package com.trackery.trackerybackapiserver.domain.image.dto.internal;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto.internal
 * fileName       : ImageSearchByUserIdDto
 * author         : durururuk
 * date           : 25. 7. 8.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 8.		durururuk		최초 생성
 */
@Getter
@Builder
public class ImageSearchByUserIdDto {
	private Long userId;
	private int pageNum;
	private int pageSize;
	private Long excludeAlbumId;
}
