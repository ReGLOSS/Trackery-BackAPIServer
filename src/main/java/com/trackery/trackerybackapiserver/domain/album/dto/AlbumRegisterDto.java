package com.trackery.trackerybackapiserver.domain.album.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto
 * fileName       : AlbumRegisterDto
 * author         : durururuk
 * date           : 25. 5. 14.
 * description    : 앨범 생성에 필요한 정보를 담는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.		durururuk		최초 생성
 */
@Getter
@NoArgsConstructor
public class AlbumRegisterDto {
	/**
	 * albumTitle : 앨범 제목
	 * albumDescription : 앨범 설명
	 * thumbnailImageId : 썸네일 이미지 ID
	 * imageIdList : 앨범에 포함될 이미지 리스트
	 * isPublic : 공개 여부
	 */
	private String albumTitle;
	private String albumDescription;
	private Long thumbnailImageId;
	private List<Long> imageIdList;
	private boolean isPublic;
}
