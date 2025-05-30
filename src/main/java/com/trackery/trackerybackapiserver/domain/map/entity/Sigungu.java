package com.trackery.trackerybackapiserver.domain.map.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.map.entity
 * fileName       : Sigungu
 * author         : inari
 * date           : 25. 5. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 20.        inari       최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sigungu {
	private Long sigunguId;
	private String sigunguName;
	private Sido sido;
}
