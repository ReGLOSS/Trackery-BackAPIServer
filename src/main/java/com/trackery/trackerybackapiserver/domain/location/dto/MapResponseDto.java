package com.trackery.trackerybackapiserver.domain.location.dto;

import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.dto
 * fileName       : MapResponseDto
 * author         : inari
 * date           : 25. 5. 30.
 * description    : 지도 API의 통합 응답 DTO입니다.
 *                  시도명, 시군구명과 각각의 ID를 포함하여 프론트엔드에서 쉽게 사용할 수 있도록 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 30.        inari       최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapResponseDto {

	/**
	 * 시도명 (예: "경기도", "서울특별시")
	 */
	private String sidoName;

	/**
	 * 시군구명 (예: "수원시", "화성시")
	 */
	private String sigunguName;

	/**
	 * 시도 ID (데이터베이스 Primary Key)
	 */
	private Long sidoId;

	/**
	 * 시군구 ID (데이터베이스 Primary Key)
	 */
	private Long sigunguId;

	/**
	 * JusoSigungu 엔티티로부터 DTO를 생성하는 정적 팩토리 메서드입니다.
	 *
	 * @param sigungu 시군구 엔티티 (시도 정보 포함)
	 * @return MapResponseDto 인스턴스
	 * @apiNote JusoSigungu 엔티티는 연관된 JusoSido 정보를 포함해야 합니다.
	 */
	public static MapResponseDto from(JusoSigungu sigungu) {
		return MapResponseDto.builder()
			.sidoName(sigungu.getSido().getSidoName())
			.sigunguName(sigungu.getSigunguName())
			.sidoId(sigungu.getSido().getSidoId())
			.sigunguId(sigungu.getSigunguId())
			.build();
	}
}