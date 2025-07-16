package com.trackery.trackerybackapiserver.config.mybatis.spatial;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * packageName    : com.trackery.trackerybackapiserver.config.mybatis.spatial
 * fileName       : MybatisSpatialConfig
 * author         : durururuk
 * date           : 25. 4. 18.
 * description    : Mybatis에서 Spatial 타입을 사용하기 위한 타입 핸들러입니다.
 * 					<a href="https://github.com/kakawin/mybatis-mysql-spatial">...</a>"
 * 					를 사용했습니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 18.		durururuk		최초 생성
 * 25. 4. 21.		durururuk		mybatis에서 spatial 타입을 사용하기 위한 타입 핸들러 적용
 * 25. 5. 15.		durururuk		이미지 조회 기능 폴더 지정 오류로 안 되던 문제 수정
 * 25. 6. 30.		inari		mybatis 패키지내 spitial 패키지 오타 수정 및 주석 추가 및 코드스멜 제거
 * 25. 7. 7.		inari		tagtype enum으로 관리하도록 설정(DB에서는 숫자로 들어감)
 * 25. 7. 9.		inari		spital 패키지에서 spatial로 이동
 * 25. 7. 9.		inari		MybatisEnumConfig를 분리
 */
@Configuration
public class MybatisSpatialConfig {
	/**
	 * MyBatis에서 공간 데이터 타입을 처리하기 위한 설정 커스터마이저를 제공합니다.
	 * <p>
	 * Geometry와 Point 클래스에 대해 MysqlGeometryTypeHandler를 등록하여
	 * MySQL의 공간 데이터 타입과 JTS Geometry 객체 간의 변환을 지원합니다.
	 *
	 * @return MyBatis 설정 커스터마이저
	 */
	@Bean
	public ConfigurationCustomizer mybatisSpatialConfigurationCustomizer() {
		return configuration -> {
			MysqlGeometryTypeHandler handler = new MysqlGeometryTypeHandler();
			configuration.getTypeHandlerRegistry()
				.register(org.locationtech.jts.geom.Geometry.class, handler.getClass());
			configuration.getTypeHandlerRegistry()
				.register(org.locationtech.jts.geom.Point.class, handler.getClass());
		};
	}
}
