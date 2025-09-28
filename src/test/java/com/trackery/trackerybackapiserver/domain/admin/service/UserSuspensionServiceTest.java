package com.trackery.trackerybackapiserver.domain.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trackery.trackerybackapiserver.domain.admin.entity.UserSuspension;
import com.trackery.trackerybackapiserver.domain.admin.enums.SuspensionActionType;
import com.trackery.trackerybackapiserver.domain.admin.enums.UserStatus;
import com.trackery.trackerybackapiserver.domain.admin.mapper.UserSuspensionMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.service
 * fileName       : UserSuspensionServiceTest
 * author         : inari
 * date           : 25. 9. 28.
 * description    : 사용자 정지 서비스 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 28.		inari		최초 생성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserSuspensionService 테스트")
class UserSuspensionServiceTest {

	@Mock
	private UserSuspensionMapper userSuspensionMapper;

	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private UserSuspensionService userSuspensionService;

	private UserSuspension testSuspension;

	@BeforeEach
	void setUp() {
		testSuspension = createTestSuspension();
	}

	@Nested
	@DisplayName("사용자 정지")
	class SuspendUserTest {

		@Test
		@DisplayName("임시정지를 성공적으로 처리한다")
		void suspendUserTemporarilySuccess() {
			// Given
			Long userId = 1L;
			Integer suspensionType = UserStatus.TEMPORARILY_SUSPENDED.getCode();
			LocalDate endDate = LocalDate.now().plusDays(7);
			String reason = "부적절한 행동";
			Long adminId = 2L;

			when(userMapper.existsByUserId(eq(userId))).thenReturn(true);
			when(userSuspensionMapper.findActiveSuspensionByUserId(eq(userId))).thenReturn(null);
			doNothing().when(userSuspensionMapper).insertSuspension(any(UserSuspension.class));
			doNothing().when(userMapper).updateUserStatus(eq(userId), eq(suspensionType));

			// When
			userSuspensionService.suspendUser(userId, suspensionType, endDate, reason, adminId);

			// Then
			ArgumentCaptor<UserSuspension> suspensionCaptor = ArgumentCaptor.forClass(UserSuspension.class);
			verify(userSuspensionMapper).insertSuspension(suspensionCaptor.capture());
			verify(userMapper).updateUserStatus(userId, suspensionType);

			UserSuspension capturedSuspension = suspensionCaptor.getValue();
			assertEquals(userId, capturedSuspension.getUserId());
			assertEquals(suspensionType, capturedSuspension.getSuspensionType());
			assertEquals(endDate, capturedSuspension.getEndDate());
			assertEquals(reason, capturedSuspension.getReason());
			assertEquals(adminId, capturedSuspension.getAdminId());
			assertEquals(SuspensionActionType.SUSPEND.getActionType(), capturedSuspension.getActionType());
		}

		@Test
		@DisplayName("영구정지를 성공적으로 처리한다")
		void suspendUserPermanentlySuccess() {
			// Given
			Long userId = 1L;
			Integer suspensionType = UserStatus.PERMANENTLY_SUSPENDED.getCode();
			LocalDate endDate = LocalDate.now().plusDays(7); // 이 값은 무시되어야 함
			String reason = "심각한 위반";
			Long adminId = 2L;

			when(userMapper.existsByUserId(eq(userId))).thenReturn(true);
			when(userSuspensionMapper.findActiveSuspensionByUserId(eq(userId))).thenReturn(null);
			doNothing().when(userSuspensionMapper).insertSuspension(any(UserSuspension.class));
			doNothing().when(userMapper).updateUserStatus(eq(userId), eq(suspensionType));

			// When
			userSuspensionService.suspendUser(userId, suspensionType, endDate, reason, adminId);

			// Then
			ArgumentCaptor<UserSuspension> suspensionCaptor = ArgumentCaptor.forClass(UserSuspension.class);
			verify(userSuspensionMapper).insertSuspension(suspensionCaptor.capture());

			UserSuspension capturedSuspension = suspensionCaptor.getValue();
			assertNull(capturedSuspension.getEndDate()); // 영구정지는 종료일이 null이어야 함
		}

		@Test
		@DisplayName("존재하지 않는 사용자를 정지하려 하면 예외가 발생한다")
		void suspendNonexistentUserFails() {
			// Given
			Long userId = 999L;
			when(userMapper.existsByUserId(eq(userId))).thenReturn(false);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				userSuspensionService.suspendUser(userId, 2, LocalDate.now().plusDays(7), "사유", 2L));

			assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
		}

		@Test
		@DisplayName("이미 정지된 사용자를 정지하려 하면 예외가 발생한다")
		void suspendAlreadySuspendedUserFails() {
			// Given
			Long userId = 1L;
			when(userMapper.existsByUserId(eq(userId))).thenReturn(true);
			when(userSuspensionMapper.findActiveSuspensionByUserId(eq(userId))).thenReturn(testSuspension);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				userSuspensionService.suspendUser(userId, 2, LocalDate.now().plusDays(7), "사유", 2L));

			assertEquals(ErrorCode.BAD_REQUEST_ALREADY_SUSPENDED, exception.getErrorCode());
		}

		@Test
		@DisplayName("임시정지에서 종료일이 없으면 예외가 발생한다")
		void suspendTemporarilyWithoutEndDateFails() {
			// Given
			Long userId = 1L;
			Integer suspensionType = UserStatus.TEMPORARILY_SUSPENDED.getCode();
			when(userMapper.existsByUserId(eq(userId))).thenReturn(true);
			when(userSuspensionMapper.findActiveSuspensionByUserId(eq(userId))).thenReturn(null);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				userSuspensionService.suspendUser(userId, suspensionType, null, "사유", 2L));

			assertEquals(ErrorCode.BAD_REQUEST_SUSPENSION_END_DATE_REQUIRED, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("사용자 정지 해제")
	class UnsuspendUserTest {

		@Test
		@DisplayName("정지 해제를 성공적으로 처리한다")
		void unsuspendUserSuccess() {
			// Given
			Long userId = 1L;
			Long adminId = 2L;

			when(userMapper.existsByUserId(eq(userId))).thenReturn(true);
			when(userSuspensionMapper.findActiveSuspensionByUserId(eq(userId))).thenReturn(testSuspension);
			doNothing().when(userSuspensionMapper).deactivateAllSuspensionsByUserId(eq(userId));
			doNothing().when(userSuspensionMapper).insertSuspension(any(UserSuspension.class));
			doNothing().when(userMapper).updateUserStatus(eq(userId), eq(UserStatus.ACTIVE.getCode()));

			// When
			userSuspensionService.unsuspendUser(userId, adminId);

			// Then
			verify(userSuspensionMapper).deactivateAllSuspensionsByUserId(userId);
			verify(userMapper).updateUserStatus(userId, UserStatus.ACTIVE.getCode());

			ArgumentCaptor<UserSuspension> unsuspendLogCaptor = ArgumentCaptor.forClass(UserSuspension.class);
			verify(userSuspensionMapper).insertSuspension(unsuspendLogCaptor.capture());

			UserSuspension capturedLog = unsuspendLogCaptor.getValue();
			assertEquals(userId, capturedLog.getUserId());
			assertEquals(Integer.valueOf(1), capturedLog.getSuspensionType()); // 해제
			assertEquals(adminId, capturedLog.getAdminId());
			assertEquals(SuspensionActionType.UNSUSPEND.getActionType(), capturedLog.getActionType());
		}

		@Test
		@DisplayName("존재하지 않는 사용자의 정지를 해제하려 하면 예외가 발생한다")
		void unsuspendNonexistentUserFails() {
			// Given
			Long userId = 999L;
			when(userMapper.existsByUserId(eq(userId))).thenReturn(false);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				userSuspensionService.unsuspendUser(userId, 2L));

			assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
		}

		@Test
		@DisplayName("정지되지 않은 사용자의 정지를 해제하려 하면 예외가 발생한다")
		void unsuspendActiveUserFails() {
			// Given
			Long userId = 1L;
			when(userMapper.existsByUserId(eq(userId))).thenReturn(true);
			when(userSuspensionMapper.findActiveSuspensionByUserId(eq(userId))).thenReturn(null);

			// When & Then
			ApiException exception = assertThrows(ApiException.class, () ->
				userSuspensionService.unsuspendUser(userId, 2L));

			assertEquals(ErrorCode.BAD_REQUEST_ALREADY_ACTIVE, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("정지 정보 조회")
	class FindSuspensionTest {

		@Test
		@DisplayName("활성 정지 정보를 성공적으로 조회한다")
		void findActiveSuspensionSuccess() {
			// Given
			Long userId = 1L;
			when(userSuspensionMapper.findActiveSuspensionByUserId(eq(userId))).thenReturn(testSuspension);

			// When
			UserSuspension result = userSuspensionService.findActiveSuspensionByUserId(userId);

			// Then
			assertNotNull(result);
			assertEquals(testSuspension.getSuspensionId(), result.getSuspensionId());
			verify(userSuspensionMapper).findActiveSuspensionByUserId(userId);
		}

		@Test
		@DisplayName("정지 이력을 성공적으로 조회한다")
		void findSuspensionHistorySuccess() {
			// Given
			Long userId = 1L;
			List<UserSuspension> history = List.of(testSuspension);
			when(userSuspensionMapper.findSuspensionHistoryByUserId(eq(userId))).thenReturn(history);

			// When
			List<UserSuspension> result = userSuspensionService.findSuspensionHistoryByUserId(userId);

			// Then
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(testSuspension.getSuspensionId(), result.get(0).getSuspensionId());
			verify(userSuspensionMapper).findSuspensionHistoryByUserId(userId);
		}

		@Test
		@DisplayName("관리자 작업 로그를 성공적으로 조회한다")
		void findActionLogsSuccess() {
			// Given
			Long adminId = 2L;
			List<UserSuspension> logs = List.of(testSuspension);
			when(userSuspensionMapper.findActionLogsByAdminId(eq(adminId))).thenReturn(logs);

			// When
			List<UserSuspension> result = userSuspensionService.findActionLogsByAdminId(adminId);

			// Then
			assertNotNull(result);
			assertEquals(1, result.size());
			verify(userSuspensionMapper).findActionLogsByAdminId(adminId);
		}
	}

	@Nested
	@DisplayName("통계 조회")
	class StatisticsTest {

		@Test
		@DisplayName("총 정지 횟수를 성공적으로 조회한다")
		void countTotalSuspensionsSuccess() {
			// Given
			Long totalCount = 100L;
			when(userSuspensionMapper.countTotalSuspensions()).thenReturn(totalCount);

			// When
			Long result = userSuspensionService.countTotalSuspensions();

			// Then
			assertEquals(totalCount, result);
			verify(userSuspensionMapper).countTotalSuspensions();
		}

		@Test
		@DisplayName("현재 정지 중인 사용자 수를 성공적으로 조회한다")
		void countCurrentSuspendedUsersSuccess() {
			// Given
			Long suspendedCount = 20L;
			when(userSuspensionMapper.countCurrentSuspendedUsers()).thenReturn(suspendedCount);

			// When
			Long result = userSuspensionService.countCurrentSuspendedUsers();

			// Then
			assertEquals(suspendedCount, result);
			verify(userSuspensionMapper).countCurrentSuspendedUsers();
		}
	}

	private UserSuspension createTestSuspension() {
		return UserSuspension.builder()
			.userId(1L)
			.suspensionType(UserStatus.TEMPORARILY_SUSPENDED.getCode())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(7))
			.reason("테스트 정지")
			.adminId(2L)
			.actionType(SuspensionActionType.SUSPEND.getActionType())
			.build();
	}
}