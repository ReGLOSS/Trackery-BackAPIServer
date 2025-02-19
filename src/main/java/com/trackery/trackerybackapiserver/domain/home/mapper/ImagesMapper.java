package com.trackery.trackerybackapiserver.domain.home.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.home.mapper
 * fileName       : ImagesMapper
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 이미지 데이터 처리를 위한 MyBatis Mapper 인터페이스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 */
@Mapper
public interface ImagesMapper {

	/**
	 * 공개되어있고 삭제되지 않은 이미지 주소를 가져옵니다.
	 * @return 이미지 목록
	 */
	List<String> selectPublicImageFiles();
}
