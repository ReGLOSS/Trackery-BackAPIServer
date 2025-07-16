package com.trackery.trackerybackapiserver.domain.user.dto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : UserNameAvailabilityResponseDto
 * author         : durururuk
 * date           : 25. 3. 14.
 * description    : 유저명 유효체크 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 14.		durururuk		최초 생성
 */
public record UserNameAvailabilityResponseDto(boolean available, String token) {
}
