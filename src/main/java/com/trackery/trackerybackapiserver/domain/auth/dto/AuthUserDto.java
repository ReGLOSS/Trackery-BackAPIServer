package com.trackery.trackerybackapiserver.domain.auth.dto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.auth.dto
 * fileName       : AuthUserDto
 * author         : durururuk
 * date           : 25. 3. 27.
 * description    : 인증 정보 dto
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 27.		durururuk		최초 생성
 */
public record AuthUserDto(Long userId, String userName, Long userRoleId) {
}
