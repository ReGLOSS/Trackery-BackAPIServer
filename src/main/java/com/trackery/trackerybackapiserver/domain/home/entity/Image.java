package com.trackery.trackerybackapiserver.domain.home.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.home.entity
 * fileName       : Image
 * author         : inari
 * date           : 25. 2. 14.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
	 * 이미지의 공개여부입니다.
	 */
	private Integer isPublic;

	/**
	 * 이미지의 삭제여부입니다.
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
}
