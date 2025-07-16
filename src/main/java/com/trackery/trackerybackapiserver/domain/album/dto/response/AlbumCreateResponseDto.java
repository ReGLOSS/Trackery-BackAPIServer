package com.trackery.trackerybackapiserver.domain.album.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto.response
 * fileName       : AlbumCreateResponseDto
 * author         : durururuk
 * date           : 25. 5. 20.
 * description    : 앨범 생성 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 20.		durururuk		최초 생성
 * 25. 5. 21.		durururuk		JavaDoc 작성
 * 25. 5. 21.		durururuk		앨범 생성 기능 응답 구체적으로 수정, JavaDoc 주석 작성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumCreateResponseDto {
	/**
	 * albumId 앨범 ID
	 * albumTitle 앨범 제목
	 * albumDescription 앨범 설명
	 */
	private Long albumId;
	private String albumTitle;
	private String albumDescription;
}
