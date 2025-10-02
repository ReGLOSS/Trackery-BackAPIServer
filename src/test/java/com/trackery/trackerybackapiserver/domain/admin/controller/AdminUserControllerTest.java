package com.trackery.trackerybackapiserver.domain.admin.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.admin.dto.request.UserRoleChangeRequestDto;
import com.trackery.trackerybackapiserver.domain.admin.dto.request.UserSuspensionRequestDto;
import com.trackery.trackerybackapiserver.domain.admin.entity.UserSuspension;
import com.trackery.trackerybackapiserver.domain.admin.service.AdminUserService;
import com.trackery.trackerybackapiserver.domain.admin.service.UserSuspensionService;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.user.entity.User;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.controller
 * fileName       : AdminUserControllerTest
 * author         : inari
 * date           : 25. 9. 28.
 * description    : 관리자 사용자 관리 컨트롤러 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 28.		inari		최초 생성
 * 25. 9. 30.		inari		코드스멜 수정
 */
@WebMvcTest(AdminUserController.class)
@DisplayName("AdminUserController 테스트")
class AdminUserControllerTest extends CommonMockMvcControllerTestSetUp {

	@MockitoBean
	private AdminUserService adminUserService;

	@MockitoBean
	private UserSuspensionService userSuspensionService;

	private User testUser;
	private UserSuspension testSuspension;

	@BeforeEach
	void setUp() {
		testUser = createTestUser();
		testSuspension = createTestSuspension();
	}

	@Nested
	@DisplayName("사용자 목록 조회")
	class GetUsersTest {

		@Test
		@DisplayName("관리자가 사용자 목록을 성공적으로 조회한다")
		void getUsersSuccess() throws Exception {
			// Given
			PageInfo<User> pageInfo = new PageInfo<>(List.of(testUser));
			when(adminUserService.getUserList(eq(3L), eq(1), eq(10))).thenReturn(pageInfo);
			when(userSuspensionService.findActiveSuspensionByUserId(anyLong())).thenReturn(null);

			// When & Then
			mockMvc.perform(get("/api/admin/users")
					.with(user(createAdminUserDetails()))
					.param("pageNum", "1")
					.param("pageSize", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andExpect(jsonPath("$.data.list").isArray())
				.andDo(document("admin-users-get",
					queryParameters(
						parameterWithName("pageNum").description("페이지 번호").optional(),
						parameterWithName("pageSize").description("페이지 크기").optional()
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.list").description("사용자 목록"),
						fieldWithPath("data.list[].userId").description("사용자 ID").optional(),
						fieldWithPath("data.list[].userName").description("사용자명"),
						fieldWithPath("data.list[].email").description("이메일"),
						fieldWithPath("data.list[].nickname").description("닉네임"),
						fieldWithPath("data.list[].status").description("사용자 상태"),
						fieldWithPath("data.list[].role").description("사용자 역할").optional(),
						fieldWithPath("data.list[].lastLogin").description("마지막 로그인"),
						fieldWithPath("data.list[].createdAt").description("생성일시"),
						fieldWithPath("data.list[].currentSuspension").description("현재 정지 정보").optional(),
						fieldWithPath("data.total").description("전체 사용자 수"),
						fieldWithPath("data.pageNum").description("현재 페이지 번호"),
						fieldWithPath("data.pageSize").description("페이지 크기"),
						fieldWithPath("data.pages").description("전체 페이지 수"),
						fieldWithPath("data.size").description("현재 페이지 크기"),
						fieldWithPath("data.startRow").description("시작 행"),
						fieldWithPath("data.endRow").description("끝 행"),
						fieldWithPath("data.prePage").description("이전 페이지"),
						fieldWithPath("data.nextPage").description("다음 페이지"),
						fieldWithPath("data.isFirstPage").description("첫 페이지 여부"),
						fieldWithPath("data.isLastPage").description("마지막 페이지 여부"),
						fieldWithPath("data.hasPreviousPage").description("이전 페이지 존재 여부"),
						fieldWithPath("data.hasNextPage").description("다음 페이지 존재 여부"),
						fieldWithPath("data.navigatePages").description("네비게이션 페이지 수"),
						fieldWithPath("data.navigatepageNums").description("네비게이션 페이지 번호들"),
						fieldWithPath("data.navigateFirstPage").description("네비게이션 첫 페이지"),
						fieldWithPath("data.navigateLastPage").description("네비게이션 마지막 페이지")
					)
				));

			verify(adminUserService).getUserList(3L, 1, 10);
		}

		@Test
		@DisplayName("페이지 크기가 100을 초과하면 100으로 제한된다")
		void getUsersWithLimitedPageSize() throws Exception {
			// Given
			PageInfo<User> pageInfo = new PageInfo<>(List.of(testUser));
			when(adminUserService.getUserList(eq(3L), eq(1), eq(100))).thenReturn(pageInfo);

			// When & Then
			mockMvc.perform(get("/api/admin/users")
					.with(user(createAdminUserDetails()))
					.param("pageNum", "1")
					.param("pageSize", "150"))
				.andExpect(status().isOk());

			verify(adminUserService).getUserList(3L, 1, 100);
		}
	}

	@Nested
	@DisplayName("사용자 상세 조회")
	class GetUserDetailTest {

		@Test
		@DisplayName("관리자가 사용자 상세 정보를 성공적으로 조회한다")
		void getUserDetailSuccess() throws Exception {
			// Given
			Long userId = 1L;
			when(adminUserService.getUserDetail(eq(3L), eq(userId))).thenReturn(testUser);
			when(userSuspensionService.findActiveSuspensionByUserId(eq(userId))).thenReturn(testSuspension);

			// When & Then
			mockMvc.perform(get("/api/admin/users/{userId}", userId)
					.with(user(createAdminUserDetails())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andExpect(jsonPath("$.data.userName").value("testUser"))
				.andDo(document("admin-user-detail-get",
					pathParameters(
						parameterWithName("userId").description("사용자 ID")
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.userId").description("사용자 ID").optional(),
						fieldWithPath("data.userName").description("사용자명"),
						fieldWithPath("data.email").description("이메일"),
						fieldWithPath("data.nickname").description("닉네임"),
						fieldWithPath("data.status").description("사용자 상태"),
						fieldWithPath("data.role").description("사용자 역할").optional(),
						fieldWithPath("data.lastLogin").description("마지막 로그인"),
						fieldWithPath("data.createdAt").description("생성일시"),
						fieldWithPath("data.profileImageUrl").description("프로필 이미지 URL"),
						fieldWithPath("data.currentSuspension.startDate").description("정지 시작일").optional(),
						fieldWithPath("data.currentSuspension.endDate").description("정지 종료일").optional(),
						fieldWithPath("data.currentSuspension.reason").description("정지 사유").optional(),
						fieldWithPath("data.currentSuspension.adminUsername").description("처리한 관리자명").optional(),
						fieldWithPath("data.activityStats.totalImages").description("총 이미지 수"),
						fieldWithPath("data.activityStats.totalAlbums").description("총 앨범 수"),
						fieldWithPath("data.activityStats.totalTags").description("총 태그 수")
					)
				));

			verify(adminUserService).getUserDetail(3L, userId);
			verify(userSuspensionService).findActiveSuspensionByUserId(userId);
		}
	}

	@Nested
	@DisplayName("사용자 정지")
	class SuspendUserTest {

		@Test
		@DisplayName("관리자가 사용자를 성공적으로 정지시킨다")
		void suspendUserSuccess() throws Exception {
			// Given
			Long userId = 1L;
			UserSuspensionRequestDto requestDto = new UserSuspensionRequestDto();
			requestDto.setSuspensionType(2);
			requestDto.setEndDate(LocalDate.now().plusDays(7));
			requestDto.setReason("부적절한 행동");

			doNothing().when(userSuspensionService).suspendUser(
				eq(userId), eq(2), any(LocalDate.class), eq("부적절한 행동"), eq(3L)
			);

			// When & Then
			mockMvc.perform(post("/api/admin/users/{userId}/suspend", userId)
					.with(user(createAdminUserDetails()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andDo(document("admin-user-suspend",
					pathParameters(
						parameterWithName("userId").description("정지할 사용자 ID")
					),
					requestFields(
						fieldWithPath("suspensionType").description("정지 유형 (2: 임시정지, 3: 영구정지)"),
						fieldWithPath("endDate").description("정지 종료일 (임시정지인 경우)").optional(),
						fieldWithPath("reason").description("정지 사유")
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지")
					)
				));

			verify(userSuspensionService).suspendUser(
				eq(userId), eq(2), any(LocalDate.class), eq("부적절한 행동"), eq(3L)
			);
		}

		@Test
		@DisplayName("자기 자신을 정지시키려 하면 실패한다")
		void suspendSelfFails() throws Exception {
			// Given
			Long userId = 3L; // 관리자 자신의 ID
			UserSuspensionRequestDto requestDto = new UserSuspensionRequestDto();
			requestDto.setSuspensionType(2);
			requestDto.setEndDate(LocalDate.now().plusDays(7));
			requestDto.setReason("테스트");

			// When & Then
			mockMvc.perform(post("/api/admin/users/{userId}/suspend", userId)
					.with(user(createAdminUserDetails()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isBadRequest()); // 400 오류
		}
	}

	@Nested
	@DisplayName("사용자 정지 해제")
	class UnsuspendUserTest {

		@Test
		@DisplayName("관리자가 사용자 정지를 성공적으로 해제한다")
		void unsuspendUserSuccess() throws Exception {
			// Given
			Long userId = 1L;
			doNothing().when(userSuspensionService).unsuspendUser(eq(userId), eq(3L));

			// When & Then
			mockMvc.perform(post("/api/admin/users/{userId}/unsuspend", userId)
					.with(user(createAdminUserDetails())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andDo(document("admin-user-unsuspend",
					pathParameters(
						parameterWithName("userId").description("정지 해제할 사용자 ID")
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지")
					)
				));

			verify(userSuspensionService).unsuspendUser(userId, 3L);
		}
	}

	@Nested
	@DisplayName("사용자 역할 변경")
	class ChangeUserRoleTest {

		@Test
		@DisplayName("ADMIN이 사용자 역할을 성공적으로 변경한다")
		void changeUserRoleSuccess() throws Exception {
			// Given
			Long userId = 1L;
			UserRoleChangeRequestDto requestDto = new UserRoleChangeRequestDto();
			requestDto.setRoleId(2);

			doNothing().when(adminUserService).changeUserRole(eq(3L), eq(3L), eq(userId), eq(2L));

			// When & Then
			mockMvc.perform(put("/api/admin/users/{userId}/role", userId)
					.with(user(createAdminUserDetails()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andDo(document("admin-user-role-change",
					pathParameters(
						parameterWithName("userId").description("역할을 변경할 사용자 ID")
					),
					requestFields(
						fieldWithPath("roleId").description("새로운 역할 ID (1: 일반사용자, 2: 매니저)")
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지")
					)
				));

			verify(adminUserService).changeUserRole(3L, 3L, userId, 2L);
		}
	}

	@Nested
	@DisplayName("정지 이력 조회")
	class GetSuspensionHistoryTest {

		@Test
		@DisplayName("관리자가 사용자의 정지 이력을 성공적으로 조회한다")
		void getSuspensionHistorySuccess() throws Exception {
			// Given
			Long userId = 1L;
			List<UserSuspension> suspensionHistory = List.of(testSuspension);
			when(userSuspensionService.findSuspensionHistoryByUserId(eq(userId))).thenReturn(suspensionHistory);

			// When & Then
			mockMvc.perform(get("/api/admin/users/{userId}/suspension-history", userId)
					.with(user(createAdminUserDetails())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andExpect(jsonPath("$.data").isArray())
				.andDo(document("admin-user-suspension-history-get",
					pathParameters(
						parameterWithName("userId").description("조회할 사용자 ID")
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data[]").description("정지 이력 목록"),
						fieldWithPath("data[].suspensionId").description("정지 ID"),
						fieldWithPath("data[].userId").description("사용자 ID"),
						fieldWithPath("data[].userName").description("사용자명"),
						fieldWithPath("data[].suspensionType").description("정지 유형"),
						fieldWithPath("data[].suspensionTypeDescription").description("정지 유형 설명"),
						fieldWithPath("data[].startDate").description("정지 시작일"),
						fieldWithPath("data[].endDate").description("정지 종료일").optional(),
						fieldWithPath("data[].reason").description("정지 사유"),
						fieldWithPath("data[].adminId").description("처리한 관리자 ID"),
						fieldWithPath("data[].adminUsername").description("처리한 관리자명"),
						fieldWithPath("data[].actionType").description("액션 유형"),
						fieldWithPath("data[].isActive").description("활성 상태"),
						fieldWithPath("data[].createdAt").description("생성일시")
					)
				));

			verify(userSuspensionService).findSuspensionHistoryByUserId(userId);
		}
	}

	@Nested
	@DisplayName("관리자 작업 로그 조회")
	class GetAdminActionLogsTest {

		@Test
		@DisplayName("관리자가 전체 작업 로그를 성공적으로 조회한다")
		void getAdminActionLogsSuccess() throws Exception {
			// Given
			List<UserSuspension> actionLogs = List.of(testSuspension);
			when(userSuspensionService.findActionLogsByAdminId(isNull())).thenReturn(actionLogs);

			// When & Then
			mockMvc.perform(get("/api/admin/users/logs/admin-actions")
					.with(user(createAdminUserDetails()))
					.param("pageNum", "1")
					.param("pageSize", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andExpect(jsonPath("$.data.list").isArray())
				.andDo(document("admin-action-logs-get",
					queryParameters(
						parameterWithName("adminId").description("조회할 관리자 ID").optional(),
						parameterWithName("pageNum").description("페이지 번호").optional(),
						parameterWithName("pageSize").description("페이지 크기").optional()
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.list").description("작업 로그 목록"),
						fieldWithPath("data.list[].suspensionId").description("정지 ID").optional(),
						fieldWithPath("data.list[].userId").description("사용자 ID"),
						fieldWithPath("data.list[].userName").description("사용자명"),
						fieldWithPath("data.list[].suspensionType").description("정지 유형"),
						fieldWithPath("data.list[].suspensionTypeDescription").description("정지 유형 설명"),
						fieldWithPath("data.list[].startDate").description("정지 시작일"),
						fieldWithPath("data.list[].endDate").description("정지 종료일").optional(),
						fieldWithPath("data.list[].reason").description("정지 사유"),
						fieldWithPath("data.list[].adminId").description("처리한 관리자 ID"),
						fieldWithPath("data.list[].adminUsername").description("처리한 관리자명"),
						fieldWithPath("data.list[].actionType").description("액션 유형"),
						fieldWithPath("data.list[].isActive").description("활성 상태"),
						fieldWithPath("data.list[].createdAt").description("생성일시").optional(),
						fieldWithPath("data.total").description("전체 로그 수"),
						fieldWithPath("data.pageNum").description("현재 페이지 번호"),
						fieldWithPath("data.pageSize").description("페이지 크기"),
						fieldWithPath("data.pages").description("전체 페이지 수"),
						fieldWithPath("data.size").description("현재 페이지 크기"),
						fieldWithPath("data.startRow").description("시작 행"),
						fieldWithPath("data.endRow").description("끝 행"),
						fieldWithPath("data.prePage").description("이전 페이지"),
						fieldWithPath("data.nextPage").description("다음 페이지"),
						fieldWithPath("data.isFirstPage").description("첫 페이지 여부"),
						fieldWithPath("data.isLastPage").description("마지막 페이지 여부"),
						fieldWithPath("data.hasPreviousPage").description("이전 페이지 존재 여부"),
						fieldWithPath("data.hasNextPage").description("다음 페이지 존재 여부"),
						fieldWithPath("data.navigatePages").description("네비게이션 페이지 수"),
						fieldWithPath("data.navigatepageNums").description("네비게이션 페이지 번호들"),
						fieldWithPath("data.navigateFirstPage").description("네비게이션 첫 페이지"),
						fieldWithPath("data.navigateLastPage").description("네비게이션 마지막 페이지")
					)
				));

			verify(userSuspensionService).findActionLogsByAdminId(null);
		}

		@Test
		@DisplayName("특정 관리자의 작업 로그를 성공적으로 조회한다")
		void getSpecificAdminActionLogsSuccess() throws Exception {
			// Given
			Long adminId = 2L;
			List<UserSuspension> actionLogs = List.of(testSuspension);
			when(userSuspensionService.findActionLogsByAdminId(eq(adminId))).thenReturn(actionLogs);

			// When & Then
			mockMvc.perform(get("/api/admin/users/logs/admin-actions")
					.with(user(createAdminUserDetails()))
					.param("adminId", adminId.toString())
					.param("pageNum", "1")
					.param("pageSize", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"));

			verify(userSuspensionService).findActionLogsByAdminId(adminId);
		}
	}

	@Nested
	@DisplayName("유틸리티 메서드 테스트")
	class UtilityMethodTest {

		@Test
		@DisplayName("정지 이력 조회 시 suspensionTypeDescription이 올바르게 변환된다")
		void suspensionTypeDescriptionConversionTest() throws Exception {
			// Given
			Long userId = 1L;
			UserSuspension temporarySuspension = UserSuspension.builder()
				.userId(userId)
				.suspensionType(2) // 임시정지
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusDays(7))
				.reason("임시정지 테스트")
				.adminId(2L)
				.actionType("SUSPEND")
				.build();

			UserSuspension permanentSuspension = UserSuspension.builder()
				.userId(userId)
				.suspensionType(3) // 영구정지
				.startDate(LocalDate.now())
				.endDate(null)
				.reason("영구정지 테스트")
				.adminId(2L)
				.actionType("SUSPEND")
				.build();

			UserSuspension releaseSuspension = UserSuspension.builder()
				.userId(userId)
				.suspensionType(null) // 해제
				.startDate(LocalDate.now())
				.endDate(null)
				.reason("정지 해제")
				.adminId(2L)
				.actionType("UNSUSPEND")
				.build();

			List<UserSuspension> suspensionHistory = List.of(temporarySuspension, permanentSuspension, releaseSuspension);
			when(userSuspensionService.findSuspensionHistoryByUserId(eq(userId))).thenReturn(suspensionHistory);

			// When & Then
			mockMvc.perform(get("/api/admin/users/{userId}/suspension-history", userId)
					.with(user(createAdminUserDetails())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].suspensionTypeDescription").value("임시정지"))
				.andExpect(jsonPath("$.data[1].suspensionTypeDescription").value("영구정지"))
				.andExpect(jsonPath("$.data[2].suspensionTypeDescription").value("해제"));

			verify(userSuspensionService).findSuspensionHistoryByUserId(userId);
		}

		@Test
		@DisplayName("사용자 목록 조회 시 role이 올바르게 변환된다")
		void adminRoleConversionTest() throws Exception {
			// Given
			User regularUser = User.builder()
				.userName("regularUser")
				.email("regular@example.com")
				.nickname("regular")
				.status(1)
				.roleId(1L) // 일반 사용자
				.lastLogin(LocalDateTime.now())
				.startDate(LocalDateTime.now().minusDays(30))
				.userProfile("http://example.com/regular.jpg")
				.build();

			User managerUser = User.builder()
				.userName("managerUser")
				.email("manager@example.com")
				.nickname("manager")
				.status(1)
				.roleId(2L) // 매니저
				.lastLogin(LocalDateTime.now())
				.startDate(LocalDateTime.now().minusDays(30))
				.userProfile("http://example.com/manager.jpg")
				.build();

			User adminUser = User.builder()
				.userName("adminUser")
				.email("admin@example.com")
				.nickname("admin")
				.status(1)
				.roleId(3L) // 관리자
				.lastLogin(LocalDateTime.now())
				.startDate(LocalDateTime.now().minusDays(30))
				.userProfile("http://example.com/admin.jpg")
				.build();

			PageInfo<User> pageInfo = new PageInfo<>(List.of(regularUser, managerUser, adminUser));
			when(adminUserService.getUserList(eq(3L), eq(1), eq(10))).thenReturn(pageInfo);
			when(userSuspensionService.findActiveSuspensionByUserId(anyLong())).thenReturn(null);

			// When & Then
			mockMvc.perform(get("/api/admin/users")
					.with(user(createAdminUserDetails()))
					.param("pageNum", "1")
					.param("pageSize", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.list[0].role").value("USER"))
				.andExpect(jsonPath("$.data.list[1].role").value("MANAGER"))
				.andExpect(jsonPath("$.data.list[2].role").value("ADMIN"));

			verify(adminUserService).getUserList(3L, 1, 10);
		}

		@Test
		@DisplayName("사용자 상세 조회 시 role이 올바르게 변환된다")
		void userDetailAdminRoleConversionTest() throws Exception {
			// Given
			Long userId = 1L;
			User managerUser = User.builder()
				.userName("managerUser")
				.email("manager@example.com")
				.nickname("manager")
				.status(1)
				.roleId(2L) // 매니저
				.lastLogin(LocalDateTime.now())
				.startDate(LocalDateTime.now().minusDays(30))
				.userProfile("http://example.com/manager.jpg")
				.build();

			when(adminUserService.getUserDetail(eq(3L), eq(userId))).thenReturn(managerUser);
			when(userSuspensionService.findActiveSuspensionByUserId(eq(userId))).thenReturn(testSuspension);

			// When & Then
			mockMvc.perform(get("/api/admin/users/{userId}", userId)
					.with(user(createAdminUserDetails())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.role").value("MANAGER"));

			verify(adminUserService).getUserDetail(3L, userId);
			verify(userSuspensionService).findActiveSuspensionByUserId(userId);
		}
	}

	private User createTestUser() {
		return User.builder()
			.userName("testUser")
			.email("test@example.com")
			.nickname("testNick")
			.status(1)
			.roleId(1L)
			.lastLogin(LocalDateTime.now())
			.startDate(LocalDateTime.now().minusDays(30))
			.userProfile("http://example.com/profile.jpg")
			.build();
	}

	private UserSuspension createTestSuspension() {
		return UserSuspension.builder()
			.userId(1L)
			.suspensionType(2)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(7))
			.reason("테스트 정지")
			.adminId(2L)
			.actionType("SUSPEND")
			.build();
	}
}
