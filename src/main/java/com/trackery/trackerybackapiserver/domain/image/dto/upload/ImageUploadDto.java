package com.trackery.trackerybackapiserver.domain.image.dto.upload;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto.upload
 * fileName       : ImageUploadDto
 * author         : durururuk
 * date           : 25. 4. 17.
 * description    : 이미지를 업로드하기 위해 필요한 메타데이터를 담당하는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 17.		durururuk		최초 생성
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 4. 21.		durururuk		이미지 업로드 DTO 작성
 * 25. 4. 24.		durururuk		클래스 설명 주석 작성
 * 25. 7. 8.		inari		이미지 업로드시 일반 태그 추가
 * 25. 7. 9.		inari		regionalTags를 Tags로 수정 및 관련 메서드 및 변수 변경
 */
@Getter
@NoArgsConstructor
public class ImageUploadDto {
	private String imageName;
	private String imageType;
	private String description;
	private List<String> tags;
	private double longitude;
	private double latitude;
	private String dateString;
	private boolean isPublic;
}
