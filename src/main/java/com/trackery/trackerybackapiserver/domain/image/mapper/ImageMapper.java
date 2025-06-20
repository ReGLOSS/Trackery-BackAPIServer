package com.trackery.trackerybackapiserver.domain.image.mapper;

import java.time.LocalDateTime;
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
 * 25. 6. 16.        inari       지도를 통한 이미지 조회 기능 추가
 * 25. 6. 20.		 inari		 이미지 수정 및 삭제 추가
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

	/**
	 * 특정 시도에 등록된 특정 사용자의 이미지를 조회합니다.
	 * @param sidoId 시도 ID
	 * @param userId 사용자 ID
	 * @return 해당 시도의 사용자 이미지 목록
	 */
	List<Image> findImagesBySidoIdAndUserId(@Param("sidoId") Long sidoId, @Param("userId") Long userId);

	/**
	 * 특정 시군구에 등록된 특정 사용자의 이미지를 조회합니다.
	 * @param sigunguId 시군구 ID
	 * @param userId 사용자 ID
	 * @return 해당 시군구의 사용자 이미지 목록
	 */
	List<Image> findImagesBySigunguIdAndUserId(@Param("sigunguId") Long sigunguId, @Param("userId") Long userId);

	/**
	 * 이미지의 메타데이터를 수정합니다.
	 * @param imageId 이미지 ID
	 * @param imageName 이미지 이름
	 * @param imageContent 이미지 설명
	 * @param imageDate 이미지 촬영 날짜
	 * @param isPublic 공개 여부
	 * @return 수정된 행의 수
	 */
	int updateImageMetadata(@Param("imageId") Long imageId,
			@Param("imageName") String imageName,
			@Param("imageContent") String imageContent,
			@Param("imageDate") LocalDateTime imageDate,
			@Param("isPublic") Integer isPublic);

	/**
	 * 이미지를 논리적으로 삭제합니다 (is_deleted = 1).
	 * @param imageId 이미지 ID
	 * @return 삭제된 행의 수
	 */
	int deleteImage(@Param("imageId") Long imageId);

	/**
	 * 이미지의 좌표 정보를 수정합니다.
	 * @param imageId 이미지 ID
	 * @param coordinatePointId 새로운 좌표 포인트 ID
	 * @return 수정된 행의 수
	 */
	int updateImageLocation(@Param("imageId") Long imageId, @Param("coordinatePointId") Long coordinatePointId);
}
