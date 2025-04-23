package com.trackery.trackerybackapiserver.domain.aws.service;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.aws
 * fileName       : S3Service
 * author         : durururuk
 * date           : 25. 4. 21.
 * description    : S3 관련 서비스를 담고 있는 인터페이스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 21.		durururuk		최초 생성
 */
public interface S3Service {
	String generatePreSignedGetUrl(String objectKey);

	String generatePreSignedPutUrl(String objectKey);
}
