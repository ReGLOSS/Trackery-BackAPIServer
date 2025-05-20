package com.trackery.trackerybackapiserver.domain.album.entity;

import com.trackery.trackerybackapiserver.domain.image.entity.Image;

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
	private Long albumImageId;
	private Album album;
	private Image image;
}
