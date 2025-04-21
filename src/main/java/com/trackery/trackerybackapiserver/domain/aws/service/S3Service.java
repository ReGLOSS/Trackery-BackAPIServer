package com.trackery.trackerybackapiserver.domain.aws.service;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.aws
 * fileName       : S3Service
 * author         : durururuk
 * date           : 25. 4. 21.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 21.		durururuk		최초 생성
 */
public interface S3Service {
	public String generatePreSignedGetUrl(String objectKey);

	public String generatePreSignedPutUrl(String objectKey);
}
