package com.trackery.trackerybackapiserver.domain.image.event;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.event
 * fileName       : ImageDeleteEvent
 * author         : durururuk
 * date           : 25. 7. 14.
 * description    : 이미지 삭제 시 발행될 이벤트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 14.		durururuk		최초 생성
 */
public record ImageDeleteEvent(Long imageId) {
}
