package com.trackery.trackerybackapiserver.domain.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trackery.trackerybackapiserver.domain.admin.enums.UserStatus;
import com.trackery.trackerybackapiserver.domain.admin.mapper.UserSuspensionMapper;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.tag.mapper.TagMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.service
 * fileName       : AdminDashboardServiceTest
 * author         : inari
 * date           : 25. 9. 28.
 * description    : 관리자 대시보드 서비스 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 28.		inari		최초 생성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDashboardService 테스트")
class AdminDashboardServiceTest {

	@Mock
	private UserMapper userMapper;

	@Mock
	private ImageMapper imageMapper;

	@Mock
	private AlbumMapper albumMapper;

	@Mock
	private TagMapper tagMapper;

	@Mock
	private UserSuspensionMapper userSuspensionMapper;

	@InjectMocks
	private AdminDashboardService adminDashboardService;

	@Nested
	@DisplayName("대시보드 통계 조회")
	class GetDashboardStatisticsTest {

		@Test
		@DisplayName("ADMIN이 대시보드 통계를 성공적으로 조회한다")
		void getDashboardStatisticsSuccess() {
			// Given
			Long adminRoleId = 3L;

			// 기본 통계 설정
			when(userMapper.countAllUsers()).thenReturn(1000L);
			when(imageMapper.countAllImages()).thenReturn(5000L);
			when(albumMapper.countAllAlbums()).thenReturn(500L);
			when(tagMapper.countAllTags()).thenReturn(200L);

			// 사용자 상태별 통계 설정
			when(userMapper.countUsersByStatus(UserStatus.ACTIVE.getCode())).thenReturn(800L);
			when(userMapper.countUsersByStatus(UserStatus.TEMPORARILY_SUSPENDED.getCode())).thenReturn(30L);
			when(userMapper.countUsersByStatus(UserStatus.PERMANENTLY_SUSPENDED.getCode())).thenReturn(20L);
			when(userMapper.countUsersByStatus(UserStatus.WITHDRAWN.getCode())).thenReturn(150L);

			// 역할별 통계 설정
			when(userMapper.countUsersByRole(1L)).thenReturn(900L);
			when(userMapper.countUsersByRole(2L)).thenReturn(90L);
			when(userMapper.countUsersByRole(3L)).thenReturn(10L);

			// When
			AdminDashboardService.DashboardStatistics result =
				adminDashboardService.getDashboardStatistics(adminRoleId);

			// Then
			assertNotNull(result);

			// 기본 통계 검증
			assertEquals(1000L, result.totalUsers());
			assertEquals(5000L, result.totalImages());
			assertEquals(500L, result.totalAlbums());
			assertEquals(200L, result.totalTags());

			// 사용자 상태별 통계 검증
			AdminDashboardService.UserStatusStatistics userStatusStats = result.userStatusStats();
			assertNotNull(userStatusStats);
			assertEquals(800L, userStatusStats.activeUsers());
			assertEquals(30L, userStatusStats.temporarilySuspendedUsers());
			assertEquals(20L, userStatusStats.permanentlySuspendedUsers());
			assertEquals(50L, userStatusStats.totalSuspendedUsers()); // 30 + 20
			assertEquals(150L, userStatusStats.withdrawnUsers());

			// 역할별 통계 검증
			AdminDashboardService.RoleStatistics roleStats = result.roleStats();
			assertNotNull(roleStats);
			assertEquals(900L, roleStats.regularUsers());
			assertEquals(90L, roleStats.managers());
			assertEquals(10L, roleStats.admins());

			// 메서드 호출 검증
			verify(userMapper).countAllUsers();
			verify(imageMapper).countAllImages();
			verify(albumMapper).countAllAlbums();
			verify(tagMapper).countAllTags();
			verify(userMapper, times(4)).countUsersByStatus(anyInt());
			verify(userMapper, times(3)).countUsersByRole(anyLong());
		}

		@Test
		@DisplayName("MANAGER가 대시보드 통계를 조회하려 하면 예외가 발생한다")
		void getDashboardStatisticsWithManagerRoleFails() {
			// Given
			Long managerRoleId = 2L;

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminDashboardService.getDashboardStatistics(managerRoleId));

			assertEquals(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES, exception.getErrorCode());

			// 어떤 매퍼도 호출되지 않아야 함
			verify(userMapper, never()).countAllUsers();
			verify(imageMapper, never()).countAllImages();
			verify(albumMapper, never()).countAllAlbums();
			verify(tagMapper, never()).countAllTags();
		}

		@Test
		@DisplayName("일반 사용자가 대시보드 통계를 조회하려 하면 예외가 발생한다")
		void getDashboardStatisticsWithUserRoleFails() {
			// Given
			Long userRoleId = 1L;

			// When & Then
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
				adminDashboardService.getDashboardStatistics(userRoleId));
		}

		@Test
		@DisplayName("통계 수치가 0인 경우도 정상적으로 처리한다")
		void getDashboardStatisticsWithZeroValues() {
			// Given
			Long adminRoleId = 3L;

			// 모든 통계를 0으로 설정
			when(userMapper.countAllUsers()).thenReturn(0L);
			when(imageMapper.countAllImages()).thenReturn(0L);
			when(albumMapper.countAllAlbums()).thenReturn(0L);
			when(tagMapper.countAllTags()).thenReturn(0L);

			when(userMapper.countUsersByStatus(anyInt())).thenReturn(0L);
			when(userMapper.countUsersByRole(anyLong())).thenReturn(0L);

			// When
			AdminDashboardService.DashboardStatistics result =
				adminDashboardService.getDashboardStatistics(adminRoleId);

			// Then
			assertNotNull(result);
			assertEquals(0L, result.totalUsers());
			assertEquals(0L, result.totalImages());
			assertEquals(0L, result.totalAlbums());
			assertEquals(0L, result.totalTags());

			assertEquals(0L, result.userStatusStats().activeUsers());
			assertEquals(0L, result.userStatusStats().totalSuspendedUsers());
			assertEquals(0L, result.userStatusStats().withdrawnUsers());

			assertEquals(0L, result.roleStats().regularUsers());
			assertEquals(0L, result.roleStats().managers());
			assertEquals(0L, result.roleStats().admins());
		}

		@Test
		@DisplayName("정지 사용자 수가 올바르게 계산된다")
		void calculateSuspendedUsersCorrectly() {
			// Given
			Long adminRoleId = 3L;

			when(userMapper.countAllUsers()).thenReturn(100L);
			when(imageMapper.countAllImages()).thenReturn(0L);
			when(albumMapper.countAllAlbums()).thenReturn(0L);
			when(tagMapper.countAllTags()).thenReturn(0L);

			when(userMapper.countUsersByStatus(UserStatus.ACTIVE.getCode())).thenReturn(70L);
			when(userMapper.countUsersByStatus(UserStatus.TEMPORARILY_SUSPENDED.getCode())).thenReturn(15L);
			when(userMapper.countUsersByStatus(UserStatus.PERMANENTLY_SUSPENDED.getCode())).thenReturn(10L);
			when(userMapper.countUsersByStatus(UserStatus.WITHDRAWN.getCode())).thenReturn(5L);

			when(userMapper.countUsersByRole(anyLong())).thenReturn(0L);

			// When
			AdminDashboardService.DashboardStatistics result =
				adminDashboardService.getDashboardStatistics(adminRoleId);

			// Then
			AdminDashboardService.UserStatusStatistics userStatusStats = result.userStatusStats();
			assertEquals(15L, userStatusStats.temporarilySuspendedUsers());
			assertEquals(10L, userStatusStats.permanentlySuspendedUsers());
			assertEquals(25L, userStatusStats.totalSuspendedUsers()); // 15 + 10
		}
	}
}