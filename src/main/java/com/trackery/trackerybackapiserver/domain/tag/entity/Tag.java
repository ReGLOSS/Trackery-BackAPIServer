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
 * 25. 7. 3.        inari           최초 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    /**
     * 태그의 고유 식별자입니다.
     */
    private Long tagId;

    /**
     * 태그명입니다.
     */
    private String tagName;

    /**
     * 태그 유형입니다.
     */
    private TagType tagType;

    /**
     * 태그 사용 횟수입니다.
     */
    private Long tagUseCount;

    /**
     * 태그가 생성된 날짜입니다.
     */
    private LocalDateTime createdAt;

    @Builder
    public Tag(String tagName, TagType tagType, Long tagUseCount, LocalDateTime createdAt) {
        this.tagName = tagName;
        this.tagType = tagType;
        this.tagUseCount = tagUseCount;
        this.createdAt = createdAt;
    }
}
