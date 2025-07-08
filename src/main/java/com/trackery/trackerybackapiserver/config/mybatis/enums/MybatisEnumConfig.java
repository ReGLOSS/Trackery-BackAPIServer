package com.trackery.trackerybackapiserver.config.mybatis.enums;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.trackery.trackerybackapiserver.domain.tag.enums.TagTypeHandler;

/**
 * packageName    : com.trackery.trackerybackapiserver.config.mybatis.enums
 * fileName       : MybatisEnumConfig
 * author         : inari
 * date           : 25. 7. 9.
 * description    : Mybatis에서 Enum 타입을 사용하기 위한 타입 핸들러입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 9.		inari			최초 생성
 */
@Configuration
public class MybatisEnumConfig {
	/**
	 * MyBatis에서 Enum 타입을 처리하기 위한 설정 커스터마이저를 제공합니다.
	 * <p>
	 * Enum 타입에 대한 TypeHandler를 등록하여
	 * 데이터베이스와 Java Enum 객체 간의 변환을 지원합니다.
	 *
	 * @return MyBatis 설정 커스터마이저
	 */
	@Bean
	public ConfigurationCustomizer mybatisEnumConfigurationCustomizer() {
		return configuration -> configuration.getTypeHandlerRegistry().register(new TagTypeHandler());
	}
}
