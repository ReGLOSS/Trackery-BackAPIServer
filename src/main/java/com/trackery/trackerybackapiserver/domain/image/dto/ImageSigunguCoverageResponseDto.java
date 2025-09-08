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
 * description    : 이미지가 업로드돼있는 시군구 아이디를 반환하는 DTO
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
	@JsonProperty("SIGUNGU-IDS")
	private List<Long> havingImagesSigunguIdSet;
}