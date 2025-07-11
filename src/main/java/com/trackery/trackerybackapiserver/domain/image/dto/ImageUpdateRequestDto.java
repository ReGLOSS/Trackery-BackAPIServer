package com.trackery.trackerybackapiserver.domain.image.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto
 * fileName       : ImageUpdateRequestDto
 * author         : inari
 * date           : 25. 6. 20.
 * description    : 이미지 메타데이터 수정 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 20.        inari       최초 생성
 * 25. 7. 11.        inari       주석 추가 및 체크스타일 수정
 *
 * @param imageName 이미지 이름 (최대 100자)
 * @param imageContent 이미지 설명 (최대 500자)
 * @param imageDate 이미지 촬영 날짜 및 시간
 * @param isPublic 공개 여부 (0: 비공개, 1: 공개)
 * @param latitude 위도 (33.0 ~ 43.0, 한국 영토 범위)
 * @param longitude 경도 (124.0 ~ 132.0, 한국 영토 범위)
 */
@Builder
public record ImageUpdateRequestDto(
	@Size(max = 100, message = "이미지 이름은 100자를 초과할 수 없습니다.")
	String imageName,
	@Size(max = 500, message = "이미지 설명은 500자를 초과할 수 없습니다.")
	String imageContent,
	LocalDateTime imageDate,
	Integer isPublic,
	@DecimalMin(value = "33.0", message = "위도는 33.0 이상이어야 합니다.")
	@DecimalMax(value = "43.0", message = "위도는 43.0 이하여야 합니다.")
	Double latitude,
	@DecimalMin(value = "124.0", message = "경도는 124.0 이상이어야 합니다.")
	@DecimalMax(value = "132.0", message = "경도는 132.0 이하여야 합니다.")
	Double longitude
) {
}
