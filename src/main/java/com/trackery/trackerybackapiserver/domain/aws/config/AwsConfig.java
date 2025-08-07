package com.trackery.trackerybackapiserver.domain.aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.aws.config
 * fileName       : AwsConfig
 * author         : durururuk
 * date           : 25. 4. 21.
 * description    : AWS 관련 설정을 담당하는 Config 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 21.		durururuk		최초 생성
 * 25. 4. 21.		durururuk		AWS Config 작성
 * 25. 4. 21.		durururuk		이미지 업로드 기능 구현
 */
@Slf4j
@Configuration
public class AwsConfig {
	@Value("${aws.region}")
	private String region;

	@Value("${aws.accessKey}")
	private String accessKey;

	@Value("${aws.secretKey}")
	private String secretKey;

	@Bean
	public S3Client s3Client() {
		AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

		return S3Client.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}

	@Bean
	public S3Presigner s3Presigner() {
		AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
		return S3Presigner.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}

	@Bean
	public SqsClient sqsClient() {
		AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
		return SqsClient.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}
}
