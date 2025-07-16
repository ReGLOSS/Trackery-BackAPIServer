package com.trackery.trackerybackapiserver.domain.image.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.upload.ImageUploadDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.tag.service.TagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.service
 * fileName       : ImageUploadService
 * author         : durururuk
 * date           : 25. 4. 17.
 * description    : 이미지 업로드 관련 기능을 하는 서비스 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 17.		durururuk		최초 생성
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 4. 21.		durururuk		이미지 업로드 DTO 작성
 * 25. 4. 21.		durururuk		이미지 업로드 기능 구현
 * 25. 4. 21.		durururuk		더 이상 사용되지 않는 클래스 삭제
 * 25. 4. 21.		durururuk		개발 중 테스트용 코드 남아있던 것 삭제
 * 25. 4. 23.		durururuk		LocationService JavaDoc 주석 추가
 * 25. 4. 23.		durururuk		ImageUploadService JavaDoc 주석 작성
 * 25. 4. 23.		durururuk		ImageUploadService Mock 단위 테스트 작성
 * 25. 4. 25.		durururuk		이미지 메타데이터 저장 후 이미지를 s3 임시 폴더에서 이미지 폴더로 이동시키는 작업 추가
 * 25. 7. 1.		durururuk		key 관리를 편하게 하기 위해서 imageName에서 확장자 제거
 * 25. 7. 8.		Nari-Lee		이미지 업로드시 태그 추가
 * 25. 7. 8.		Nari-Lee		이미지 업로드시 일반 태그 추가
 * 25. 7. 9.		Nari-Lee		regionalTags를 Tags로 수정 및 관련 메서드 및 변수 변경
 * 25. 7. 11.		Nari-Lee		image 도메인의 자바독 누락 및 체크스타일 해결
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {
	private final ImageMapper imageMapper;
	private final LocationService locationService;
	private final ImageS3Service imageS3Service;
	private final TagService tagService;

	/**
	 * S3에 Object Put Presigned URL을 요청하는 메서드입니다.
	 * @param imageFileName 이미지 파일명
	 * @param userId 사용자 ID
	 * @return S3 PresignedPutUrl
	 */
	public String getPresignedPutUrl(String imageFileName, Long userId) {
		isImage(imageFileName);

		return imageS3Service.generatePreSignedPutUrl(imageFileName, userId);
	}

	/**
	 * 파일명이 이미지 확장자인지 검증하는 메서드입니다.
	 * @param imageFileName 이미지 파일명
	 */
	private void isImage(String imageFileName) {
		List<String> imageExtensions = List.of("jpg", "jpeg", "png", "webp");

		String extension = imageFileName.substring(imageFileName.lastIndexOf(".") + 1).toLowerCase();

		if (!imageExtensions.contains(extension)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_IMAGE_FILE);
		}
	}

	/**
	 * yyyy / M /d 형식으로 들어오는 사진 촬영일을 LocalDateTime으로 변환합니다. 시간은 00시 00분으로 세팅됩니다.
	 * 촬영일이 기록되지 않아 유저가 수동으로 날짜를 기입한 이미지 파일 때문에 이렇게 설정해놨지만 추후 회의를 통해 수정될 수 있습니다.
	 * @param dateString yyyy / M / d 형식의 촬영일 String
	 * @return : LocalDateTime 타입의 촬영일
	 */
	private LocalDateTime parseDateString(String dateString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy / M / d");

		LocalDate date = LocalDate.parse(dateString, formatter);

		return date.atStartOfDay();
	}

	/**
	 * boolean으로 들어오는 이미지 공개 여부를 int 타입으로 변환합니다.
	 * @param isPublic boolean 타입의 이미지 공개 여부
	 * @return int 타입의 이미지 공개 여부
	 */
	private int convertIsPublicToInt(boolean isPublic) {
		if (isPublic) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * 파일명에서 확장자를 제거한 이름을 반환합니다.
	 * @param imageFileName 이미지 파일명
	 * @return 확장자를 제거한 파일명
	 */
	private String getFileNameWithoutExtension(String imageFileName) {
		int lastDotIndex = imageFileName.lastIndexOf(".");
		if (lastDotIndex == -1) {
			return imageFileName;
		}
		return imageFileName.substring(0, lastDotIndex);
	}

	/**
	 * 유저ID, 이미지 업로드 DTO에서 사진 메타데이터를 DB에 삽입하는 메서드입니다.
	 * @param userId 사진을 업로드한 유저 ID
	 * @param imageUploadDto 이미지 메타데이터를 담고 있는 DTO
	 * @return DB에서 자동으로 할당된 ID를 포함하는 이미지 엔티티 객체
	 */
	@Transactional
	public Image saveImage(Long userId, ImageUploadDto imageUploadDto) {
		CoordinateDto coordinateDto = new CoordinateDto(imageUploadDto.getLatitude(), imageUploadDto.getLongitude());
		CoordinatePoint coordinatePoint = locationService.insertCoordinatePoint(coordinateDto);
		int isPublic = convertIsPublicToInt(imageUploadDto.isPublic());
		LocalDateTime dateTime = parseDateString(imageUploadDto.getDateString());

		Image image = Image.builder()
			.coordPoint(coordinatePoint)
			.imageName(getFileNameWithoutExtension(imageUploadDto.getImageName()))
			.imageFile(imageUploadDto.getImageName())
			.isPublic(isPublic)
			.isDeleted(false)
			.imageType(imageUploadDto.getImageType())
			.imageContent(imageUploadDto.getDescription())
			.imageDate(dateTime)
			.imageRegDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
			.userId(userId)
			.build();

		imageMapper.insertImage(image);

		if (imageUploadDto.getTags() != null && !imageUploadDto.getTags().isEmpty()) {
			tagService.attachRegionalTagsToImage(image.getImageId(), imageUploadDto.getTags());
		}

		return image;
	}
}
