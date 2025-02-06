package com.trackery.trackerybackapiserver.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : SecurityConfig
 * author         : narilee
 * date           : 25. 02. 06.
 * description    : Spring Security 설정을 담당하는 Configuration 클래스입니다.
 * 					JWT 기반의 인증과 CORS 설정을 처리합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 02. 06.        narilee       최초 생성
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CorsProperties corsProperties;

	public SecurityConfig(CorsProperties corsProperties) {
		this.corsProperties = corsProperties;
	}

	/**
	 * Spring Security 필터 체인을 구성합니다.
	 *
	 * @param http Http Security 객체
	 * @return 구성된 SecurityFilterChain
	 * @throws Exception 보안 구성 중 발생할 수 있는 예외
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())  // CSRF 비활성화
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 설정 적용
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/**").permitAll()  // 모든 API 요청 허용
				.anyRequest().authenticated()
			);

		return http.build();
	}

	/**
	 * CORS 설정을 위한 CorsConfigurationSource 빈 구성입니다.
	 *
	 * @return 구성된 CorsConfigurationSource
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

}
