package com.trackery.trackerybackapiserver.domain.location.dto;

import jakarta.validation.constraints.NotNull;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : CoordinateDto
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 위도, 경도 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 */
public record CoordinateDto(
	@NotNull double latitude,
	@NotNull double longitude
) {
}
