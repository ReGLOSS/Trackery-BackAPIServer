package com.trackery.trackerybackapiserver.domain.home.entity;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.home.entity
 * fileName       : Image
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 이미지의 기본 정보를 나타내는 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 * 25. 2. 19		 inari		 imageId 빌더에서 제외
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

	/**
	 * 이미지의 고유 식별자입니다.
	 */
	private Long imageId;

	/**
	 * 이미지의 좌표 식별번호입니다.
	 */
	private Long coordPointId;

	/**
	 * 이미지의 이름입니다.
	 */
	private String imageName;

	/**
	 * 이미지의 S3 URL 주소입니다.
	 */
	private String imageFile;

	/**
	 * 이미지의 공개여부입니다. (1: 공개, 0: 비공개)
	 */
	private Integer isPublic;

	/**
	 * 이미지의 삭제여부입니다. (true: 공개, false: 비공개)
	 */
	private Boolean isDeleted;

	/**
	 * 이미지의 확장자입니다.
	 */
	private String imageType;

	/**
	 * 이미지의 설명입니다.
	 */
	private String imageContent;

	/**
	 * 이미지의 촬영된 날짜입니다.
	 */
	private LocalDateTime imageDate;

	/**
	 * 이미지가 등록된 업로드일입니다.
	 */
	private LocalDateTime imageRegDate;

	@Builder
	public Image(Long coordPointId, String imageName, String imageFile, Integer isPublic, Boolean isDeleted,
		String imageType, String imageContent, LocalDateTime imageDate, LocalDateTime imageRegDate) {
		this.coordPointId = coordPointId;
		this.imageName = imageName;
		this.imageFile = imageFile;
		this.isPublic = isPublic;
		this.isDeleted = isDeleted;
		this.imageType = imageType;
		this.imageContent = imageContent;
		this.imageDate = imageDate;
		this.imageRegDate = imageRegDate;
	}
}
