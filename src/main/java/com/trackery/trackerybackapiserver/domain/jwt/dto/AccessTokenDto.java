package com.trackery.trackerybackapiserver.domain.jwt.dto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.dto
 * fileName       : AccessTokenDto
 * author         : durururuk
 * date           : 25. 3. 28.
 * description    : 액세스 토큰의 정보를 가지고 있는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 28.        durururuk      최초 생성
 */
public record AccessTokenDto(String accessToken, String subject) {
}
