package com.trackery.trackerybackapiserver.domain.tag.entity;

import java.time.LocalDateTime;

import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.entity
 * fileName       : Tag
 * author         : inari
 * date           : 25. 7. 3.
 * description    : 태그의 기본 정보를 나타내는 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 3.		inari		최초 생성
 * 25. 7. 3.		inari		매퍼와 엔티티 추가
 * 25. 7. 7.		inari		tagtype enum으로 관리하도록 설정(DB에서는 숫자로 들어감)
 * 25. 7. 8.		inari		태그 도메인 구현
 * 25. 7. 11.		inari		수정
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

	/**
	 * 태그의 고유 식별자.
	 */
	private Long tagId;
	/**
	 * 태그명.
	 */
	private String tagName;
	/**
	 * 태그 유형.
	 */
	private TagType tagType;
	/**
	 * 태그 사용 횟수.
	 */
	private Long tagUseCount;
	/**
	 * 태그 생성일시.
	 */
	private LocalDateTime createdAt;
	/**
	 * Tag 생성자.
	 *
	 * @param tagName 태그명
	 * @param tagType 태그 유형
	 * @param tagUseCount 태그 사용 횟수
	 * @param createdAt 생성일시
	 */
	@Builder
	public Tag(final String tagName, final TagType tagType,
			final Long tagUseCount, final LocalDateTime createdAt) {
		this.tagName = tagName;
		this.tagType = tagType;
		this.tagUseCount = tagUseCount;
		this.createdAt = createdAt;
	}
}
