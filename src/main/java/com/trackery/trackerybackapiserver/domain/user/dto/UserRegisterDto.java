package com.trackery.trackerybackapiserver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : UserRegisterDto
 * author         : durururuk
 * description    : 사용자 회원가입 정보를 담는 DTO 클래스입니다.
 * 					이 클래스는 회원가입시 클라이언트로부터 받는 사용자 정보를 검증하고 전달하는 역활을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.		durururuk		최초 생성
 * 25. 2. 12.		durururuk		기본 회원가입 기능 구현
 * 25. 2. 12.		durururuk		UserDto validation 추가
 * 25. 2. 14.		durururuk		User 생성 방식 빌더 패턴으로 변경
 * 25. 2. 14.		durururuk		유저명 제약조건 추가
 * 25. 2. 14.		durururuk		비밀번호에 공백 허용하지 않게 수정
 * 25. 2. 14.		durururuk		userName -> username 오타 수정
 * 25. 2. 17.		durururuk		Java 컨벤션에 맞게 username -> userName 수정
 * 25. 2. 18.		inari			dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 24.		inari			자바독 주석 추가
 * 25. 3. 14.		durururuk		수정된 로직에 맞게 테스트 코드 수정
 * 25. 3. 20.		durururuk		사용하지 않는 import 제거
 * 25. 8. 7.		inari			닉네임 패턴 추가
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegisterDto {
	/**
	 * 사용자의 닉네입입니다.
	 * 필수 입력값 입니다.
	 */
	@NotBlank(message = "닉네임은 공백일 수 없습니다.")
	@Pattern(regexp = "^.{1,20}$", message = "닉네임은 최대 20자까지 입력 가능합니다.")
	private String nickname;

	/**
	 * 사용자의 비밀번호입니다.
	 * 비밀번호는 최소 16자리이며, 대문자, 소문자, 숫자, 밑줄(_)을 제외한 특수문자를 1개이상 포함해야 합니다.
	 */
	@NotBlank
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s])^\\S{16,}$",
		message = "비밀번호는 최소 16자리이며, 대문자, 소문자, 숫자, 밑줄(_)을 제외한 특수문자를 포함해야 합니다."
	)
	private String password;

	/**
	 * 사용자의 프로필 이미지입니다.
	 * 필수 사항이 아닙니다.
	 */
	private String userProfile;
}
