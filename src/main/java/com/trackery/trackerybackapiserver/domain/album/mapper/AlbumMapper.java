package com.trackery.trackerybackapiserver.domain.album.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageInfoForThumbnailDto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.mapper
 * fileName       : AlbumMapper
 * author         : durururuk
 * date           : 25. 5. 14.
 * description    : 앨범 관련 매퍼
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 14.		durururuk		최초 생성
 * 25. 5. 14.		durururuk		앨범 기능 기본 entity, mapper 생성
 * 25. 5. 15.		durururuk		이미지 조회 기능 구현
 * 25. 5. 15.		durururuk		서식 수정
 * 25. 5. 20.		durururuk		앨범 생성 기능 작성
 * 25. 5. 21.		durururuk		앨범에 이미지 추가하는 기능 작성
 * 25. 5. 21.		durururuk		JavaDoc 작성
 * 25. 5. 22.		durururuk		앨범 조회 기능 구현
 * 25. 5. 22.		durururuk		앨범 정보 수정 기능 구현
 * 25. 5. 29.		durururuk		내 앨범 간단 조회 기능 구현
 * 25. 6. 11.		durururuk		앨범 이미지 삭제 기능 구현
 * 25. 6. 12.		durururuk		JavaDoc 주석 작성
 * 25. 6. 16.		durururuk		파라미터가 아닌 url에 앨범 ID를 포함해서 요청할 수 있도록 수정
 * 25. 6. 20.		durururuk		앨범 썸네일 자동 생성 로직 작성
 * 25. 7. 7.		durururuk		쿼리가 산발적으로 돼있어서 페이지네이션 정보가 실제 값과 일치하지 않던 문제 수정
 * 25. 7. 8.		durururuk		내 이미지 조회 시 조회 결과에서 제외될 앨범 ID 파라미터 추가
 * 25. 7. 8.		durururuk		개발 도중 흔적 제거
 * 25. 7. 8.		durururuk		사용되지 않는 메서드 정리
 * 25. 7. 11.		Nari-Lee		album 도메인의 자바독 누락 및 체크스타일 해결
 * 25. 7. 15.		durururuk		앨범_이미지 테이블에서 이미지 삭제 메서드 추가
 * 25. 7. 15.		durururuk		앨범 이미지 삭제 전 영향을 받는 앨범 ID 조회 쿼리 추가
 */
@Mapper
public interface AlbumMapper {
	/**
	 * 앨범 테이블 인서트
	 * @param album 앨범 엔티티
	 */
	void insertAlbum(Album album);

	/**
	 * 앨범_이미지 테이블 인서트
	 * @param albumImage 앨범 이미지 엔티티
	 */
	void insertAlbumImage(AlbumImage albumImage);

	/**
	 * 앨범 ID로 앨범 조회
	 * @param albumId 앨범 ID
	 * @return 앨범 엔티티
	 */
	Optional<Album> findByAlbumId(Long albumId);

	/**
	 * 앨범 ID로 앨범 이미지 목록 조회
	 * @param albumId 앨범 ID
	 * @return 앨범 이미지 목록
	 */
	List<AlbumImage> findAlbumImagesByAlbumId(Long albumId);

	/**
	 * 앨범 정보 업데이트
	 * @param albumId 앨범 ID
	 * @param albumUpdateRequestDto 앨범 업데이트 정보
	 */
	void updateAlbumInfo(@Param("albumId") Long albumId, @Param("dto") AlbumUpdateRequestDto albumUpdateRequestDto);

	/**
	 * 사용자 ID로 앨범 목록 조회
	 * @param userId 사용자 ID
	 * @return 앨범 목록
	 */
	List<Album> findAlbumsByUserId(Long userId);

	/**
	 * 앨범 ID와 이미지 ID로 앨범 이미지 조회
	 * @param albumId 앨범 ID
	 * @param imageId 이미지 ID
	 * @return 앨범 이미지 엔티티
	 */
	Optional<AlbumImage> findAlbumImageByAlbumIdAndImageId(Long albumId, Long imageId);

	/**
	 * 앨범 이미지 ID로 앨범 이미지 삭제
	 * @param albumImageId 앨범 이미지 ID
	 */
	void deleteAlbumImageByAlbumImageId(Long albumImageId);

	/**
	 * 앨범 ID로 앨범 삭제
	 * @param albumId 앨범 ID
	 */
	void deleteAlbumByAlbumId(Long albumId);

	/**
	 * 앨범 썸네일 설정
	 * @param albumId 앨범 ID
	 * @param imageId 이미지 ID
	 */
	void setThumbnail(@Param("albumId") Long albumId, @Param("imageId") Long imageId);

	/**
	 * 앨범의 이미지들을 썸네일 생성용으로 조회합니다.
	 * @param albumId 앨범 ID
	 * @return 썸네일 생성에 필요한 이미지 기본 정보
	 */
	List<ImageInfoForThumbnailDto> findImagesForThumbnailByAlbumId(@Param("albumId") Long albumId);

	/**
	 * 이미지 ID로 해당 이미지가 포함된 앨범 ID 목록 조회
	 * @param imageId 이미지 ID
	 * @return 앨범 ID 목록
	 */
	List<Long> findAlbumIdsByImageId(@Param("imageId") Long imageId);

	//모든 앨범에서 어떤 이미지 삭제
	void deleteAllAlbumImagesByImageId(@Param("imageId") Long imageId);
}
