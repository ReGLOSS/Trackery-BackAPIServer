package com.trackery.trackerybackapiserver.domain.image.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.image.dto.upload.ImageUploadDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
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
	private final LocationService locationService;
	private final ImageService imageService;

	public Map<Long, String> UploadImage(ImageUploadDto	imageUploadDto) {
		CoordinateDto coordinateDto = new CoordinateDto(imageUploadDto.getLatitude(), imageUploadDto.getLongitude());

		CoordinatePoint coordinatePoint = locationService.insertCoordinatePoint(coordinateDto);

		Image image = Image.builder()
			.coordPoint(coordinatePoint)
			.imageName(imageUploadDto.getImageName())
			.build();
		return null;
	}


}
