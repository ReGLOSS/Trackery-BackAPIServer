package com.trackery.trackerybackapiserver.domain.image.entity;

import java.time.LocalDateTime;

import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.entity
 * fileName       : Image
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 이미지의 기본 정보를 나타내는 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.		inari		최초 생성
 * 25. 2. 14.		inari		매퍼생성
 * 25. 2. 14.		inari		랜덤이미지 가져오기 구현
 * 25. 2. 19.		inari		이미지가 비었을시 에러코드로 변경
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 4. 21.		durururuk		이미지 업로드 DTO 작성
 * 25. 4. 21.		durururuk		AWS Config 작성
 * 25. 8. 8.		durururuk		더 이상 사용되지 않는 imageFile, imageName 필드 제거
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
	private CoordinatePoint coordPoint;

	/**
	 * 이미지의 이름입니다.
	 */
	private String imageName;

	/**
	 * 이미지의 공개여부입니다. (1: 공개, 0: 비공개)
	 */
	private Integer isPublic;

	/**
	 * 이미지의 삭제여부입니다. (true: 공개, false: 비공개)
	 */
	private Boolean isDeleted;

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

	/**
	 * 이미지를 업로드한 유저ID입니다.
	 */
	private Long userId;

	@Builder
	@SuppressWarnings("java:S107")
	public Image(CoordinatePoint coordPoint, String imageName, Integer isPublic, Boolean isDeleted, String imageContent,
		LocalDateTime imageDate, LocalDateTime imageRegDate, Long userId) {
		this.coordPoint = coordPoint;
		this.imageName = imageName;
		this.isPublic = isPublic;
		this.isDeleted = isDeleted;
		this.imageContent = imageContent;
		this.imageDate = imageDate;
		this.imageRegDate = imageRegDate;
		this.userId = userId;
	}
}
