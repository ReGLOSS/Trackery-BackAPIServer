package com.trackery.trackerybackapiserver.domain.user.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.mapper
 * fileName       : UserMapper
 * author         : dururuk
 * date           : 25. 2. 12.
 * description    : 사용자 데이터 처리를 위한 MyBatis Mapper 인터페이스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.        dururuk       최초 생성
 * 25. 2. 24.        inari         주석 추가
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
	 * @param userId 조회할 사용자 고유 식별자
	 * @return 조회된 사용자 정보
	 */
	Optional<User> findByUserId(Long userId);

	/**
	 * 새로운 사용자-권한 관계를 데이터베이스에 저장합니다.
	 *
	 * @param userRole 저장할 사용자-권한 관계 정보
	 */
	void insertUserRole(UserRole userRole);
}
