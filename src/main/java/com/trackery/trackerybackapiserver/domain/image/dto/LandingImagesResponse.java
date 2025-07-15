package com.trackery.trackerybackapiserver.domain.image.dto;

import java.util.List;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.dto
 * fileName       : LandingImagesResponse
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 랜딩 페이지 이미지 URL 응답 DTO 입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 * 25. 7. 14.        inari       레코드로 변경
 *
 * @param imageUrls 랜딩 페이지에 표시될 이미지 URL 목록
 */
public record LandingImagesResponse(List<String> imageUrls) {
}
