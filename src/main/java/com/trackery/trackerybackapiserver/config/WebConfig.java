package com.trackery.trackerybackapiserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.trackery.trackerybackapiserver.domain.user.enums.OAuthProvider;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : WebConfig
 * author         : inari
 * date           : 25. 3. 26.
 * description    : 웹 설정 관련 클래스입니다.
 *                  OAuth 제공자 문자열을 OAuthProvider 열거형으로 변환하는 컨버터를 등록합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 26.		inari		최초 생성
 * 25. 3. 26.		inari		provider enum 적용
 * 25. 6. 12.		inari		index.html을 프론트에서 접근가능하도록 핸들러 및 플러그인 pom.xml에 추가
 * 25. 6. 12.		inari		핸들러에서 컨트롤러로 변경
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * 포맷터 레지스트리에 커스텀 컨버터를 추가합니다.
	 *
	 * @param registry 포맷터 레지스트리
	 */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToOAthProviderConverter());
	}

	/**
	 * 문자열을 OAuthProvider 열거형으로 변환하는 컨버터 클래스입니다.
	 * 대소문자 구분 없이 문자열을 OAuthProvider 열거형으로 변환합니다.
	 * 지원하지 않는 OAuth 제공자가 입력된 경우 IllegalArgumentException을 발생시킵니다.
	 */
	private static class StringToOAthProviderConverter implements Converter<String, OAuthProvider> {

		/**
		 * 문자열을 OAuthProvider 열거형으로 변환합니다.
		 *
		 * @param source 변환할 문자열
		 * @return 변환된 OAuthProvider 열거형
		 * @throws IllegalArgumentException 지원하지 않는 OAuth 제공자가 입력된 경우
		 */
		@Override
		public OAuthProvider convert(String source) {
			try {
				return OAuthProvider.valueOf(source.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("지원하지 않는 OAuth 제공자: " + source);
			}
		}
	}
}
