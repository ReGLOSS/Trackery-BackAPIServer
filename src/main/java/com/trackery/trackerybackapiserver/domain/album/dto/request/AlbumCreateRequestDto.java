package com.trackery.trackerybackapiserver.domain.album.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto.request
 * fileName       : AlbumCreateRequestDto
 * author         : durururuk
 * date           : 25. 5. 14.
 * description    : 앨범 생성 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.		durururuk		최초 생성
 * 25. 5. 14.		durururuk		앨범 기능 기본 entity, mapper 생성
 * 25. 5. 15.		durururuk		이미지 조회 기능 구현
 * 25. 5. 20.		durururuk		앨범 생성 기능 작성
 * 25. 5. 21.		durururuk		JavaDoc 작성
 * 25. 5. 21.		durururuk		체크스타일 경고 수정
 * 25. 5. 22.		durururuk		앨범 정보 수정 기능 구현
 */
@Getter
@NoArgsConstructor
public class AlbumCreateRequestDto {
	/**
	 * albumTitle : 앨범 제목
	 * albumDescription : 앨범 설명
	 * isPublic : 공개 여부
	 */
	@NotBlank
	private String albumTitle;
	private String albumDescription;
	private Integer isPublic;
}