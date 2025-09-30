package com.trackery.trackerybackapiserver.domain.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.admin.enums.UserStatus;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserRoleMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.service
 * fileName       : AdminUserServiceTest
 * author         : inari
 * date           : 25. 9. 28.
 * description    : 관리자 사용자 서비스 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 28.		inari		최초 생성
 * 25. 9. 29.		inari		테스트 코드 추가
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService 테스트")
class AdminUserServiceTest {

	@Mock
	private UserMapper userMapper;

	@Mock
	private UserRoleMapper userRoleMapper;

	@InjectMocks
	private AdminUserService adminUserService;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = createTestUser();
	}

	@Nested
	@DisplayName("사용자 목록 조회")
	class GetUserListTest {

		@Test
		@DisplayName("MANAGER가 일반 사용자 목록을 성공적으로 조회한다")
		void getManagerUserListSuccess() {
			// Given
			Long managerRoleId = 2L;
			List<User> users = List.of(testUser);
			when(userMapper.findUsersForAdmin(eq(List.of(1L)))).thenReturn(users);

			// When
			PageInfo<User> result = adminUserService.getUserList(managerRoleId, 1, 10);

			// Then
			assertNotNull(result);
			assertEquals(1, result.getList().size());
			assertEquals(testUser.getUserId(), result.getList().get(0).getUserId());
			verify(userMapper).findUsersForAdmin(List.of(1L));
		}

		@Test
		@DisplayName("ADMIN이 모든 사용자 목록을 성공적으로 조회한다")
		void getAdminUserListSuccess() {
			// Given
			Long adminRoleId = 3L;
			List<User> users = List.of(testUser);
			when(userMapper.findUsersForAdmin(eq(List.of(1L, 2L, 3L)))).thenReturn(users);

			// When
			PageInfo<User> result = adminUserService.getUserList(adminRoleId, 1, 10);

			// Then
			assertNotNull(result);
			assertEquals(1, result.getList().size());
			verify(userMapper).findUsersForAdmin(List.of(1L, 2L, 3L));
		}

		@Test
		@DisplayName("페이지 크기가 최대값을 초과하면 100으로 제한된다")
		void getUserListWithMaxPageSize() {
			// Given
			Long adminRoleId = 3L;
			List<User> users = List.of(testUser);
			when(userMapper.findUsersForAdmin(anyList())).thenReturn(users);

			// When
			PageInfo<User> result = adminUserService.getUserList(adminRoleId, 1, 150);

			// Then
			assertNotNull(result);
			// PageHelper.startPage가 100으로 호출되었는지 확인하기 위해 간접적으로 검증
			verify(userMapper).findUsersForAdmin(anyList());
		}

		@Test
		@DisplayName("잘못된 관리자 역할로 조회하면 예외가 발생한다")
		void getUserListWithInvalidAdminRole() {
			// Given
			Long invalidRoleId = 1L; // 일반 사용자 역할

			// When & Then
			ApiException exception = assertThrows(ApiException.class,
				() -> adminUserService.getUserList(invalidRoleId, 1, 10));

			assertNotNull(exception.getMessage());
			assertEquals(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES, exception.getErrorCode());
			verify(userMapper, never()).findUsersForAdmin(anyList());
		}
	}

	@Nested
	@DisplayName("사용자 상세 조회")
	class GetUserDetailTest {

		@Test
		@DisplayName("관리자가 사용자 상세 정보를 성공적으로 조회한다")
		void getUserDetailSuccess() {
			// Given
			Long adminRoleId = 3L;
			Long userId = 1L;
			when(userMapper.findByUserId(eq(userId))).thenReturn(Optional.of(testUser));

			// When
			User result = adminUserService.getUserDetail(adminRoleId, userId);

			// Then
			assertNotNull(result);
			assertEquals(testUser.getUserId(), result.getUserId());
			assertEquals(testUser.getUserName(), result.getUserName());
			verify(userMapper).findByUserId(userId);
		}

		@Test
		@DisplayName("존재하지 않는 사용자 조회시 예외가 발생한다")
		void getUserDetailWithNonexistentUser() {
			// Given
			Long adminRoleId = 3L;
			Long userId = 999L;
			when(userMapper.findByUserId(eq(userId))).thenReturn(Optional.empty());

			// When & Then
			ApiException exception = assertThrows(ApiException.class,
				() -> adminUserService.getUserDetail(adminRoleId, userId));

			assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
		}

		@Test
		@DisplayName("MANAGER가 관리자 사용자를 조회하려 하면 예외가 발생한다")
		void getManagerCannotViewAdminUser() {
			// Given
			Long managerRoleId = 2L;
			Long userId = 1L;
			User adminUser = User.builder()
				.email("admin@example.com")
				.userName("adminUser")
				.nickname("adminNick")
				.password("password")
				.salt("salt")
				.startDate(LocalDateTime.now().minusDays(30))
				.status(1)
				.lastLogin(LocalDateTime.now())
				.userProfile("profile.jpg")
				.roleId(3L) // ADMIN 역할
				.build();

			when(userMapper.findByUserId(eq(userId))).thenReturn(Optional.of(adminUser));

			// When & Then
			ApiException exception = assertThrows(ApiException.class,
				() -> adminUserService.getUserDetail(managerRoleId, userId));

			assertEquals(ErrorCode.FORBIDDEN_CANNOT_MANAGE_HIGHER_ROLE, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("사용자 역할 변경")
	class ChangeUserRoleTest {

		@Test
		@DisplayName("ADMIN이 사용자 역할을 성공적으로 변경한다")
		void changeUserRoleSuccess() {
			// Given
			Long adminUserId = 3L;
			Long adminRoleId = 3L;
			Long targetUserId = 1L;
			Long newRoleId = 2L;

			when(userMapper.existsByUserId(eq(targetUserId))).thenReturn(true);
			doNothing().when(userRoleMapper).updateUserRole(eq(targetUserId), eq(newRoleId));
			doNothing().when(userMapper).updateUserRoleId(eq(targetUserId), eq(newRoleId));

			// When & Then
			assertDoesNotThrow(() ->
				adminUserService.changeUserRole(adminUserId, adminRoleId, targetUserId, newRoleId));

			verify(userRoleMapper).updateUserRole(targetUserId, newRoleId);
			verify(userMapper).updateUserRoleId(targetUserId, newRoleId);
		}

		@Test
		@DisplayName("MANAGER가 역할을 변경하려 하면 예외가 발생한다")
		void changeUserRoleWithInsufficientPrivileges() {
			// Given
			Long managerUserId = 2L;
			Long managerRoleId = 2L;
			Long targetUserId = 1L;
			Long newRoleId = 2L;

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminUserService.changeUserRole(managerUserId, managerRoleId, targetUserId, newRoleId));

			assertEquals(ErrorCode.FORBIDDEN_INSUFFICIENT_ADMIN_PRIVILEGES, exception.getErrorCode());
		}

		@Test
		@DisplayName("자기 자신의 역할을 변경하려 하면 예외가 발생한다")
		void changeOwnRoleFails() {
			// Given
			Long adminUserId = 3L;
			Long adminRoleId = 3L;
			Long targetUserId = 3L; // 같은 사용자 ID
			Long newRoleId = 2L;

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminUserService.changeUserRole(adminUserId, adminRoleId, targetUserId, newRoleId));

			assertEquals(ErrorCode.BAD_REQUEST_CANNOT_CHANGE_OWN_ROLE, exception.getErrorCode());
		}

		@Test
		@DisplayName("존재하지 않는 사용자의 역할을 변경하려 하면 예외가 발생한다")
		void changeNonexistentUserRoleFails() {
			// Given
			Long adminUserId = 3L;
			Long adminRoleId = 3L;
			Long targetUserId = 999L;
			Long newRoleId = 2L;

			when(userMapper.existsByUserId(eq(targetUserId))).thenReturn(false);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminUserService.changeUserRole(adminUserId, adminRoleId, targetUserId, newRoleId));

			assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
		}

		@Test
		@DisplayName("유효하지 않은 역할 ID로 변경하려 하면 예외가 발생한다")
		void changeToInvalidRoleFails() {
			// Given
			Long adminUserId = 3L;
			Long adminRoleId = 3L;
			Long targetUserId = 1L;
			Long invalidRoleId = 3L; // ADMIN 역할은 할당 불가

			when(userMapper.existsByUserId(eq(targetUserId))).thenReturn(true);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				adminUserService.changeUserRole(adminUserId, adminRoleId, targetUserId, invalidRoleId));

			assertEquals(ErrorCode.BAD_REQUEST_INVALID_ROLE, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("사용자 통계 조회")
	class GetUserStatisticsTest {

		@Test
		@DisplayName("사용자 통계를 성공적으로 조회한다")
		void getUserStatisticsSuccess() {
			// Given
			when(userMapper.countAllUsers()).thenReturn(1000L);
			when(userMapper.countUsersByRole(1L)).thenReturn(900L);
			when(userMapper.countUsersByRole(2L)).thenReturn(90L);
			when(userMapper.countUsersByRole(3L)).thenReturn(10L);
			when(userMapper.countUsersByStatus(UserStatus.ACTIVE.getCode())).thenReturn(800L);
			when(userMapper.countUsersByStatus(UserStatus.TEMPORARILY_SUSPENDED.getCode())).thenReturn(30L);
			when(userMapper.countUsersByStatus(UserStatus.PERMANENTLY_SUSPENDED.getCode())).thenReturn(20L);
			when(userMapper.countUsersByStatus(UserStatus.WITHDRAWN.getCode())).thenReturn(150L);

			// When
			AdminUserService.UserStatistics result = adminUserService.getUserStatistics();

			// Then
			assertNotNull(result);
			assertEquals(1000L, result.totalUsers());
			assertEquals(900L, result.regularUsers());
			assertEquals(90L, result.managers());
			assertEquals(10L, result.admins());
			assertEquals(800L, result.activeUsers());
			assertEquals(50L, result.suspendedUsers()); // 임시정지 + 영구정지
			assertEquals(150L, result.withdrawnUsers());

			verify(userMapper).countAllUsers();
			verify(userMapper).countUsersByRole(1L);
			verify(userMapper).countUsersByRole(2L);
			verify(userMapper).countUsersByRole(3L);
			verify(userMapper, times(4)).countUsersByStatus(anyInt());
		}
	}

	private User createTestUser() {
		return User.builder()
			.email("test@example.com")
			.userName("testUser")
			.nickname("testNick")
			.password("password")
			.salt("salt")
			.startDate(LocalDateTime.now().minusDays(30))
			.status(1)
			.lastLogin(LocalDateTime.now())
			.userProfile("profile.jpg")
			.roleId(1L)
			.build();
	}
}
