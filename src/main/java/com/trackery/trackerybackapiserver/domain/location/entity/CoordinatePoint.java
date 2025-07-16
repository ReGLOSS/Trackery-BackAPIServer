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
 * 25. 4. 15.		durururuk		좌표로 지역 찾아오는 기능 구현
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 4. 23.		durururuk		CoordinatePoint 엔티티 주석 작성
 * 25. 4. 24.		durururuk		클래스 설명 주석 작성
 * 25. 6. 15.		inari		체크스타일 수정
 * 25. 7. 11.		inari		location 도메인의 자바독 누락 및 체크스타일 해결
 */
@Getter
@NoArgsConstructor
public class CoordinatePoint {
	/**
	 * 좌표 식별번호
	 */
	private Long coordinatePointId;
	/**
	 * 좌표 시도 + 시군구 명 (예: 부산광역시 수영구)
	 */
	private String coordinatePointName;
	/**
	 * 좌표 상세 정보
	 */
	private String coordinatePointDetail;
	/**
	 * 좌표 타입 (기본값: 1)
	 */
	private byte coordinatePointType;
	/**
	 * JTS Point 객체로 표현된 좌표 정보
	 */
	private Point coordinatePointPoint;
	/**
	 * 처음 등록된 시간
	 */
	private LocalDateTime firstRegisteredDate;
	/**
	 * 최종 수정 시간
	 */
	private LocalDateTime lastModifiedDate;
	/**
	 * 좌표가 속한 시군구 엔티티
	 */
	private JusoSigungu sigungu;

	@Builder
	public CoordinatePoint(String coordinatePointName, String coordinatePointDetail, byte coordinatePointType,
		LocalDateTime firstRegisteredDate, LocalDateTime lastModifiedDate, JusoSigungu sigungu,
		Point coordinatePointPoint) {
		this.coordinatePointName = coordinatePointName;
		this.coordinatePointDetail = coordinatePointDetail;
		this.coordinatePointType = coordinatePointType;
		this.firstRegisteredDate = firstRegisteredDate;
		this.lastModifiedDate = lastModifiedDate;
		this.sigungu = sigungu;
		this.coordinatePointPoint = coordinatePointPoint;
	}
}
