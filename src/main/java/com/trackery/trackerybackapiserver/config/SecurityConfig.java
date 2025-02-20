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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Value("${PROJECT_DOMAIN}")
	private String projectDomain;
	@Value("${LOCAL_DOMAIN}")
	private String localDomain;

	private final JwtUtil jwtUtil;

	public SecurityConfig(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/error").permitAll()
				.requestMatchers("/api/home/images", "/api/users/register", "/api/users/**").permitAll()
				.anyRequest().authenticated())
			.addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
			.cors(cors -> cors.configurationSource(request -> {
				CorsConfiguration config = new CorsConfiguration();
				config.setAllowedOrigins(List.of(projectDomain, localDomain));
				config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
				config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
				return config;
			}))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable);

		return http.build();
	}
}
