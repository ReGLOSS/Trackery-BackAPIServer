package com.trackery.trackerybackapiserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 *packageName    : com.trackery.trackerybackapiserver.config

 fileName       : CustomWebSecurity
 author         : durururuk
 date           : 25. 2. 20.
 description    :
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 20.        durururuk       최초 생성
 */
@Configuration
public class CustomWebSecurity {

	@Bean
	public WebSecurityCustomizer ignoringCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/api/users/**");
	}
}
