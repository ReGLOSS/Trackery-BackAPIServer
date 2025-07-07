package com.trackery.trackerybackapiserver.domain.tag.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonValue;

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
	CUSTOM(0, "사용자 정의"),
	LOCATION(1, "위치"),
	SEASON(2, "계절"),
	TIME(3, "시간대"),
	WEATHER(4, "날씨");

	/**
	 * 태그 타입의 고유 코드 (데이터베이스 저장용)
	 */
	@JsonValue
	private final int code;

	/**
	 * 태그 타입의 설명
	 */
	private final String description;

	/**
	 * 정수 코드를 사용해 해당하는 TagType enum을 반환합니다.
	 * 
	 * @param code 태그 타입 코드 (0: CUSTOM, 1: LOCATION, 2: SEASON, 3: TIME, 4: WEATHER)
	 * @return 해당하는 TagType enum 값
	 * @throws IllegalArgumentException 유효하지 않은 코드인 경우
	 */
	public static TagType fromCode(int code) {
		return Arrays.stream(TagType.values())
			.filter(t -> t.getCode() == code)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Invalid TagType code: " + code));
	}
}
