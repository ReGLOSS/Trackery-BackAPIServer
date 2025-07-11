package com.trackery.trackerybackapiserver.domain.album.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.entity
 * fileName       : Album
 * author         : durururuk
 * date           : 25. 5. 14.
 * description    : 앨범 엔티티
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class Album {
	/** 앨범 고유 식별자 */
	private Long albumId;
	/** 앨범을 소유한 사용자 ID */
	private Long userId;
	/** 앨범 제목 */
	private String albumTitle;
	/** 앨범 설명 */
	private String albumDescription;
	/** 앨범 대표 이미지 ID */
	private Long thumbnailImageId;
	/** 앨범 생성일시 */
	private LocalDateTime albumRegDate;
	/** 앨범 수정일시 */
	private LocalDateTime albumModDate;
	/** 공개 여부 (0: 비공개, 1: 공개) */
	private Integer isPublic;
	/** 삭제 여부 (0: 활성, 1: 삭제) */
	private Integer isDeleted;

	@Builder
	public Album(Long userId, String albumTitle, String albumDescription, Long thumbnailImageId,
		LocalDateTime albumRegDate, LocalDateTime albumModDate, Integer isPublic) {
		this.userId = userId;
		this.albumTitle = albumTitle;
		this.albumDescription = albumDescription;
		this.thumbnailImageId = thumbnailImageId;
		this.albumRegDate = albumRegDate;
		this.albumModDate = albumModDate;
		this.isPublic = isPublic;
	}
}
