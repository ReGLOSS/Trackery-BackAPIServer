package com.trackery.trackerybackapiserver.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : CacheConfig
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 캐시 설정을 담당하는 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.		inari		최초 생성
 * 25. 2. 14.		inari		랜덤이미지 가져오기 구현
 * 25. 2. 24.		inari		자바독 주석 추가
 */
@Configuration
@EnableCaching
public class CacheConfig {

	/**
	 * 캐시 매니저를 설정합니다.
	 * 캐시에 null 값을 저장하는 것을 금지합니다.
	 *
	 * @return CacheManager 인스턴스
	 */
	@Bean
	public CacheManager cacheManager() {
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
		cacheManager.setAllowNullValues(false);
		return cacheManager;
	}
}
