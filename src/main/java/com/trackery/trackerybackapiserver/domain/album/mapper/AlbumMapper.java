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
	void insertAlbum(Album album);

	void insertAlbumImage(AlbumImage albumImage);

	Optional<Album> findByAlbumId(Long albumId);
}
