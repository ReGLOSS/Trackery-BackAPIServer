package com.trackery.trackerybackapiserver.domain.common.dto;

import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.dto
 * fileName       : VerifyEmailDto
 * author         : durururuk
 * date           : 25. 3. 4.
 * description    : 이메일 인증을 위한 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 4.		durururuk		최초 생성
 * 25. 3. 4.		durururuk		이메일 요청 검증 기능 추가
 * 25. 3. 5.		durururuk		이메일 관련 기능 user 도메인에서 분리
 * 25. 3. 14.		durururuk		비밀번호 찾기 기능 리팩터링
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerifyEmailDto {
	@Email
	private String email;
	private String authNumber;
}
