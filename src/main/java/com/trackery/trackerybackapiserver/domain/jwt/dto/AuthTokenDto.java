package com.trackery.trackerybackapiserver.domain.jwt.dto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.dto
 * fileName       : AuthTokenDto
 * author         : durururuk
 * date           : 25. 3. 29.
 * description    : 인증에 필요한 토큰을 담는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 29.		durururuk		최초 생성
 */
public record AuthTokenDto(String accessToken, String refreshToken) {
}
