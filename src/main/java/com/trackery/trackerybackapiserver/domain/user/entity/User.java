package com.trackery.trackerybackapiserver.domain.user.entity;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.entity
 * fileName       : User
 * author         : durururuk
 * date           : 25. 2. 12.
 * description    : 유저 기본 정보를 담는 엔티티 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.		durururuk		최초 생성
 * 25. 2. 12.		durururuk		기본 회원가입 기능 구현
 * 25. 2. 12.		durururuk		네이버 체크 스타일에 맞게 서식 수정
 * 25. 2. 14.		durururuk		User 생성 방식 빌더 패턴으로 변경
 * 25. 2. 14.		durururuk		회원가입 할 때 비밀번호를 해싱해서 저장하게 수정
 * 25. 2. 14.		durururuk		서식 수정
 * 25. 2. 17.		durururuk		userName -> username 오타 수정
 * 25. 2. 17.		durururuk		Java 컨벤션에 맞게 username -> userName 수정
 * 25. 2. 18.		inari		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 19.		durururuk		Role, UserRole 엔티티 추가, User 클래스 생성 정보 추가
 * 25. 2. 24.		inari		자바독 주석 추가
 * 25. 3. 28.		durururuk		리프레시 토큰을 통한 액세스 토큰 재발급 기능 구현
 * 25. 3. 31.		Durururuk		유저 엔티티 빌더 패턴 S107 경고 소나큐브 제외
 * 25. 3. 31.		Durururuk		User Entity에 잘못 설정돼있던 타입 timeStamp를 dateTime으로 수정
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	/**
	 * 사용자의 고유 식별자
	 */
	@SuppressWarnings("unused")
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
	private LocalDateTime startDate;

	/**
	 * 회원 상태 (탈퇴 여부등)
	 */
	private Integer status;

	/**
	 * 마지막 로그인 일자
	 */
	private LocalDateTime lastLogin;

	/**
	 * 사용자의 프로필 사진
	 */
	private String userProfile;

	private Long roleId;

	@Builder
	@SuppressWarnings("java:S107")
	public User(String email, String userName, String nickname, String password, String salt, LocalDateTime startDate,
		Integer status,
		LocalDateTime lastLogin, String userProfile, Long roleId) {
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
