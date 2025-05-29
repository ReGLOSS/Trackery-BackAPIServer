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
	/**
	 * albumId : 앨범 ID
	 * userId : 앨범을 등록한 유저 ID
	 * albumName : 앨범 제목
	 * albumDescription : 앨범 설명
	 * thumnailImageId : 대표이미지 ID
	 * albumRegDate : 앨범 등록 날짜
	 * albumModDate : 앨범 최종 수정 날짜
	 * isPublic : 공개 여부 (0 : 비공개, 1 : 공개)
	 * isDeleted : 삭제 여부 (0 : 활성, 1 : 삭제)
	 */
	private Long albumId;
	private Long userId;
	private String albumTitle;
	private String albumDescription;
	private Long thumbnailImageId;
	private LocalDateTime albumRegDate;
	private LocalDateTime albumModDate;
	private Integer isPublic;
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
