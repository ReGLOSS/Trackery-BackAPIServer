package com.trackery.trackerybackapiserver.domain.config;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.config
 * fileName       : RestDocsConfiguration
 * author         : durururuk
 * date           : 25. 6. 13.
 * description    : Spring Rest Docs 관련 설정 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 13.		durururuk		최초 생성
 * 25. 6. 13.		durururuk		앨범 생성, 앨범에 이미지 추가 API 문서화 코드 작성
 */
@TestConfiguration
public class RestDocsConfiguration {
	@Bean
	public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer() {
		return configurer -> configurer
			.operationPreprocessors()
			.withRequestDefaults(prettyPrint())
			.withResponseDefaults(prettyPrint());
	}
}
