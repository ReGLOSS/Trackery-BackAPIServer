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
 * description    : 좌표 엔티티 객체입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 */

/**
 * CoordinatePoint 엔티티 객체입니다.
 *
 * coodinatePointId Long 타입 좌표 식별번호 (예시 : 1L)
 * coordinatePointName 좌표 시도 + 시군구 명 (예시 : 부산광역시 수영구)
 * coordinatePointType 좌표 타입입니다. 기본 상태로 1을 사용중입니다. 추후 구분이 필요하면 주석 수정 부탁드립니다.
 * coordinatePointPoint 좌표의 Point 객체입니다.
 * firstRegisteredDate 처음 등록된 시간입니다.
 * lastModifiedDate 최종 수정 시간입니다.
 * sigungu 좌표가 속한 시군구 엔티티입니다. 시도 정보도 sigungu.getSido() 방식으로 찾을 수 있습니다.
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
