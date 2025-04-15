package com.trackery.trackerybackapiserver.domain.location.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.entity
 * fileName       : CoordinatePoint
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class CoordinatePoint {
	private Long coordinatePointId;
	private String coordinatePointName;
	private String coordinatePointDetail;
	private byte coordinatePointType;
	private LocalDateTime firstRegisterDate;
	private LocalDateTime lastModifiedDate;
	private JusoSigungu sigungu;

	@Builder
	public CoordinatePoint(String coordinatePointName, String coordinatePointDetail, byte coordinatePointType,
		LocalDateTime firstRegisterDate, LocalDateTime lastModifiedDate, JusoSigungu sigungu) {
		this.coordinatePointName = coordinatePointName;
		this.coordinatePointDetail = coordinatePointDetail;
		this.coordinatePointType = coordinatePointType;
		this.firstRegisterDate = firstRegisterDate;
		this.lastModifiedDate = lastModifiedDate;
		this.sigungu = sigungu;
	}
}
