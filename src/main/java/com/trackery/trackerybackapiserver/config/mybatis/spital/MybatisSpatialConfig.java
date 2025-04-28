package com.trackery.trackerybackapiserver.config.mybatis.spital;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * packageName    : com.trackery.trackerybackapiserver.config.mybatis.spital
 * fileName       : MybatisSpitalConfig
 * author         : durururuk
 * date           : 25. 4. 18.
 * description    : Mybatis에서 Spatial 타입을 사용하기 위한 타입 핸들러입니다.
 * https://github.com/kakawin/mybatis-mysql-spatial
 * 를 사용했습니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 18.		durururuk		최초 생성
 */
@Configuration
public class MybatisSpatialConfig {
	@Bean
	public ConfigurationCustomizer mybatisSpatialConfigurationCustomizer() {
		return configuration -> {
			configuration.getTypeHandlerRegistry()
				.register(org.locationtech.jts.geom.Geometry.class, MysqlGeometryTypeHandler.class);
		};
	}
}
