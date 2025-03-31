package com.trackery.trackerybackapiserver.domain.user.entity;

import java.sql.Timestamp;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.entity
 * fileName       : User
 * author         : durururuk
 * date           : 25. 2. 19.
 * description    : 유저 기본 정보를 담는 엔티티 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 19.        durururuk       최초 생성
 * 25. 2. 24.        inari           상세 주석 추가
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	/**
	 * 사용자의 고유 식별자
	 */
	private Long userId;

	/**
	 * 사용자의 이메일 주소
	 */
	private String email;

	/**
	 * 사용자의 아이디
	 */
	private String userName;

	/**
	 * 사용자의 닉네임
	 */
	private String nickname;

	/**
	 * 사용자의 비밀번호
	 */
	private String password;

	/**
	 * 비밀번호 암호화에 사용된 솔트값
	 */
	private String salt;

	/**
	 * 계정 가입일자
	 */
	private Timestamp startDate;

	/**
	 * 회원 상태 (탈퇴 여부등)
	 */
	private Integer status;

	/**
	 * 마지막 로그인 일자
	 */
	private Timestamp lastLogin;

	/**
	 * 사용자의 프로필 사진
	 */
	private String userProfile;

	private Long roleId;

	@Builder
	@SuppressWarnings("java:S107")
	public User(String email, String userName, String nickname, String password, String salt, Timestamp startDate,
		Integer status,
		Timestamp lastLogin, String userProfile, Long roleId) {
		this.email = email;
		this.userName = userName;
		this.nickname = nickname;
		this.password = password;
		this.salt = salt;
		this.startDate = startDate;
		this.status = status;
		this.lastLogin = lastLogin;
		this.userProfile = userProfile;
		this.roleId = roleId;
	}
}
