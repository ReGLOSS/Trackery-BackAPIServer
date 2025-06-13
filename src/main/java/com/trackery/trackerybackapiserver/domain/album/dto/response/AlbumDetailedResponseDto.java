package com.trackery.trackerybackapiserver.domain.album.dto.response;

import java.util.List;

import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.dto.response
 * fileName       : AlbumDetailedResponseDto
 * author         : durururuk
 * date           : 25. 5. 22.
 * description    : 앨범 상세 조회 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 22.		durururuk		최초 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDetailedResponseDto {
	private Long albumId;
	private Long createdUserId;
	private String albumTitle;
	private String albumDescription;
	private Integer isPublic;
	private Integer imageCount;

	private List<ImageDto> imageList;

	public static AlbumDetailedResponseDto of(Album album, List<ImageDto> imageList) {
		return AlbumDetailedResponseDto.builder()
			.albumId(album.getAlbumId())
			.createdUserId(album.getUserId())
			.albumTitle(album.getAlbumTitle())
			.albumDescription(album.getAlbumDescription())
			.imageList(imageList)
			.isPublic(album.getIsPublic())
			.imageCount(imageList.size())
			.build();
	}
}
