package com.trackery.trackerybackapiserver.domain.admin.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.dto.request
 * fileName       : UserSuspensionRequestDto
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 사용자 정지 요청 정보를 담는 DTO 클래스입니다.
 * 					이 클래스는 관리자가 사용자를 정지시킬 때 필요한 정보를 전달하는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@Setter
@NoArgsConstructor
public class UserSuspensionRequestDto {

	/**
	 * 정지 유형
	 * 2: 임시정지, 3: 영구정지
	 */
	@NotNull(message = "정지 유형은 필수입니다.")
	private Integer suspensionType;

	/**
	 * 정지 해제 예정일 (임시정지인 경우만 필수)
	 * 날짜만 저장 (시간 정보 없음)
	 */
	private LocalDate endDate;

	/**
	 * 정지 사유
	 */
	@Size(max = 500, message = "정지 사유는 500자를 초과할 수 없습니다.")
	private String reason;
}
