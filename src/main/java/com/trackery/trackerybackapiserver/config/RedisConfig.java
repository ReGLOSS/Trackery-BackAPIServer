package com.trackery.trackerybackapiserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : RedisConfig
 * author         : durururuk
 * date           : 25. 3. 3.
 * description    : Redis에 환경설정 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 3.        durururuk      최초 생성
 * 25. 3. 3.        durururuk      RedisConnectionFactory, RedisTemplate Bean 작성
 */
@Configuration
public class RedisConfig {

	/**
	 * RedisTemplate를 생성해서 Redis와 통신할 수 있도록 설정
	 *
	 * @param redisConnectionFactory : Redis에 연결하기 위한 connectionFactory
	 * @return : RedisTemplate
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

		return redisTemplate;
	}
}
