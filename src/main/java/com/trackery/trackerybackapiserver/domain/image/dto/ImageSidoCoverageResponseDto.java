package com.trackery.trackerybackapiserver.domain.image.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto
 * fileName       : ImageSidoCoverageResponseDto
 * author         : durururuk
 * date           : 25. 9. 5.
 * description    : 시도별 이미지 커버리지 현황을 나타내는 응답 DTO
 * 					- 이미지가 전혀 없는 시도는 응답에 포함되지 않음
 * 					- PARTIAL: 해당 시도의 일부 시군구에만 이미지가 존재
 *                  - COMPLETE: 해당 시도의 모든 시군구에 이미지가 1장 이상 존재
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 5.		durururuk		최초 생성
 * 25. 9. 5.		durururuk		필드 구성
 * 25. 9. 5.		durururuk		javadoc 주석 작성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageSidoCoverageResponseDto {
	/**
	 * 일부 시군구에만 이미지가 있는 시도 ID Set
	 */
	@JsonProperty("PARTIAL")
	private Set<Long> partialSidoIdSet;
	
	/**
	 * 모든 시군구에 이미지가 있는 시도 ID Set
	 */
	@JsonProperty("COMPLETE")
	private Set<Long> completeSidoIdSet;
}
