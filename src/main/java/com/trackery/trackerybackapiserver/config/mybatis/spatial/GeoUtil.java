package com.trackery.trackerybackapiserver.config.mybatis.spatial;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

/**
 * packageName    : com.trackery.trackerybackapiserver.config.mybatis.spatial
 * fileName       : GeoUtil
 * author         : durururuk
 * date           : 25. 4. 18.
 * description    : JTS(Java Topology Suite) 라이브러리를 사용한 공간 데이터 처리 유틸리티 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 18.		durururuk		최초 생성
 * 25. 4. 21.		durururuk		mybatis에서 spatial 타입을 사용하기 위한 타입 핸들러 적용
 * 25. 6. 30.		Nari-Lee		mybatis 패키지내 spitial 패키지 오타 수정 및 주석 추가 및 코드스멜 제거
 * 25. 7. 9.		Nari-Lee		spital 패키지에서 spatial로 이동
 */
public class GeoUtil {
	private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

	private static WKTReader wktReader = new WKTReader(geometryFactory);

	private static WKTWriter wktWriter = new WKTWriter();

	private static WKBReader wkbReader = new WKBReader(geometryFactory);

	private static WKBWriter wkbWriter = new WKBWriter();

	/**
	 * 유틸리티 클래스의 인스턴스화를 방지하는 private 생성자입니다.
	 * 리플렉션을 이용한 강제 접근도 방지합니다.
	 * 
	 * @throws ApiException 유틸리티 클래스 인스턴스화 시도 시 발생
	 */
	private GeoUtil() {
		throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_UTIL_CLASS_INSTANTIATED);
	}

	/**
	 * 기본 SRID(4326, WGS84)를 사용하는 GeometryFactory를 반환합니다.
	 *
	 * @return 기본 SRID(4326)를 사용하는 GeometryFactory
	 */
	public static GeometryFactory getFactory() {
		if (geometryFactory == null) {
			geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
		}
		return geometryFactory;
	}

	/**
	 * 지정된 SRID를 사용하는 GeometryFactory를 생성하여 반환합니다.
	 *
	 * @param srid 공간 참조 시스템 식별자
	 * @return 지정된 SRID를 사용하는 GeometryFactory
	 */
	public static GeometryFactory getFactory(int srid) {
		return new GeometryFactory(new PrecisionModel(), srid);
	}

	/**
	 * WKT(Well-Known Text) 형식의 문자열을 Geometry 객체로 변환하는 Reader를 반환합니다.
	 *
	 * @return WKT 형식을 읽을 수 있는 WKTReader
	 */
	public static WKTReader getWktReader() {
		if (wktReader == null) {
			wktReader = new WKTReader(getFactory());
		}
		return wktReader;
	}

	/**
	 * Geometry 객체를 WKT(Well-Known Text) 형식의 문자열로 변환하는 Writer를 반환합니다.
	 *
	 * @return WKT 형식으로 출력할 수 있는 WKTWriter
	 */
	public static WKTWriter getWktWriter() {
		if (wktWriter == null) {
			wktWriter = new WKTWriter();
		}
		return wktWriter;
	}

	/**
	 * WKB(Well-Known Binary) 형식의 바이트 배열을 Geometry 객체로 변환하는 Reader를 반환합니다.
	 *
	 * @return WKB 형식을 읽을 수 있는 WKBReader
	 */
	public static WKBReader getWkbReader() {
		if (wkbReader == null) {
			wkbReader = new WKBReader(getFactory());
		}
		return wkbReader;
	}

	/**
	 * Geometry 객체를 WKB(Well-Known Binary) 형식의 바이트 배열로 변환하는 Writer를 반환합니다.
	 *
	 * @return WKB 형식으로 출력할 수 있는 WKBWriter
	 */
	public static WKBWriter getWkbWriter() {
		if (wkbWriter == null) {
			wkbWriter = new WKBWriter();
		}
		return wkbWriter;
	}

	/**
	 * 좌표 문자열을 파싱하여 경계 박스를 나타내는 Polygon을 생성합니다.
	 * <p>
	 * 좌표 문자열은 "minX,minY,maxX,maxY" 형식이어야 합니다.
	 *
	 * @param coords 쉼표로 구분된 좌표 문자열 (minX,minY,maxX,maxY)
	 * @return 경계 박스를 나타내는 Polygon 객체
	 */
	public static Polygon getExtent(String coords) {
		String[] split = coords.split(",");
		double minX = Double.parseDouble(split[0]);
		double minY = Double.parseDouble(split[1]);
		double maxX = Double.parseDouble(split[2]);
		double maxY = Double.parseDouble(split[3]);
		Coordinate[] coordinates = new Coordinate[5];
		coordinates[0] = new Coordinate(minX, minY);
		coordinates[1] = new Coordinate(minX, maxY);
		coordinates[2] = new Coordinate(maxX, maxY);
		coordinates[3] = new Coordinate(maxX, minY);
		coordinates[4] = new Coordinate(minX, minY);
		return getFactory().createPolygon(coordinates);
	}

	/**
	 * 좌표 문자열을 파싱하여 Point 객체를 생성합니다.
	 * <p>
	 * 좌표 문자열은 "x,y" 형식이어야 합니다.
	 *
	 * @param coords 쉼표로 구분된 좌표 문자열 (x,y)
	 * @return 지정된 좌표의 Point 객체
	 */
	public static Point getPoint(String coords) {
		String[] split = coords.split(",");
		double x = Double.parseDouble(split[0]);
		double y = Double.parseDouble(split[1]);
		return getFactory().createPoint(new Coordinate(x, y));
	}
}
