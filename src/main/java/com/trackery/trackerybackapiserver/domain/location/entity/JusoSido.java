package com.trackery.trackerybackapiserver.domain.location.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.entity
 * fileName       : JusoSido
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 시/도 ID, 이름을 담는 엔티티입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 * 25. 7. 11.		inari			자바독 수정
 */
@Getter
@Setter
@NoArgsConstructor
public class JusoSido {
	/**
	 * 시/도 식별번호 (예: 11L)
	 */
	private Long sidoId;

	/**
	 * 시/도 명 (예: 서울특별시)
	 */
	private String sidoName;
}
