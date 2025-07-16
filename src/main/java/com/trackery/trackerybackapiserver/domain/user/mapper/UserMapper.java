package com.trackery.trackerybackapiserver.domain.user.mapper;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.trackery.trackerybackapiserver.domain.user.entity.User;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.mapper
 * fileName       : UserMapper
 * author         : durururuk
 * date           : 25. 2. 12.
 * description    : 사용자 데이터 처리를 위한 MyBatis Mapper 인터페이스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.		durururuk		최초 생성
 * 25. 2. 12.		durururuk		기본 회원가입 기능 구현
 * 25. 2. 14.		durururuk		닉네임 중복 체크하는 api 구현
 * 25. 2. 17.		durururuk		username 중복 체크 SQL count(*) -> EXISTS()로 수정
 * 25. 2. 18.		Nari-Lee		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 19.		durururuk		UserDetails, UserDetailsService 구현체 작성,
 * 25. 2. 19.		durururuk		회원가입 시 UserRole에 기본값 인서트되게 기능 추가
 * 25. 2. 21.		durururuk		사용자명 중복체크 메서드명 더 명확하게 수정, 반대로 작동하던 로직 수정
 * 25. 2. 21.		durururuk		이원화
 * 25. 2. 24.		Nari-Lee		자바독 주석 추가
 * 25. 2. 25.		durururuk		유저명으로 user 엔티티 불러오는 쿼리 추가
 * 25. 2. 28.		Nari-Lee		구현중
 * 25. 3. 11.		durururuk		비밀번호 변경 기능 작성
 * 25. 3. 11.		durururuk		비밀번호 변경 컨트롤러 작성
 * 25. 4. 10.		durururuk		이메일 토큰 기반 비밀번호 변경 url 변경, 인증 기반 비밀번호 변경 기능 구현
 * 25. 4. 10.		durururuk		final이 될 수 있는 변수 final화, 로직 수정으로 사용되지 않는 메서드 삭제
 * 25. 4. 12.		durururuk		닉네임 변경 기능 구현
 * 25. 4. 12.		durururuk		유저명 변경 기능 구현
 * 25. 4. 14.		durururuk		이메일 수정 API 추가, 주석 작성
 * 25. 4. 14.		durururuk		사용되지 않는 변수 삭제
 * 25. 6. 26.		Nari-Lee		회원 탈퇴 매퍼 작성
 * 25. 6. 26.		Nari-Lee		자바독 추가
 * 25. 6. 27.		Nari-Lee		로그인시 마지막 로그인 갱신되도록 수정
 * 25. 7. 11.		Nari-Lee		user 도메인의 자바독 누락 및 체크스타일 해결
 * 25. 7. 11.		Nari-Lee		자바독 오류 해결 및 자바독 주입
 */
@Mapper
public interface UserMapper {
	/**
	 * 새로운 사용자 정보를 데이터 베이스에 저장합니다.
	 *
	 * @param user 저장할 사용자 정보
	 */
	void insertUser(User user);

	/**
	 * 주어진 아이디가 이미 존재하는지 확인합니다.
	 *
	 * @param username 확인할 사용자 아이디
	 * @return 존재하면 true, 없으면 false
	 */
	boolean isExistsUserName(String username);

	/**
	 * 사용자 고유 식별자로 사용자 정보를 조회합니다.
	 *
	 * @param userId 조회할 사용자 고유 식별자
	 * @return 조회된 사용자 정보
	 */
	Optional<User> findByUserId(Long userId);


	/**
	 * 사용자 이메일로 사용자 정보를 조회합니다.
	 *
	 * @param email 조회할 사용자 이메일
	 * @return 조회된 사용자 정보
	 */
	Optional<User> findByEmail(String email);


	/**
	 * 유저명으로 User를 찾아서 반환합니다.
	 * @param userName : 유저명
	 * @return Optional&lt;User&gt;
	 */
	Optional<User> findByUserName(String userName);

	/**
	 * 사용자 ID로 비밀번호와 솔트를 업데이트합니다.
	 *
	 * @param userId 업데이트할 사용자 ID
	 * @param password 새로운 암호화된 비밀번호
	 * @param salt 새로운 솔트 값
	 */
	void updatePasswordByUserId(@Param("userId") Long userId, @Param("password") String password,
		@Param("salt") String salt);

	/**
	 * 사용자 ID로 닉네임을 업데이트합니다.
	 *
	 * @param userId 업데이트할 사용자 ID
	 * @param nickname 새로운 닉네임
	 */
	void updateNicknameByUserId(@Param("userId") Long userId, @Param("nickname") String nickname);

	/**
	 * 사용자 ID로 사용자명을 업데이트합니다.
	 *
	 * @param userId 업데이트할 사용자 ID
	 * @param userName 새로운 사용자명
	 */
	void updateUserNameByUserId(@Param("userId") Long userId, @Param("userName") String userName);

	/**
	 * 사용자 ID로 이메일을 업데이트합니다.
	 *
	 * @param userId 업데이트할 사용자 ID
	 * @param email 새로운 이메일 주소
	 */
	void updateEmailByUserId(@Param("userId") Long userId, @Param("email") String email);

	/**
	 * 주어진 사용자 ID가 존재하는지 확인합니다.
	 *
	 * @param userId 확인할 사용자 ID
	 * @return 존재하면 true, 없으면 false
	 */
	boolean existsByUserId(Long userId);

	/**
	 * 사용자 ID로 마지막 로그인 시간을 업데이트합니다.
	 *
	 * @param userId 업데이트할 사용자 ID
	 * @param lastLogin 마지막 로그인 시간
	 */
	void updateLastLoginByUserId(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

	/**
	 * 사용자를 논리적으로 삭제하고 개인정보를 익명화합니다.
	 *
	 * @param userId 탈퇴할 사용자 ID
	 * @param email 익명화된 이메일
	 * @param userName 익명화된 사용자명
	 * @param nickname 익명화된 닉네임
	 * @param password 랜덤 패스워드
	 * @param salt 랜덤 솔트
	 */
	void deleteUserByUserId(@Param("userId") Long userId, @Param("email") String email,
		@Param("userName") String userName, @Param("nickname") String nickname,
		@Param("password") String password, @Param("salt") String salt);
}
