package com.trackery.trackerybackapiserver.domain.location.entity;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Point;

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
	private Point coordinatePointPoint;
	private LocalDateTime firstRegisteredDate;
	private LocalDateTime lastModifiedDate;
	private JusoSigungu sigungu;

	@Builder
	public CoordinatePoint(String coordinatePointName, String coordinatePointDetail, byte coordinatePointType,
		LocalDateTime firstRegisteredDate, LocalDateTime lastModifiedDate, JusoSigungu sigungu, Point coordinatePointPoint) {
		this.coordinatePointName = coordinatePointName;
		this.coordinatePointDetail = coordinatePointDetail;
		this.coordinatePointType = coordinatePointType;
		this.firstRegisteredDate = firstRegisteredDate;
		this.lastModifiedDate = lastModifiedDate;
		this.sigungu = sigungu;
		this.coordinatePointPoint = coordinatePointPoint;
	}
}
