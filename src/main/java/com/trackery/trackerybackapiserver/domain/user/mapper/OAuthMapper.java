package com.trackery.trackerybackapiserver.domain.user.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.trackery.trackerybackapiserver.domain.user.entity.OAuth;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.mapper
 * fileName       : OAuthMapper
 * author         : inari
 * date           : 25. 2. 24.
 * description    : 간편 로그인 사용자 데이터 처리를 위한 MyBatis Mapper 인터페이스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 24.		inari		최초 생성
 * 25. 2. 24.		inari		매퍼생성
 * 25. 2. 28.		inari		구현중
 * 25. 3. 26.		inari		연동유무를 세션에서 토큰으로 이전
 * 25. 4. 9.		durururuk		유저 정보를 찾을 때 role 정보도 같이 조회하게끔 쿼리 수정
 * 25. 4. 9.		durururuk		유저 상세정보 조회 API 단위테스트 코드 작성
 * 25. 6. 26.		inari		탈퇴시 간편로그인 연동 삭제 매퍼 추가
 */
@Mapper
public interface OAuthMapper {

	/**
	 * 새로운 간편 로그인 정보를 데이터 베이스에 등록합니다.
	 *
	 * @param oAuth 저장할 간편 로그인 정보
	 */
	void insertOAuth(OAuth oAuth);

	/**
	 * 간편 로그인 제공자와 제공 ID로 조회하여 기존 등록자인지 확인하는 쿼리
	 *
	 * @param provider 간편 로그인을 등록한 SNS 이름
	 * @param providerUserId  간편 로그인을 등록한 SNS에서 제공한 고유 식별자
	 * @return 조회된 간편 로그인 정보
	 */
	Optional<OAuth> findByProviderAndProviderId(String provider, String providerUserId);

	/**
	 * 특정 유저 아이디와 간편 로그인 제공자로 간편 로그인 정보를 조회하는 쿼리
	 *
	 * @param userId 사용자의 고유 식별자
	 * @param provider 간편 로그인을 등록한 SNS 이름
	 * @return 특정 유저가 연동한 간편 로그인 정보
	 */
	Optional<OAuth> findByUserIdAndProvider(Long userId, String provider);

	/**
	 * 유저의 간편로그인 연동 정보를 조회
	 * @param userId : 유저 ID
	 * @return : OAuth 엔티티 리스트
	 */
	List<OAuth> findByUserId(Long userId);

	/**
	 * 사용자의 모든 OAuth 연동 정보를 삭제합니다.
	 * @param userId 사용자 ID
	 */
	void deleteByUserId(Long userId);
}
