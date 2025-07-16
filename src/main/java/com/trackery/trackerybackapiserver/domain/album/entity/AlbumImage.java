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
 * 25. 5. 14.		durururuk		앨범 기능 기본 entity, mapper 생성
 * 25. 5. 20.		durururuk		앨범 생성 기능 작성
 * 25. 5. 21.		durururuk		앨범에 이미지 추가하는 기능 작성
 * 25. 5. 21.		durururuk		JavaDoc 작성
 * 25. 5. 21.		durururuk		체크스타일 경고 수정
 * 25. 7. 11.		inari		album 도메인의 자바독 누락 및 체크스타일 해결
 */
@Getter
@NoArgsConstructor
public class AlbumImage {
	/** 앨범 이미지 매핑 고유 식별자 */
	private Long albumImageId;
	/** 앨범 ID */
	private Long albumId;
	/** 이미지 ID */
	private Long imageId;

	@Builder
	public AlbumImage(Long albumId, Long imageId) {
		this.albumId = albumId;
		this.imageId = imageId;
	}
}
