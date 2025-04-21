package com.trackery.trackerybackapiserver.domain.image.dto.upload;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto.upload
 * fileName       : ImageUploadDto
 * author         : durururuk
 * date           : 25. 4. 17.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 17.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class ImageUploadDto {
	private String imageName;
	private String imageType;
	private String description;
	private String tags;
	private double longitude;
	private double latitude;
	private String dateString;
	private boolean isPublic;
}
