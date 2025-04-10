package com.trackery.trackerybackapiserver.domain.user.dto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : UpdatePasswordDto
 * author         : durururuk
 * date           : 25. 4. 10.
 * description    : 비밀번호 업데이트 쿼리를 사용하기 위한 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 10.		durururuk		최초 생성
 */
public record UpdatePasswordDto(Long userId, String password, String salt) {
}
