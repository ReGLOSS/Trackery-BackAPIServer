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

	//모든 앨범에서 어떤 이미지 삭제
	void deleteAllAlbumImagesByImageId(@Param("imageId") Long imageId);
}
