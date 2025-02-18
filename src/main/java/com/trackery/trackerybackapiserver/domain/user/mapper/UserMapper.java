package com.trackery.trackerybackapiserver.domain.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.trackery.trackerybackapiserver.domain.user.entity.User;

@Mapper
public interface UserMapper {
	void insertUser(User user);

	boolean checkUsernameAvailability(String username);
}
