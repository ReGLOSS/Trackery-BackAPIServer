package com.trackery.trackerybackapiserver.domain.tag.enums;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.enums
 * fileName       : TagTypeHandler
 * author         : inari
 * date           : 25. 7. 7.
 * description    : TagType 열거형과 데이터베이스 정수 값 간의 변환을 담당하는 MyBatis 타입 핸들러입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.		inari		최초 생성
 * 25. 7. 7.		inari		tagtype enum으로 관리하도록 설정(DB에서는 숫자로 들어감)
 */
@MappedTypes(TagType.class)
public class TagTypeHandler extends BaseTypeHandler<TagType> {

	/**
	 * TagType 열거형 값을 데이터베이스에 저장하기 위해 정수 값으로 변환합니다.
	 *
	 * @param ps PreparedStatement 객체
	 * @param index 파라미터 인덱스
	 * @param parameter 변환할 TagType 열거형 값
	 * @param jdbcType JDBC 타입
	 * @throws SQLException SQL 예외
	 */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int index, TagType parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setInt(index, parameter.getCode());
	}

	/**
	 * 컬럼명으로 데이터베이스에서 조회한 정수 값을 TagType 열거형으로 변환합니다.
	 *
	 * @param rs ResultSet 객체
	 * @param columnName 컬럼명
	 * @return 변환된 TagType 열거형 값
	 * @throws SQLException SQL 예외
	 */
	@Override
	public TagType getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return TagType.fromCode(rs.getInt(columnName));
	}

	/**
	 * 컬럼 인덱스로 데이터베이스에서 조회한 정수 값을 TagType 열거형으로 변환합니다.
	 *
	 * @param rs ResultSet 객체
	 * @param columnIndex 컬럼 인덱스
	 * @return 변환된 TagType 열거형 값
	 * @throws SQLException SQL 예외
	 */
	@Override
	public TagType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return TagType.fromCode(rs.getInt(columnIndex));
	}

	/**
	 * CallableStatement에서 조회한 정수 값을 TagType 열거형으로 변환합니다.
	 *
	 * @param cs CallableStatement 객체
	 * @param columnIndex 컬럼 인덱스
	 * @return 변환된 TagType 열거형 값
	 * @throws SQLException SQL 예외
	 */
	@Override
	public TagType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return TagType.fromCode(cs.getInt(columnIndex));
	}
}
