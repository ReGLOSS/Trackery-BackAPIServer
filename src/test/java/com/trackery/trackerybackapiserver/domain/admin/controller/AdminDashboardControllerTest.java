package com.trackery.trackerybackapiserver.domain.admin.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.trackery.trackerybackapiserver.domain.admin.service.AdminDashboardService;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.controller
 * fileName       : AdminDashboardControllerTest
 * author         : inari
 * date           : 25. 9. 28.
 * description    : 관리자 대시보드 컨트롤러 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 28.		inari		최초 생성
 * 25. 9. 29.		inari		테스트 코드 추가
 * 25. 9. 30.		inari		코드스멜 수정
 */
@WebMvcTest(AdminDashboardController.class)
@DisplayName("AdminDashboardController 테스트")
class AdminDashboardControllerTest extends CommonMockMvcControllerTestSetUp {

	@MockitoBean
	private AdminDashboardService adminDashboardService;

	private AdminDashboardService.DashboardStatistics testDashboardStatistics;

	@BeforeEach
	void setUp() {
		testDashboardStatistics = createTestDashboardStatistics();
	}

	@Nested
	@DisplayName("대시보드 통계 조회")
	class GetDashboardTest {

		@Test
		@DisplayName("ADMIN이 대시보드 통계를 성공적으로 조회한다")
		void getDashboardSuccess() throws Exception {
			// Given
			when(adminDashboardService.getDashboardStatistics(3L)).thenReturn(testDashboardStatistics);

			// When & Then
			mockMvc.perform(get("/api/admin/dashboard")
					.with(user(createAdminUserDetails())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andExpect(jsonPath("$.data.totalUsers").value(1000))
				.andExpect(jsonPath("$.data.totalImages").value(5000))
				.andExpect(jsonPath("$.data.totalAlbums").value(500))
				.andExpect(jsonPath("$.data.totalTags").value(200))
				.andExpect(jsonPath("$.data.userStatusStats.activeUsers").value(800))
				.andExpect(jsonPath("$.data.userStatusStats.withdrawnUsers").value(150))
				.andExpect(jsonPath("$.data.roleStats.regularUsers").value(900))
				.andExpect(jsonPath("$.data.roleStats.managers").value(90))
				.andExpect(jsonPath("$.data.roleStats.admins").value(10))
				.andDo(document("admin-dashboard-get",
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.totalUsers").description("전체 사용자 수"),
						fieldWithPath("data.totalImages").description("전체 이미지 수"),
						fieldWithPath("data.totalAlbums").description("전체 앨범 수"),
						fieldWithPath("data.totalTags").description("전체 태그 수"),
						fieldWithPath("data.userStatusStats").description("사용자 상태별 통계"),
						fieldWithPath("data.userStatusStats.activeUsers").description("활성 사용자 수"),
						fieldWithPath("data.userStatusStats.suspendedUsers").description("정지된 사용자 수"),
						fieldWithPath("data.userStatusStats.withdrawnUsers").description("탈퇴한 사용자 수"),
						fieldWithPath("data.roleStats").description("역할별 통계"),
						fieldWithPath("data.roleStats.regularUsers").description("일반 사용자 수"),
						fieldWithPath("data.roleStats.managers").description("매니저 수"),
						fieldWithPath("data.roleStats.admins").description("관리자 수")
					)
				));

			verify(adminDashboardService).getDashboardStatistics(3L);
		}

		@Test
		@DisplayName("MANAGER 권한으로는 접근할 수 없다")
		void getDashboardForbiddenForManager() throws Exception {
			// When & Then
			mockMvc.perform(get("/api/admin/dashboard")
					.with(user(createManagerUserDetails())))
				.andExpect(status().isForbidden());

			verify(adminDashboardService, never()).getDashboardStatistics(anyLong());
		}

		@Test
		@DisplayName("일반 사용자는 접근할 수 없다")
		void getDashboardForbiddenForUser() throws Exception {
			// When & Then
			mockMvc.perform(get("/api/admin/dashboard")
					.with(user(createUserUserDetails())))
				.andExpect(status().isForbidden());

			verify(adminDashboardService, never()).getDashboardStatistics(anyLong());
		}
	}

	private AdminDashboardService.DashboardStatistics createTestDashboardStatistics() {
		AdminDashboardService.UserStatusStatistics userStatusStats =
			new AdminDashboardService.UserStatusStatistics(
				800L, // activeUsers
				30L,  // temporarilySuspendedUsers
				20L,  // permanentlySuspendedUsers
				50L,  // totalSuspendedUsers
				150L  // withdrawnUsers
			);

		AdminDashboardService.RoleStatistics roleStats =
			new AdminDashboardService.RoleStatistics(900L, 90L, 10L);

		return new AdminDashboardService.DashboardStatistics(
			1000L, // totalUsers
			5000L, // totalImages
			500L,  // totalAlbums
			200L,  // totalTags
			userStatusStats,
			roleStats
		);
	}
}
