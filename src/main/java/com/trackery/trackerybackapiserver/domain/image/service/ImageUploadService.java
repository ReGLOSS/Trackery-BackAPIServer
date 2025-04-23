package com.trackery.trackerybackapiserver.domain.image.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.aws.service.S3Service;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.upload.ImageUploadDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.service
 * fileName       : ImageUploadService
 * author         : durururuk
 * date           : 25. 4. 17.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 17.		durururuk		최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {
	private final ImageMapper imageMapper;
	private final LocationService locationService;
	private final S3Service s3Service;

	public String getPresignedPutUrl(String imageFileName) {
		isImage(imageFileName);

		return s3Service.generatePreSignedPutUrl(imageFileName);
	}

	private void isImage(String imageFileName) {
		List<String> imageExtensions = List.of("jpg", "jpeg", "png", "webp");

		String extension = imageFileName.substring(imageFileName.lastIndexOf(".") + 1).toLowerCase();

		if (!imageExtensions.contains(extension)) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_IMAGE_FILE);
		}
	}

	private LocalDateTime parseDateString(String dateString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy / M / d");

		LocalDate date = LocalDate.parse(dateString, formatter);

		return date.atStartOfDay();
	}

	private int convertIsPublicToInt(boolean isPublic) {
		if (isPublic) {
			return 1;
		} else {
			return 0;
		}
	}

	@Transactional
	public void saveImage(Long userId, ImageUploadDto imageUploadDto) {
		CoordinateDto coordinateDto = new CoordinateDto(imageUploadDto.getLatitude(), imageUploadDto.getLongitude());
		CoordinatePoint coordinatePoint = locationService.insertCoordinatePoint(coordinateDto);
		int isPublic = convertIsPublicToInt(imageUploadDto.isPublic());
		LocalDateTime dateTime = parseDateString(imageUploadDto.getDateString());

		Image image = Image.builder()
			.coordPoint(coordinatePoint)
			.imageName(imageUploadDto.getImageName())
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
	}

}
