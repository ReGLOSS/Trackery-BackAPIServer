package com.trackery.trackerybackapiserver.domain.jwt.dto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.dto
 * fileName       : AccessTokenDto
 * author         : durururuk
 * date           : 25. 3. 28.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 28.        durururuk      최초 생성
 */
public record AccessTokenDto(String accessToken, String subject) {
}
