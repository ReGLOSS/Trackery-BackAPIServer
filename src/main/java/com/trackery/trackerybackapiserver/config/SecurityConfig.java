package com.trackery.trackerybackapiserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/**").permitAll()
				.requestMatchers("/").permitAll()
				.anyRequest().permitAll()
			)
			.csrf(csrf -> csrf.disable())
			.formLogin(form -> form.disable()
			).build();
	}
}
