
package com.trackery.trackerybackapiserver.domain.album.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.entity
 * fileName       : AlbumImage
 * author         : durururuk
 * date           : 25. 5. 14.
 * description    : 앨범에 포함된 이미지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class AlbumImage {
	/**
	 * albumImageId 앨범 이미지 ID
	 * albumId : 앨범 ID
	 * imageId : 이미지 ID
	 */
	private Long albumImageId;
	private Long albumId;
	private Long imageId;

	@Builder
	public AlbumImage(Long albumId, Long imageId) {
		this.albumId = albumId;
		this.imageId = imageId;
	}
}