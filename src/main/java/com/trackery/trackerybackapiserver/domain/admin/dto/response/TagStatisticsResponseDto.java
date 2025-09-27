package com.trackery.trackerybackapiserver.domain.admin.dto.response;

import java.time.LocalDateTime;

import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.dto.response
 * fileName       : TagStatisticsResponseDto
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 태그 통계 응답 데이터를 담는 DTO 클래스입니다.
 * 					이 클래스는 관리자가 태그 사용 통계를 조회할 때 사용되는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagStatisticsResponseDto {

	/**
	 * 태그 ID
	 */
	private Long tagId;

	/**
	 * 태그명
	 */
	private String tagName;

	/**
	 * 태그 타입
	 */
	private TagType tagType;

	/**
	 * 태그 사용 빈도 (연결된 이미지 수)
	 */
	private long usageCount;

	/**
	 * 태그 생성일
	 */
	private LocalDateTime createdAt;
}
