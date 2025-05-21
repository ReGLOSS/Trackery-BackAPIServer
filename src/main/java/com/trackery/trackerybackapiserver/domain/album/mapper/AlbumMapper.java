package com.trackery.trackerybackapiserver.domain.album.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;

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
}
