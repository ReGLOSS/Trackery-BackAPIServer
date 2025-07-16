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
 * 25. 7. 8.		durururuk		내 이미지 조회 시 조회 결과에서 제외될 앨범 ID 파라미터 추가
 * 25. 7. 8.		durururuk		개발 도중 흔적 제거
 */
@Getter
@Builder
public class ImageSearchByUserIdDto {
	private Long userId;
	private int pageNum;
	private int pageSize;
	private Long excludeAlbumId;
}
