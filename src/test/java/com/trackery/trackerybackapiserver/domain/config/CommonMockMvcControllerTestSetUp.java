package com.trackery.trackerybackapiserver.domain.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.enums.UserRole;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.config
 * fileName       : CommonMockMvcControllerTestSetUp
 * author         : durururuk
 * date           : 25. 2. 19.
 * description    : 컨트롤러 테스트를 위한 mockMvc를 사용할 때 필요한 필수 bean을 불러오는 추상 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 19.		durururuk		최초 생성
 * 25. 2. 19.		durururuk		MockMvc 테스트코드 작성을 도와주는 추상 클래스 추가
 * 25. 2. 19.		durururuk		코딩 컨벤션에 맞게 정리
 * 25. 2. 19.		durururuk		테스트 하고자 하는 컨트롤러만 로드하게 수정
 * 25. 3. 5.		durururuk		메일 컨트롤러 mockMvc 테스트 작성
 * 25. 3. 20.		durururuk		사용하지 않는 import 제거
 * 25. 3. 27.		durururuk		사용되지 않는 spy 임포트 삭제
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 * 25. 6. 13.		durururuk		앨범 생성, 앨범에 이미지 추가 API 문서화 코드 작성
 * 25. 9. 29.		inari		공통 CustomUserDetails 생성 메서드 추가
 * 25. 9. 30.		inari		UserRole enum 통합 및 상수 추가
 */
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({MockMvcUnitTestSecurityConfig.class, RestDocsConfiguration.class})
@ExtendWith(RestDocumentationExtension.class)
public abstract class CommonMockMvcControllerTestSetUp {
	@Autowired
	protected MockMvc mockMvc;

	protected ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	protected CustomUserDetails createUserUserDetails() {
		return new CustomUserDetails(UserRole.USER.getRoleId(), "user", UserRole.USER.getRoleId());
	}

	protected CustomUserDetails createManagerUserDetails() {
		return new CustomUserDetails(UserRole.MANAGER.getRoleId(), "manager", UserRole.MANAGER.getRoleId());
	}

	protected CustomUserDetails createAdminUserDetails() {
		return new CustomUserDetails(UserRole.ADMIN.getRoleId(), "admin", UserRole.ADMIN.getRoleId());
	}
}
