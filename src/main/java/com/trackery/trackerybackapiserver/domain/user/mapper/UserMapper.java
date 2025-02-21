package com.trackery.trackerybackapiserver.domain.user.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;

@Mapper
public interface UserMapper {
	void insertUser(User user);

	boolean isExistsUsername(String username);

	Optional<User> findByUserId(Long userId);

	void insertUserRole(UserRole userRole);
}
