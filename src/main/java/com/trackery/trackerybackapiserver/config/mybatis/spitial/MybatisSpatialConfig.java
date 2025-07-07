package com.trackery.trackerybackapiserver.config.mybatis.spitial;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.trackery.trackerybackapiserver.domain.tag.enums.TagTypeHandler;

/**
 * packageName    : com.trackery.trackerybackapiserver.config.mybatis.spital
 * fileName       : MybatisSpitalConfig
 * author         : durururuk
 * date           : 25. 4. 18.
 * description    : Mybatis에서 Spatial 타입을 사용하기 위한 타입 핸들러입니다.
 * 					<a href="https://github.com/kakawin/mybatis-mysql-spatial">...</a>"
 * 					를 사용했습니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 18.		durururuk		최초 생성
 * 25. 6. 30.		inari			주석추가
 * 25. 7. 7.		inari			TagTypeHandler 추가
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
			configuration.getTypeHandlerRegistry().register(new TagTypeHandler());
		};
	}
}
