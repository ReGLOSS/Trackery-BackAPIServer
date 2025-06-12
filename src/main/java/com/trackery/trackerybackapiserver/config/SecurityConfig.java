package com.trackery.trackerybackapiserver.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.trackery.trackerybackapiserver.config.filter.ExceptionHandlerFilter;
import com.trackery.trackerybackapiserver.config.filter.JwtAuthenticationFilter;
import com.trackery.trackerybackapiserver.config.filter.JwtResolverFilter;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : SecurityConfig
 * author         : inari
 * date           : 25. 2. 06.
 * description    : 애플리캐이션의 보안 설정을 정의하는 구성 클래스입니다.
 * 					이 클래스는 Spring Security 관련 설정을 담당합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 06.        inari       최초 생성
 * 25. 2. 21.        durururuk   JWT 추가
 * 25. 2. 21.        inari       상세 주석 추가
 * 25. 2. 27.        inari       간편 로그인 경로 추가
 * 25. 6. 12.        inari       docs 경로 추가
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	/**
	 * JWT 토큰을 검증하는 유틸리티 클래스
	 */
	private final JwtService jwtService;
	private final UserService userService;

	/**
	 * 인증 없이 접근 가능한 공개 API 목록
	 */
	private final String[] publicUri = {
		"/api/home/images",
		"/api/users/register",
		"/api/users/exists/username",
		"/api/users/login",
		"/api/users/oauth/**",
		"/api/users/me/password/email-token",
		"/favicon.ico",
		"/api/mail/**",
		"/api/docs/**"
	};

	/**
	 * 프로젝트의 도메인 주소
	 */
	@Value("${PROJECT_DOMAIN}")
	private String projectDomain;

	/**
	 * 테스트에서 사용될 로컬 도메인 주소
	 */
	@Value("${LOCAL_DOMAIN}")
	private String localDomain;

	/**
	 * 공개 API에 대한 보안 설정을 구성하는 Spring Security의 필터 체인
	 *
	 * @param http Http Security 객체
	 * @return 구성된 SecurityFilterChain
	 * @throws Exception 보안 구성 중 발생할 수 있는 예외
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher(publicUri)
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

	/**
	 * JWT 기반 인증이 필요한 API에 대한 보안 설정을 구성하는 Spring Security의 필터 체인
	 *
	 * @param http Http Security 객체
	 * @return 구성된 SecurityFilterChain
	 * @throws Exception 보안 구성 중 발생할 수 있는 예외
	 */
	@Bean
	@Order(2)
	public SecurityFilterChain protectedFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/error").permitAll().anyRequest().authenticated())
			.addFilterBefore(new JwtAuthenticationFilter(jwtService),
				UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new JwtResolverFilter(jwtService, userService),
				JwtAuthenticationFilter.class)
			.addFilterBefore(new ExceptionHandlerFilter(), JwtResolverFilter.class);

		return http.build();
	}

	/**
	 * CORS (Cross-Origin Resource Sharing) 설정
	 * 다른 도메인에서 온 요청을 허용할지 설정하는 기능
	 *
	 * @return CorsConfigurationSource 객체
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(projectDomain, localDomain));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
		config.setAllowedHeaders(List.of("Content-Type"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
