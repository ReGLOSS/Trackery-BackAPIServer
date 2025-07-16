package com.trackery.trackerybackapiserver.domain.album.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.entity.Album;
import com.trackery.trackerybackapiserver.domain.album.entity.AlbumImage;
import com.trackery.trackerybackapiserver.domain.album.enums.AlbumImageEditOperation;
import com.trackery.trackerybackapiserver.domain.album.mapper.AlbumMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.service
 * fileName       : AlbumThumbnailService
 * author         : durururuk
 * date           : 25. 6. 20.
 * description    : 앨범 썸네일 관련 기능을 담당하는 서비스 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 20.		durururuk		최초 생성
 * 25. 6. 20.		durururuk		AlbumThumbnailService 생성
 * 25. 6. 20.		durururuk		앨범 썸네일 자동 생성 로직 작성
 * 25. 6. 20.		durururuk		이벤트 관련 클래스 이벤트 패키지로 이동
 * 25. 6. 20.		durururuk		이벤트를 발행할 때 앨범 정보도 같이 넘겨줘서 Album 두 번 조회하지 않게 수정
 * 25. 6. 20.		durururuk		주석 수정
 * 25. 6. 23.		durururuk		이미지 추가에 성공한 이미지가 없을 경우 예외가 아닌 썸네일 관련 로직이 작동하지 않게 수정, 현재 썸네일이 없을 경우 이미지 삭제 시 썸네일 작동하지 않게 수정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumThumbnailService {
	private final AlbumMapper albumMapper;

	/**
	 * 앨범에 이미지를 추가하거나 삭제할 때 썸네일을 설정할 수 있도록 핸들링해주는 메서드입니다.
	 * @param dto 이미지 추가, 삭제 후 반환받은 DTO
	 * @param operation 이미지 추가, 삭제를 나타내는 동작
	 */
	public void handleAlbumThumbnailChange(Album album, AlbumImageEditResponseDto dto,
		AlbumImageEditOperation operation) {
		switch (operation) {
			case ADD:
				setAlbumThumbnailForAddImagesIntoAlbum(album, dto);
				break;
			case DELETE:
				changeAlbumThumbnailForDeleteImagesFromAlbum(album, dto);
				break;
			default:
				throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 앨범에 썸네일을 설정하는 메서드입니다.
	 * 앨범에 이미지를 추가할 때 이미 앨범의 썸네일이 존재하지 않는다면 추가된 이미지의 첫번째 이미지를 자동으로 썸네일로 설정합니다.
	 * @param album 앨범 엔티티
	 * @param dto 앨범 이미지 추가 후 반환받은 dto
	 */
	public void setAlbumThumbnailForAddImagesIntoAlbum(Album album, AlbumImageEditResponseDto dto) {
		if (album.getThumbnailImageId() != null || dto.getSucceededImageIds().isEmpty()) {
			return;
		}

		Long firstRegisteredImageId = dto.getSucceededImageIds().iterator().next();

		albumMapper.setThumbnail(album.getAlbumId(), firstRegisteredImageId);
	}

	/**
	 * 앨범에서 썸네일인 이미지가 삭제됐을 경우 다른 이미지로 교체하거나 모든 이미지를 삭제했을 때 썸네일을 null로 설정합니다.
	 * @param album 앨범 엔티티
	 * @param dto 이미지 삭제 후 반환받은 DTO
	 */
	public void changeAlbumThumbnailForDeleteImagesFromAlbum(Album album, AlbumImageEditResponseDto dto) {
		if (album.getThumbnailImageId() == null || !dto.getSucceededImageIds().contains(album.getThumbnailImageId())) {
			return;
		}

		List<AlbumImage> albumImageList = albumMapper.findAlbumImagesByAlbumId(album.getAlbumId());

		Long newThumbnailImageId = albumImageList.stream().findFirst().map(AlbumImage::getImageId).orElse(null);

		albumMapper.setThumbnail(album.getAlbumId(), newThumbnailImageId);
	}
}
