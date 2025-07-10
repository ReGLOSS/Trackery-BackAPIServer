package com.trackery.trackerybackapiserver.domain.image.dto.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto.internal
 * fileName       : ImageInfoForThumbnail
 * author         : durururuk
 * date           : 25. 7. 7.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class ImageInfoForThumbnailDto {
	private Long imageId;
	private Long userId;
	private String imageName;
}
