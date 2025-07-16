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
 * 25. 5. 14.		durururuk		앨범 기능 기본 entity, mapper 생성
 * 25. 5. 20.		durururuk		앨범 생성 기능 작성
 * 25. 5. 21.		durururuk		앨범에 이미지 추가하는 기능 작성
 * 25. 5. 21.		durururuk		JavaDoc 작성
 * 25. 7. 11.		Nari-Lee		album 도메인의 자바독 누락 및 체크스타일 해결
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
