package com.trackery.trackerybackapiserver.domain.home.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.home.controller
 * fileName       : LandingImagesResponse
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 랜딩 페이지 이미지 URL 응답 DTO 입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 */
@Getter
@AllArgsConstructor
public class LandingImagesResponse {
	private final List<String> imageUrls;
}
