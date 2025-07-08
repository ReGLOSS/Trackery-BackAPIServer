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

	List<AlbumImage> findAlbumImagesByAlbumId(Long albumId);

	void updateAlbumInfo(@Param("albumId") Long albumId, @Param("dto") AlbumUpdateRequestDto albumUpdateRequestDto);

	List<Album> findAlbumsByUserId(Long userId);

	Optional<AlbumImage> findAlbumImageByAlbumIdAndImageId(Long albumId, Long imageId);

	void deleteAlbumImageByAlbumImageId(Long albumImageId);

	void deleteAlbumByAlbumId(Long albumId);

	void setThumbnail(@Param("albumId") Long albumId, @Param("imageId") Long imageId);

	/**
	 * 앨범의 이미지들을 썸네일 생성용으로 조회합니다.
	 * @param albumId 앨범 ID
	 * @return 썸네일 생성에 필요한 이미지 기본 정보
	 */
	List<ImageInfoForThumbnailDto> findImagesForThumbnailByAlbumId(@Param("albumId") Long albumId);

	List<ImageInfoForThumbnailDto> findMyImagesThumbnailWithoutAlbumImages(@Param("albumId") Long albumId,
		@Param("userId") Long userId);
}
