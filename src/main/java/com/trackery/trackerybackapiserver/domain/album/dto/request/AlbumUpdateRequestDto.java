package com.trackery.trackerybackapiserver.domain.album.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto.request
 * fileName       : AlbumUpdateRequestDto
 * author         : durururuk
 * date           : 25. 5. 22.
 * description    : 앨범 정보 업데이트에 사용될 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 22.		durururuk		최초 생성
 * 25. 5. 22.		durururuk		앨범 정보 수정 기능 구현
 * 25. 5. 29.		durururuk		JavaDoc 주석 작성
 * 25. 6. 13.		durururuk		requestDTO에서 Long 타입 albumId에 @NotBlank가 돼있던 것 수정
 * 25. 6. 16.		durururuk		파라미터가 아닌 url에 앨범 ID를 포함해서 요청할 수 있도록 수정
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumUpdateRequestDto {
	/*
	albumTitle 앨범 제목
	albumDescription 앨범 설명
	isPublic 공개 여부
	 */
	private String albumTitle;
	private String albumDescription;
	private Integer isPublic;
}
