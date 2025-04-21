package com.trackery.trackerybackapiserver.config.mybatis.spital;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : MysqlGeometryTypeHandler
 * author         : durururuk
 * date           : 25. 4. 18.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 18.		durururuk		최초 생성
 */
@MappedTypes({ Geometry.class, Point.class, Polygon.class, LineString.class, LinearRing.class, MultiPoint.class,
	MultiPolygon.class, MultiLineString.class })
public class MysqlGeometryTypeHandler extends BaseTypeHandler<Geometry> {

	@Override
	public Geometry getNullableResult(ResultSet paramResultSet, String paramString) throws SQLException {
		byte[] bytes = paramResultSet.getBytes(paramString);
		return fromMysqlWkb(bytes);
	}

	@Override
	public Geometry getNullableResult(ResultSet paramResultSet, int paramInt) throws SQLException {
		byte[] bytes = paramResultSet.getBytes(paramInt);
		return fromMysqlWkb(bytes);
	}

	@Override
	public Geometry getNullableResult(CallableStatement paramCallableStatement, int paramInt) throws SQLException {
		byte[] bytes = paramCallableStatement.getBytes(paramInt);
		return fromMysqlWkb(bytes);
	}

	@Override
	public void setNonNullParameter(PreparedStatement paramPreparedStatement, int paramInt, Geometry paramT,
		JdbcType paramJdbcType) throws SQLException {
		byte[] bytes = mysqlWkbFrom(paramT);
		paramPreparedStatement.setBytes(paramInt, bytes);
	}

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
		}
		return null;
	}

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
