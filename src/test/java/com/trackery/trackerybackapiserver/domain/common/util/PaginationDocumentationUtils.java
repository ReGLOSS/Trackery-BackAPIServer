package com.trackery.trackerybackapiserver.domain.common.util;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.util
 * fileName       : PaginationDocumentationUtils
 * author         : durururuk
 * date           : 25. 6. 24.
 * description    : 페이지네이션 문서화 템플릿
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 24.		durururuk		최초 생성
 * 25. 6. 24.		durururuk		페이지네이션 문서화 클래스 작성
 * 25. 6. 24.		durururuk		쿼리 파라미터도 문서화 클래스에서 받아올 수 있게 수정
 */
public class PaginationDocumentationUtils {
	public static FieldDescriptor[] getPageableResponseFields() {
		return new FieldDescriptor[] {
			fieldWithPath("data.total").description("전체 항목 수"),
			fieldWithPath("data.pageNum").description("현재 페이지 번호"),
			fieldWithPath("data.pageSize").description("페이지당 항목 수"),
			fieldWithPath("data.size").description("현재 페이지의 실제 항목 수"),
			fieldWithPath("data.startRow").description("시작 행 번호"),
			fieldWithPath("data.endRow").description("끝 행 번호"),
			fieldWithPath("data.pages").description("전체 페이지 수"),
			fieldWithPath("data.prePage").description("이전 페이지 번호"),
			fieldWithPath("data.nextPage").description("다음 페이지 번호"),
			fieldWithPath("data.isFirstPage").description("첫 번째 페이지 여부"),
			fieldWithPath("data.isLastPage").description("마지막 페이지 여부"),
			fieldWithPath("data.hasPreviousPage").description("이전 페이지 존재 여부"),
			fieldWithPath("data.hasNextPage").description("다음 페이지 존재 여부"),
			fieldWithPath("data.navigatePages").description("네비게이션 페이지 수"),
			fieldWithPath("data.navigatepageNums").description("네비게이션 페이지 번호 배열"),
			fieldWithPath("data.navigateFirstPage").description("네비게이션 첫 페이지"),
			fieldWithPath("data.navigateLastPage").description("네비게이션 마지막 페이지")
		};
	}

	public static ParameterDescriptor[] getPageableQueryParameters() {
		return new ParameterDescriptor[] {
			parameterWithName("pageNum").description("페이지 번호 (기본값 : 1)"),
			parameterWithName("pageSize").description("페이지 크기 (기본값 : 10)")
		};
	}
}
