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
 */

/**
 * 시/도 엔티티입니다.
 *
 * sidoId 시/도 식별번호 (예시 : 11L )
 * sidoName 시/도 명 (예시 : 서울특별시 )
 */
@Getter
@Setter
@NoArgsConstructor
public class JusoSido {
	private Long sidoId;
	private String sidoName;
}
