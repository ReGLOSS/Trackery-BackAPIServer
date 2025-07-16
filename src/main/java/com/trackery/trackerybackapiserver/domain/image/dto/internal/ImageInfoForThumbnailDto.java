package com.trackery.trackerybackapiserver.domain.image.dto.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto.internal
 * fileName       : ImageInfoForThumbnailDto
 * author         : durururuk
 * date           : 25. 7. 7.
 * description    : 썸네일 생성을 위한 이미지 정보 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.		durururuk		최초 생성
 * 25. 7. 7.		durururuk		쿼리가 산발적으로 돼있어서 페이지네이션 정보가 실제 값과 일치하지 않던 문제 수정
 */
@Getter
@NoArgsConstructor
public class ImageInfoForThumbnailDto {
	private Long imageId;
	private Long userId;
	private String imageName;
}
