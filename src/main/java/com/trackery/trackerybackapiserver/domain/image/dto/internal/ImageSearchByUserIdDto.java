package com.trackery.trackerybackapiserver.domain.image.dto.internal;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto.internal
 * fileName       : ImageSearchByUserIdDto
 * author         : durururuk
 * date           : 25. 7. 8.
 * description    : userId로 이미지 조회 시 조건을 설정할 수 있는 내부 DTO
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
