package com.trackery.trackerybackapiserver.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.dto.request
 * fileName       : UserRoleChangeRequestDto
 * author         : inari
 * date           : 25. 9. 24.
 * description    : 사용자 역할 변경 요청 정보를 담는 DTO 클래스입니다.
 * 					이 클래스는 관리자가 사용자의 역할을 변경할 때 필요한 정보를 전달하는 역할을 합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 24.		inari		최초 생성
 */
@Getter
@Setter
@NoArgsConstructor
public class UserRoleChangeRequestDto {

	/**
	 * 변경할 역할 ID
	 * 1: USER, 2: MANAGER, 3: ADMIN
	 * ADMIN만 역할 변경 가능하며, ADMIN 역할로는 변경 불가
	 */
	@NotNull(message = "역할 ID는 필수입니다.")
	private Integer roleId;
}
