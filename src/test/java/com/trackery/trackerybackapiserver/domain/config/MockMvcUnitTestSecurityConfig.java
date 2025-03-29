package com.trackery.trackerybackapiserver.domain.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain
 * fileName       : MockMvcUnitTestSecurityConfig
 * author         : durururuk
 * date           : 25. 3. 29.
 * description    : MockMvc를 이용한 컨트롤러 단위 테스트를 하기위해
 * 					따로 사용될 설정 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 29.        durururuk      최초 생성
 */
@TestConfiguration
public class MockMvcUnitTestSecurityConfig {
	@Bean
	public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.build();
	}
}
