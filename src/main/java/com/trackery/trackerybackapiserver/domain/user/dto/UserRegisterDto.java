package com.trackery.trackerybackapiserver.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegisterDto {

	@Email(message = "이메일 형식이 아닙니다.") @NotBlank(message = "이메일은 공백일 수 없습니다.")
	private String email;
	@NotBlank(message = "사용자명은 공백일 수 없습니다.")
	@Pattern(regexp = "^\\w{4,15}$", message = "유저명은 4~15자 길이에 영문 대소문자,숫자,밑줄(_)만 허용됩니다.")
	private String userName;
	@NotBlank(message = "닉네임은 공백일 수 없습니다.")
	private String nickname;
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{16,}$",
		message = "비밀번호는 최소 16자리이며, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
	)
	private String password;
	private String userProfile;
}
