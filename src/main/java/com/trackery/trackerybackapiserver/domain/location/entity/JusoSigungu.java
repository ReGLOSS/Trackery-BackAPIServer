package com.trackery.trackerybackapiserver.domain.location.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.entity
 * fileName       : JusoSigungu
 * author         : durururuk
 * date           : 25. 4. 15.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		durururuk		최초 생성
 */

/**
 * 시/군/구 엔티티압니다.
 *
 * sigunguId 시/군/구 5자리 식별번호입니다. 앞 두 자리는 시/도의 식별번호입니다.
 * 			 예를 들어 11200L인 경우 앞 두자리 11은 서울특별시의 식별번호입니다.
 * sigunguName 시/군/구 명 (예시 : 동작구)
 * sido 시/도 엔티티 sidoId, sidoName을 가져올 수 있습니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class JusoSigungu {
	private Long sigunguId;
	private String sigunguName;
	private JusoSido sido;
}
