package com.trackery.trackerybackapiserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class CustomWebSecurityConfig {

	@Bean
	public WebSecurityCustomizer ignoringCustomizer() {
		return web -> web.ignoring()
			.requestMatchers("/error")
			.requestMatchers("/api/home/images", "/api/users/register", "/api/users/exists/username");
	}
}