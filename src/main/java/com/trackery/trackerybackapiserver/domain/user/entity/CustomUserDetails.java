package com.trackery.trackerybackapiserver.domain.user.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.entity
 * fileName       : CustomUserDetails
 * author         : durururuk
 * date           : 25. 2. 19.
 * description    : 인증된 사용자 정보를 담는 UserDetails 구현체 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 19.		durururuk		최초 생성
 * 25. 2. 19.		durururuk		UserDetails, UserDetailsService 구현체 작성,
 * 25. 2. 19.		durururuk		Jwt 생성 시 역할 정보를 담도록 추가, jwt 필터에도 반영, 로직 개선
 * 25. 2. 24.		Nari-Lee		자바독 주석 추가
 * 25. 3. 26.		durururuk		api/auth/me 테스트 코드 작성
 * 25. 3. 26.		durururuk		api/auth/me 테스트 코드 작성
 * 25. 3. 27.		durururuk		CustomUserDetails에서 userName도 함께 담도록 수정
 * 25. 7. 11.		Nari-Lee		user 도메인의 자바독 누락 및 체크스타일 해결
 */
@Getter
public class CustomUserDetails implements UserDetails {

	private final Long userId;
	private final String userName;
	private final Long roleId;

	/**
	 * 사용자 정보를 담는 CustomUserDetails 객체를 생성합니다.
	 *
	 * @param userId 사용자 ID
	 * @param userName 사용자 이름
	 * @param roleId 사용자 역할 ID
	 */
	@Builder
	public CustomUserDetails(Long userId, String userName, Long roleId) {
		this.userId = userId;
		this.userName = userName;
		this.roleId = roleId;
	}

	/**
	 * 사용자의 권한 목록을 반환합니다.
	 *
	 * @return 사용자 권한 목록
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(roleId.toString()));
	}

	/**
	 * OAuth2 기반 인증을 사용하므로 빈 문자열을 반환합니다.
	 *
	 * @return 빈 문자열
	 */
	@Override
	public String getPassword() {
		return "";
	}

	/**
	 * 사용자 이름을 반환합니다.
	 *
	 * @return 사용자 이름
	 */
	@Override
	public String getUsername() {
		return userName;
	}

}
