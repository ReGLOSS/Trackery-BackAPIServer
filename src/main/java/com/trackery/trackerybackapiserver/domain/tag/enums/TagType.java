package com.trackery.trackerybackapiserver.domain.tag.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.enums
 * fileName       : TagType
 * author         : narilee
 * date           : 25. 7. 2.
 * description    : 태그 유형을 정의하는 enum입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 2.        narilee       최초 생성
 */
@Getter
@RequiredArgsConstructor
public enum TagType {
    CUSTOM("사용자 정의"),
    LOCATION("위치"),
    SEASON("계절"),
    TIME("시간대"),
    WEATHER("날씨");

    private final String description;
}
