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
 * 25. 7. 8.		inari			태그리스트 추가
 */
@Getter
@NoArgsConstructor
public class ImageUploadDto {
	private String imageName;
	private String imageType;
	private String description;
	private List<String> regionalTags;
	private double longitude;
	private double latitude;
	private String dateString;
	private boolean isPublic;
}
