package com.trackery.trackerybackapiserver.domain.image.service;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.mapper.LocationMapper;
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
	private final ImageMapper imageMapper;
	private final LocationMapper locationMapper;


}
