package com.trackery.trackerybackapiserver.domain.common.util;

import java.util.List;
import java.util.function.Function;

import com.github.pagehelper.PageInfo;

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
 */
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
		List<R> convertedList = source.getList().stream()
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
