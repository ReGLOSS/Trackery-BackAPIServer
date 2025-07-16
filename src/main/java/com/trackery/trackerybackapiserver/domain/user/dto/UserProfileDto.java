package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : UserProfileDto
 * author         : inari
 * date           : 25. 4. 15.
 * description    : 사용자 프로필 정보를 담는 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		inari		최초 생성
 * 25. 4. 28.		inari		완성
 * 25. 4. 29.		durururuk		유저 프로필 조회 기능에서 프로필사진 객체명이 아닌 s3 presigned url을 요청해서 반환합니다.
 */
@Getter
@AllArgsConstructor
public class UserProfileDto {

	/**
	 * 사용자의 고유 식별자
	 */
	private Long userId;

	/**
	 * 사용자의 아이디
	 */
	private String userName;

	/**
	 * 사용자의 닉네임
	 */
	private String nickname;

	/**
	 * 사용자의 프로필 사진
	 */
	private String userProfilePic;
}
