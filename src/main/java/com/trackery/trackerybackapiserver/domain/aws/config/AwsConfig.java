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
 * 25. 8. 14.		durururuk		sqs bean 등록
 * 25. 8. 14.		durururuk		javadoc 주석 추가
 */
@Slf4j
@Configuration
public class AwsConfig {
	
	/**
	 * AWS 리전 정보
	 */
	@Value("${aws.region}")
	private String region;

	/**
	 * AWS 액세스 키
	 */
	@Value("${aws.accessKey}")
	private String accessKey;

	/**
	 * AWS 시크릿 키
	 */
	@Value("${aws.secretKey}")
	private String secretKey;

	/**
	 * S3 클라이언트 빈을 생성합니다.
	 * AWS 인증 정보와 리전을 설정하여 S3 서비스에 접근할 수 있는 클라이언트를 제공합니다.
	 * 
	 * @return S3Client 인스턴스
	 */
	@Bean
	public S3Client s3Client() {
		AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

		return S3Client.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}

	/**
	 * S3 Presigner 빈을 생성합니다.
	 * S3 객체에 대한 사전 서명된 URL을 생성하는데 사용됩니다.
	 * 클라이언트가 직접 S3에 접근할 수 있도록 임시 URL을 제공합니다.
	 * 
	 * @return S3Presigner 인스턴스
	 */
	@Bean
	public S3Presigner s3Presigner() {
		AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
		return S3Presigner.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}

	/**
	 * SQS 클라이언트 빈을 생성합니다.
	 * AWS Simple Queue Service에 접근하여 메시지 큐 작업을 수행할 수 있는 클라이언트를 제공합니다.
	 * 
	 * @return SqsClient 인스턴스
	 */
	@Bean
	public SqsClient sqsClient() {
		AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
		return SqsClient.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(credentials))
			.build();
	}
}
