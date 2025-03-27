package com.trackery.trackerybackapiserver.domain.auth.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.trackery.trackerybackapiserver.domain.auth.dto.AuthUserDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.auth.service
 * fileName       : AuthServiceTest
 * author         : durururuk
 * date           : 25. 3. 27.
 * description    : AuthService 단위테스트 작성
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 27.        durururuk      최초 생성
 * 25. 3. 27.        durururuk      toAuthUserDto 단위테스트 작성
 */
class AuthServiceTest {
    private final AuthService authService = new AuthService();

    @Test
    void DTO_변환_테스트_성공() {
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "abcdefg", 1L);
        AuthUserDto expectedDto = new AuthUserDto(1L, "abcdefg", 1L);

        AuthUserDto resultDto = authService.toAuthUserDto(customUserDetails);

        Assertions.assertEquals(expectedDto, resultDto);
    }
}
