package com.trackery.trackerybackapiserver.domain.image.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.trackery.trackerybackapiserver.domain.image.entity.Image;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image
 * fileName       : ImageMapper
 * author         : inari
 * date           : 25. 4. 17.
 * description    : 이미지 데이터 처리를 위한 MyBatis Mapper 인터페이스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 */
@Mapper
public interface ImageMapper {

	/**
	 * 공개되어있고 삭제되지 않은 이미지 주소를 가져옵니다.
	 * @return 이미지 목록
	 */
	List<String> selectPublicImageFiles();

	/**
	 * 이미지의 메타데이터를 DB에 삽입합니다.
	 * @param image 이미지 엔티티 객체
	 */
	void insertImage(Image image);

	Optional<Image> findImageByImageId(@Param("imageId") Long imageId);

	List<Image> findImagesByUserId(@Param("userId") Long userId);

	boolean isImageExist(Long imageId);
}
