package com.trackery.trackerybackapiserver.domain.user.dto;

import java.util.List;

import com.trackery.trackerybackapiserver.domain.user.entity.OAuth;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : DetailedUserDto
 * author         : durururuk
 * date           : 25. 4. 9.
 * description    : 유저 상세정보를 담고 있는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 9.		durururuk		최초 생성
 */
public record DetailedUserInfoDto(
	Long userId,
	Long userRoleId,
	String userName,
	String nickname,
	String email,
	List<OAuth> OAuthList) {
}
