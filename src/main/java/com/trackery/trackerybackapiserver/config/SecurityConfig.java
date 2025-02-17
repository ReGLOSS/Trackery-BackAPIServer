package com.trackery.trackerybackapiserver.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Value("${PROJECT_DOMAIN}")
	private String projectDomain;
	@Value("${LOCAL_DOMAIN}")
	private String localDomain;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(request -> {
				CorsConfiguration config = new CorsConfiguration();
				config.setAllowedOrigins(List.of(projectDomain, localDomain));
				config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
				config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
				return config;
			}))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/error").permitAll()
				.requestMatchers("/api/images", "/api/users/register", "/api/users/exists/username").permitAll()
				.anyRequest().authenticated());
		return http.build();
	}
}
