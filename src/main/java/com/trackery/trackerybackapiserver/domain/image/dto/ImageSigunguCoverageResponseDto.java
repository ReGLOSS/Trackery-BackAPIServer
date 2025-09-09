package com.trackery.trackerybackapiserver.domain.image.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto
 * fileName       : ImageSigunguCoverageResponseDto
 * author         : durururuk
 * date           : 25. 9. 8.
 * description    : 특정 시도 내에서 사용자가 이미지를 업로드한 시군구 커버리지 정보 응답 DTO
 * 					- 사용자가 해당 시도 내에서 이미지를 업로드한 시군구 ID 목록을 반환
 * 					- 이미지가 전혀 없는 시군구는 응답에 포함되지 않음
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 8.		durururuk       최초 생성
 * 25. 9. 9.		durururuk		필드명 더 직관적으로 변경
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageSigunguCoverageResponseDto {
	/**
	 * 사용자가 이미지를 업로드한 시군구 ID List
	 */
	@JsonProperty("coveredSigunguIds")
	private List<Long> havingImagesSigunguIdList;
}