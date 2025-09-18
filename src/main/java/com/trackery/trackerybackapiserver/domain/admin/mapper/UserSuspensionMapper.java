package com.trackery.trackerybackapiserver.domain.admin.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.trackery.trackerybackapiserver.domain.admin.entity.UserSuspension;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.mapper
 * fileName       : UserSuspensionMapper
 * author         : inari
 * date           : 25. 9. 18.
 * description    : 사용자 정지 이력을 관리하는 매퍼 인터페이스 (로그 기능 포함)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 18.		inari		최초 생성
 */
@Mapper
public interface UserSuspensionMapper {

	/**
	 * 정지 이력을 데이터베이스에 삽입
	 * @param suspension 정지 이력 정보
	 */
	void insertSuspension(UserSuspension suspension);

	/**
	 * 만료된 임시정지 목록을 조회 (배치 처리용)
	 * @param currentDate 현재 날짜
	 * @return 만료된 임시정지 목록
	 */
	List<UserSuspension> findExpiredTemporarySuspensions(@Param("currentDate") LocalDate currentDate);

	/**
	 * 사용자의 현재 활성 정지 정보를 조회
	 * @param userId 사용자 ID
	 * @return 활성 정지 정보 (없으면 null)
	 */
	UserSuspension findActiveSuspensionByUserId(@Param("userId") Long userId);

	/**
	 * 정지 이력을 비활성화 (is_active = 0)
	 * @param suspensionId 정지 이력 ID
	 */
	void deactivateSuspension(@Param("suspensionId") Long suspensionId);

	/**
	 * 사용자별 정지 이력을 조회 (최신순)
	 * @param userId 사용자 ID
	 * @return 정지 이력 목록
	 */
	List<UserSuspension> findSuspensionHistoryByUserId(@Param("userId") Long userId);

	/**
	 * 관리자별 작업 로그를 조회 (최신순)
	 * @param adminId 관리자 ID (null이면 전체 조회)
	 * @return 관리자 작업 로그 목록
	 */
	List<UserSuspension> findActionLogsByAdminId(@Param("adminId") Long adminId);

	/**
	 * 사용자ID로 모든 활성 정지를 비활성화 (정지 해제 시 사용)
	 * @param userId 사용자 ID
	 */
	void deactivateAllSuspensionsByUserId(@Param("userId") Long userId);

	/**
	 * 정지 이력 통계 조회 (대시보드용)
	 * @return 총 정지 횟수
	 */
	Long countTotalSuspensions();

	/**
	 * 현재 정지 중인 사용자 수 조회
	 * @return 정지 중인 사용자 수
	 */
	Long countCurrentSuspendedUsers();
}
