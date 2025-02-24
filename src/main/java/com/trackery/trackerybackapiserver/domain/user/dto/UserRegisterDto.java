package com.trackery.trackerybackapiserver.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : UserRegisterDto
 * author         : dururuk
 * date           : 25. 2. 12.
 * description    : 사용자 회원가입 정보를 담는 DTO 클래스입니다.
 * 					이 클래스는 회원가입시 클라이언트로부터 받는 사용자 정보를 검증하고 전달하는 역활을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.        dururuk       최초 생성
 * 25. 2. 24.        inari         주석 추가
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegisterDto {

	/**
	 * 사용자의 이메일 주소입니다.
	 * 올바른 이메일 형식이여야하며 필수 입력값입니다.
	 */
	@Email(message = "이메일 형식이 아닙니다.") @NotBlank(message = "이메일은 공백일 수 없습니다.")
	private String email;

	/**
	 * 사용자의 아이디입니다.
	 * 4~15자 길이에 영문 대소문자,숫자,밑줄(_)만 허용됩니다.
	 * 필수 입력값입니다.
	 */
	@NotBlank(message = "사용자명은 공백일 수 없습니다.")
	@Pattern(regexp = "^\\w{4,15}$", message = "유저명은 4~15자 길이에 영문 대소문자,숫자,밑줄(_)만 허용됩니다.")
	private String userName;

	/**
	 * 사용자의 닉네입입니다.
	 * 필수 입력값 입니다.
	 */
	@NotBlank(message = "닉네임은 공백일 수 없습니다.")
	private String nickname;

	/**
	 * 사용자의 비밀번호입니다.
	 * 비밀번호는 최소 16자리이며, 대문자, 소문자, 숫자, 밑줄(_)을 제외한 특수문자를 1개이상 포함해야 합니다.
	 */
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
