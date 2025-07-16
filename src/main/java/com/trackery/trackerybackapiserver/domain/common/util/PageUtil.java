package com.trackery.trackerybackapiserver.domain.common.util;

import java.util.List;
import java.util.function.Function;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.util
 * fileName       : PageUtil
 * author         : durururuk
 * date           : 25. 7. 7.
 * description    : 페이지네이션과 관련된 유틸 메서드를 모아둔 유틸 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 7.		durururuk		최초 생성
 * 25. 7. 7.		durururuk		PageUtil로 페이징 정보를 처리하는 로직 이관
 * 25. 7. 10.		durururuk		PageUtil convert 메서드 null 체크 추가
 */
@Slf4j
public class PageUtil {
	private PageUtil() {
	}

	/**
	 * PageInfo의 페이징 정보를 유지하면서 리스트 내용만 변환
	 * @param source 원본 PageInfo
	 * @param newList 변환된 새로운 리스트
	 * @return 변환된 PageInfo
	 */
	public static <T, R> PageInfo<R> convert(PageInfo<T> source, List<R> newList) {
		if (source == null) {
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_PAGE_SOURCE_NULL);
		}
		if (newList == null) {
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_PAGE_NEW_LIST_NULL);
		}
		PageInfo<R> target = new PageInfo<>(newList);
		copyPageProperties(source, target);
		return target;
	}

	/**
	 * PageInfo의 페이징 정보를 유지하면서 Function을 통해 리스트 변환
	 *
	 * @param source 원본 PageInfo
	 * @param converter 변환 함수
	 * @return 변환된 PageInfo
	 */
	public static <T, R> PageInfo<R> convert(PageInfo<T> source, Function<T, R> converter) {
		if (source == null) {
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_PAGE_SOURCE_NULL);
		}
		if (converter == null) {
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_PAGE_CONVERTER_NULL);
		}
		List<T> sourceList = source.getList();
		if (sourceList == null) {
			sourceList = List.of();
		}
		List<R> convertedList = sourceList.stream()
			.map(converter)
			.toList();
		return convert(source, convertedList);
	}

	/**
	 * 페이지네이션 정보가 들어있는 원본 리스트에서 새로운 리스트로 페이지네이션 정보 복사
	 * @param source 원본 PageInfo
	 * @param target 복사될 PageInfo
	 */
	private static void copyPageProperties(PageInfo<?> source, PageInfo<?> target) {
		target.setTotal(source.getTotal());
		target.setPages(source.getPages());
		target.setPageNum(source.getPageNum());
		target.setPageSize(source.getPageSize());
		target.setStartRow(source.getStartRow());
		target.setEndRow(source.getEndRow());
		target.setPrePage(source.getPrePage());
		target.setNextPage(source.getNextPage());
		target.setIsFirstPage(source.isIsFirstPage());
		target.setIsLastPage(source.isIsLastPage());
		target.setHasPreviousPage(source.isHasPreviousPage());
		target.setHasNextPage(source.isHasNextPage());
		target.setNavigatePages(source.getNavigatePages());
		target.setNavigatepageNums(source.getNavigatepageNums());
		target.setNavigateFirstPage(source.getNavigateFirstPage());
		target.setNavigateLastPage(source.getNavigateLastPage());
	}
}