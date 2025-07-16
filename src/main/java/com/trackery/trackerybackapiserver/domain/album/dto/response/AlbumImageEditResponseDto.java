package com.trackery.trackerybackapiserver.domain.album.dto.response;

import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto.response
 * fileName       : AlbumImageEditResponseDto
 * author         : durururuk
 * date           : 25. 5. 21.
 * description    : 앨범 이미지 추가 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.		durururuk		최초 생성
 * 25. 5. 21.		durururuk		JavaDoc 작성
 * 25. 5. 21.		durururuk		체크스타일 경고 수정
 * 25. 6. 11.		durururuk		앨범 이미지 삭제 기능 구현
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumImageEditResponseDto {
	/**
	 * albumId 앨범 ID
	 * succeededImageCount 추가에 성공한 이미지 개수
	 * failedImageCount 추가에 실패한 이미지 개수
	 * succededImageIds 추가에 성공한 이미지 ID
	 * failedImageIds 추가에 실패한 이미지 ID, 이유
	 */
	private Long albumId;
	private int succeededImageCount;
	private int failedImageCount;
	private Set<Long> succeededImageIds;
	private Map<Long, String> failedImageIds;
}
