package com.trackery.trackerybackapiserver.domain.user.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.mapper
 * fileName       : UserRoleMapper
 * author         : durururuk
 * date           : 25. 2. 25.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 25.        durururuk      최초 생성
 */
@Mapper
public interface UserRoleMapper {
	/**
	 * 새로운 사용자-권한 관계를 데이터베이스에 저장합니다.
	 *
	 * @param userRole 저장할 사용자-권한 관계 정보
	 */
	void insertUserRole(UserRole userRole);

	Optional<UserRole> findByUserId(Long userId);
}
