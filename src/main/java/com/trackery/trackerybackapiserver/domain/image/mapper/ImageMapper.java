package com.trackery.trackerybackapiserver.domain.image.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageInfoForThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageSearchByUserIdDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.mapper
 * fileName       : ImageMapper
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 이미지 데이터 처리를 위한 MyBatis Mapper 인터페이스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.		inari		최초 생성
 * 25. 2. 14.		inari		랜덤이미지 가져오기 구현
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 4. 21.		durururuk		mybatis에서 spatial 타입을 사용하기 위한 타입 핸들러 적용
 * 25. 4. 21.		durururuk		이미지 업로드 기능 구현
 * 25. 4. 23.		durururuk		ImageUploadController JavaDoc 주석 작성
 * 25. 5. 15.		durururuk		이미지 조회 기능 구현
 * 25. 5. 15.		durururuk		이미지 조회 기능 폴더 지정 오류로 안 되던 문제 수정
 * 25. 5. 21.		durururuk		앨범에 이미지 추가하는 기능 작성
 * 25. 6. 16.		inari		location 도메인과 image 도메인 리팩토링 및 지도 이미지 조회기능 추가
 * 25. 6. 20.		inari		이미지 수정 및 삭제기능 추가
 * 25. 7. 7.		durururuk		쿼리가 산발적으로 돼있어서 페이지네이션 정보가 실제 값과 일치하지 않던 문제 수정
 * 25. 7. 8.		durururuk		내 이미지 조회 시 조회 결과에서 제외될 앨범 ID 파라미터 추가
 * 25. 7. 8.		durururuk		사용되지 않는 메서드 정리
 * 25. 7. 11.		inari		사용되지 않는 매퍼 삭제
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

	/**
	 * 이미지 ID로 이미지를 조회합니다.
	 * @param imageId 이미지 ID
	 * @return 이미지 엔티티 (Optional)
	 */
	Optional<Image> findImageByImageId(@Param("imageId") Long imageId);

	/**
	 * 사용자 ID로 이미지 썸네일 정보를 조회합니다.
	 * @param imageSearchByUserIdDto 사용자 ID 기반 이미지 검색 조건
	 * @return 이미지 썸네일 정보 목록
	 */
	List<ImageInfoForThumbnailDto> findImageThumbnailsByUserId(ImageSearchByUserIdDto imageSearchByUserIdDto);

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
}
