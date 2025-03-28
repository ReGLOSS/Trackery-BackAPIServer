package com.trackery.trackerybackapiserver.domain.jwt.dto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.dto
 * fileName       : JwtUserInfo
 * author         : durururuk
 * date           : 25. 3. 28.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 28.        durururuk      최초 생성
 */
public record JwtUserInfoDto(Long userId, String username, Long roleId) {
}
