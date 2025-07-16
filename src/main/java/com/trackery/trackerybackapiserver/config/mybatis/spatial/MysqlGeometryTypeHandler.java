package com.trackery.trackerybackapiserver.config.mybatis.spatial;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.config.mybatis.spatial
 * fileName       : MysqlGeometryTypeHandler
 * author         : durururuk
 * date           : 25. 4. 18.
 * description    : MySQL의 WKB(Well-Known Binary) 형식에서 SRID 정보를 추출하고,
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 18.		durururuk		최초 생성
 * 25. 4. 21.		durururuk		mybatis에서 spatial 타입을 사용하기 위한 타입 핸들러 적용
 * 25. 6. 30.		inari		mybatis 패키지내 spitial 패키지 오타 수정 및 주석 추가 및 코드스멜 제거
 * 25. 7. 9.		inari		spital 패키지에서 spatial로 이동
 */
@Slf4j
@MappedTypes({ Geometry.class, Point.class, Polygon.class, LineString.class, LinearRing.class, MultiPoint.class,
	MultiPolygon.class, MultiLineString.class })
public class MysqlGeometryTypeHandler extends BaseTypeHandler<Geometry> {

	/**
	 * ResultSet에서 컬럼명으로 바이트 배열을 가져와 Geometry 객체로 변환합니다.
	 * 
	 * @param paramResultSet 결과 집합
	 * @param paramString 컬럼명
	 * @return 변환된 Geometry 객체 또는 null
	 * @throws SQLException SQL 예외 발생 시
	 */
	@Override
	public Geometry getNullableResult(ResultSet paramResultSet, String paramString) throws SQLException {
		byte[] bytes = paramResultSet.getBytes(paramString);
		return fromMysqlWkb(bytes);
	}

	/**
	 * ResultSet에서 컬럼 인덱스로 바이트 배열을 가져와 Geometry 객체로 변환합니다.
	 * 
	 * @param paramResultSet 결과 집합
	 * @param paramInt 컬럼 인덱스
	 * @return 변환된 Geometry 객체 또는 null
	 * @throws SQLException SQL 예외 발생 시
	 */
	@Override
	public Geometry getNullableResult(ResultSet paramResultSet, int paramInt) throws SQLException {
		byte[] bytes = paramResultSet.getBytes(paramInt);
		return fromMysqlWkb(bytes);
	}

	/**
	 * CallableStatement에서 파라미터 인덱스로 바이트 배열을 가져와 Geometry 객체로 변환합니다.
	 * 
	 * @param paramCallableStatement 호출 가능한 문장
	 * @param paramInt 파라미터 인덱스
	 * @return 변환된 Geometry 객체 또는 null
	 * @throws SQLException SQL 예외 발생 시
	 */
	@Override
	public Geometry getNullableResult(CallableStatement paramCallableStatement, int paramInt) throws SQLException {
		byte[] bytes = paramCallableStatement.getBytes(paramInt);
		return fromMysqlWkb(bytes);
	}

	/**
	 * Geometry 객체를 MySQL WKB 형식으로 변환하여 PreparedStatement에 설정합니다.
	 * 
	 * @param paramPreparedStatement 준비된 문장
	 * @param paramInt 파라미터 인덱스
	 * @param paramT 설정할 Geometry 객체
	 * @param paramJdbcType JDBC 타입
	 * @throws SQLException SQL 예외 발생 시
	 */
	@Override
	public void setNonNullParameter(PreparedStatement paramPreparedStatement, int paramInt, Geometry paramT,
		JdbcType paramJdbcType) throws SQLException {
		byte[] bytes = mysqlWkbFrom(paramT);
		paramPreparedStatement.setBytes(paramInt, bytes);
	}

	/**
	 * MySQL WKB 바이트 배열을 JTS Geometry 객체로 변환합니다.
	 * 
	 * MySQL의 WKB 형식은 처음 4바이트에 SRID 정보를 포함하고 있으며,
	 * 이를 추출하여 적절한 GeometryFactory로 변환합니다.
	 * 
	 * @param bytes MySQL WKB 형식의 바이트 배열
	 * @return 변환된 Geometry 객체 또는 null
	 */
	private Geometry fromMysqlWkb(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		try {
			ByteBuffer sridBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(bytes, 0, 4);
			sridBuffer.position(0);
			int srid = sridBuffer.getInt();
			byte[] geomBytes = ByteBuffer.allocate(bytes.length - 4).order(ByteOrder.LITTLE_ENDIAN)
				.put(bytes, 4, bytes.length - 4).array();
			if (GeoUtil.getFactory().getSRID() != srid) {
				return new WKBReader(GeoUtil.getFactory(srid)).read(geomBytes);
			}
			return GeoUtil.getWkbReader().read(geomBytes);
		} catch (Exception e) {
			log.warn("지오메트리 파싱 중 오류가 발생했습니다", e);
		}
		return null;
	}

	/**
	 * JTS Geometry 객체를 MySQL WKB 형식의 바이트 배열로 변환합니다.
	 * <p>
	 * SRID가 0인 경우 기본값으로 4326(WGS84)을 설정하고,
	 * SRID 정보를 포함한 MySQL WKB 형식으로 변환합니다.
	 * 
	 * @param geometry 변환할 Geometry 객체
	 * @return MySQL WKB 형식의 바이트 배열
	 */
	private byte[] mysqlWkbFrom(Geometry geometry) {
		int srid = geometry.getSRID();
		if (srid == 0) {
			srid = 4326;
			geometry.setSRID(4326);
		}
		byte[] bytes = new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN).write(geometry);
		return ByteBuffer.allocate(bytes.length + 4).order(ByteOrder.LITTLE_ENDIAN).putInt(srid).put(bytes).array();
	}
}
