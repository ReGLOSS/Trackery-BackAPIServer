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
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtRedisService;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : SecurityConfig
 * author         : inari
 * date           : 25. 2. 6.
 * description    : 애플리캐이션의 보안 설정을 정의하는 구성 클래스입니다.
 * 					이 클래스는 Spring Security 관련 설정을 담당합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 6.		inari		최초 생성
 * 25. 2. 12.		durururuk		기본 회원가입 기능 구현
 * 25. 2. 12.		durururuk		임시 시큐리티 전체 허용 해제, dto 검증을 위한 spring-boot-starter-validation 종속성 추가
 * 25. 2. 12.		durururuk		UserDto validation 추가
 * 25. 2. 13.		durururuk		기본 로그인폼 비활성화, 응답 확인용 임시 클래스 작성
 * 25. 2. 13.		durururuk		프로젝트 도메인 CORS 허용, 이외 제한 추가, 세션 현재 사용 안함 설정 추가, 추후 JWT 토큰 관련 코드 추가 필요
 * 25. 2. 13.		durururuk		허용 HTTP 메서드에 "PATCH" 추가
 * 25. 2. 13.		durururuk		시큐리티 설정에서 회원가입 url 허용하도록 추가
 * 25. 2. 14.		durururuk		회원가입, 닉네임 중복체크 api 퍼블릭으로 허용, 컨트롤러, 서비스에 주석 추가
 * 25. 2. 14.		inari		랜덤이미지 가져오기 구현
 * 25. 2. 14.		inari		매퍼 오타 수정 및 DB연결 테스트
 * 25. 2. 14.		durururuk		UserController 성공 케이스 테스트 코드 작성
 * 25. 2. 17.		inari		공통응답 테스트코드 추가
 * 25. 2. 18.		inari		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 19.		durururuk		Jwt 적용을 위한 필터 작성, 추가
 * 25. 2. 21.		durururuk		CustomWebSecurityConfig으로 퍼블릭 uri 관리 일원화
 * 25. 2. 24.		inari		자바독 주석 추가
 * 25. 2. 24.		durururuk		회원가입 시 인증 헤더 -> http-only 쿠키 방식으로 변경
 * 25. 2. 24.		durururuk		회원가입 시 인증 헤더 -> http-only 쿠키 방식으로 변경
 * 25. 2. 25.		durururuk		로그인 서비스, 컨트롤러 추가
 * 25. 3. 4.		durururuk		이메일 인증 요청 기능 추가
 * 25. 3. 4.		durururuk		이메일 요청 검증 기능 추가
 * 25. 3. 11.		durururuk		비밀번호 변경 기능 작성
 * 25. 3. 11.		durururuk		비밀번호 변경 컨트롤러 작성
 * 25. 3. 14.		durururuk		예외처리 필터 작성
 * 25. 3. 28.		durururuk		리프레시 토큰 레디스 저장 기능 구현
 * 25. 3. 28.		durururuk		리프레시 토큰을 통한 액세스 토큰 재발급 기능 구현
 * 25. 3. 28.		durururuk		필터 순서 수정
 * 25. 4. 1.		durururuk		Bean 순환 문제 해결
 * 25. 4. 10.		durururuk		이메일 토큰 기반 비밀번호 변경 url 변경, 인증 기반 비밀번호 변경 기능 구현
 * 25. 6. 24.		inari		SecurityConfig publicUri에 docs 경로 추가
 * 25. 9. 19.		inari		jwtRedisService; 매개변수 추가
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	/**
	 * JWT 토큰을 검증하는 유틸리티 클래스
	 */
	private final JwtService jwtService;
	private final JwtRedisService jwtRedisService;
	private final UserService userService;

	/**
	 * 인증 없이 접근 가능한 공개 API 목록
	 */
	private final String[] publicUri = {
		"/api/home/images",
		"/api/users/register",
		"/api/users/exists/username",
		"/api/users/login",
		"/api/users/oauth/login/**",
		"/api/users/oauth/link-account",
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
			.addFilterBefore(new JwtAuthenticationFilter(jwtService, jwtRedisService),
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
