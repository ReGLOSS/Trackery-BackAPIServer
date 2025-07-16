package com.trackery.trackerybackapiserver.domain.location.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.entity
 * fileName       : JusoSigungu
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 시/군/구 엔티티입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 * 25. 4. 15.		durururuk		좌표로 지역 찾아오는 기능 구현
 * 25. 4. 23.		durururuk		시/군/구 엔티티 Javadoc 주석 작성
 * 25. 4. 24.		durururuk		클래스 설명 주석 작성
 * 25. 7. 11.		Nari-Lee		location 도메인의 자바독 누락 및 체크스타일 해결
 */
@Getter
@Setter
@NoArgsConstructor
public class JusoSigungu {
	/**
	 * 시/군/구 5자리 식별번호
	 * 앞 두 자리는 시/도의 식별번호 (예: 11200L의 경우 앞 두자리 11은 서울특별시)
	 */
	private Long sigunguId;

	/**
	 * 시/군/구 명 (예: 동작구)
	 */
	private String sigunguName;

	/**
	 * 시/도 엔티티 정보
	 */
	private JusoSido sido;
}
