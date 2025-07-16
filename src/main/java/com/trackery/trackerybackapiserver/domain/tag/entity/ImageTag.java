package com.trackery.trackerybackapiserver.domain.tag.entity;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.entity
 * fileName       : ImageTag
 * author         : inari
 * date           : 25. 7. 3.
 * description    : 이미지와 태그의 연관관계를 나타내는 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 3.		inari		최초 생성
 * 25. 7. 3.		inari		매퍼와 엔티티 추가
 * 25. 7. 8.		inari		이미지 업로드시 일반 태그 추가
 * 25. 7. 11.		inari		수정
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageTag {

	/**
	 * 이미지-태그 연관관계 고유 식별자입니다.
	 */
	private Long imageTagId;

	/**
	 * 이미지 ID입니다.
	 */
	private Long imageId;

	/**
	 * 태그 ID입니다.
	 */
	private Long tagId;

	/**
	 * 연관관계가 생성된 날짜입니다.
	 */
	private LocalDateTime createdAt;

	@Builder
	public ImageTag(Long imageId, Long tagId, LocalDateTime createdAt) {
		this.imageId = imageId;
		this.tagId = tagId;
		this.createdAt = createdAt;
	}
}
