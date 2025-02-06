package com.trackery.trackerybackapiserver.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : CorsProperties
 * author         : narilee
 * date           : 25. 02. 06.
 * description    : CORS 설정을 위한 프로퍼티 클래스입니다.
 * 					application.properties의 cors 설정을 매핑합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 02. 06.        narilee       최초 생성
 */
@Component
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsProperties {

	/**
	 * CORS가 허용된 출처 목록
	 */
	private List<String> allowedOrigins;
}
