package com.trackery.trackerybackapiserver.domain.location.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : CoordinateRequestDto
 * author         : inari
 * date           : 25. 5. 30.
 * description    : 지리 좌표(위도, 경도) 정보를 담는 요청 DTO입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 30.		inari		최초 생성
 * 25. 5. 30.		inari		제작중
 * 25. 6. 10.		inari		location과 통합
 * 25. 6. 15.		inari		체크스타일 수정
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateRequestDto {

	/**
	 * 위도 (Latitude)
	 * 한국 영토 범위: 33.0° ~ 38.7°
	 * - 최남단: 마라도 (약 33.1°N)
	 * - 최북단: 온성군 (약 38.6°N)
	 *
	 * @apiNote WGS84 좌표계 기준
	 */
	@DecimalMin(value = "33.0", message = "위도는 33도 이상이어야 합니다 (한국 남단)")
	@DecimalMax(value = "38.7", message = "위도는 38.7도 이하여야 합니다 (한국 북단)")
	private double latitude;

	/**
	 * 경도 (Longitude)
	 * 한국 영토 범위: 124.5° ~ 131.1°
	 * - 최서단: 백령도 (약 124.6°E)
	 * - 최동단: 독도 (약 131.9°E, 여유값 포함)
	 *
	 * @apiNote WGS84 좌표계 기준
	 */
	@DecimalMin(value = "124.5", message = "경도는 124.5도 이상이어야 합니다 (한국 서단)")
	@DecimalMax(value = "131.1", message = "경도는 131.1도 이하여야 합니다 (한국 동단)")
	private double longitude;
}
