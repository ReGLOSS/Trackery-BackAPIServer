package com.trackery.trackerybackapiserver.domain.album.service;

import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.enums.AlbumImageEditOperation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.service
 * fileName       : AlbumImageEditEvent
 * author         : durururuk
 * date           : 25. 6. 20.
 * description    : 앨범 이미지 수정이 발생했을 때 작동할 이벤트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 20.		durururuk		최초 생성
 */
@Getter
@AllArgsConstructor
public class AlbumImageEditEvent {
	private final AlbumImageEditResponseDto albumImageEditResponseDto;
	private final AlbumImageEditOperation albumImageEditOperation;
}
