package com.trackery.trackerybackapiserver.domain.home.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.home.mapper.ImagesMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.home.service
 * fileName       : ImageService
 * author         : inari
 * date           : 25. 2. 14.
 * description    : 이미지 관련 비즈니스 로직을 처리하는 서비스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

	private final ImagesMapper imagesMapper;

	/**
	 * 공개된 이미지 URL 목록을 조회합니다.
	 * 결과는 하루동안 캐시됩니다.
	 * @return 공개된 이미지 URL 목록
	 */
	@Cacheable(value = "publicImageUrls")
	public List<String> getPublicImageUrls() {
		log.info("공개된 이미지의 주소들을 가져옵니다.");
		// return imagesMapper.selectPublicImageFiles();
		List<String> images = imagesMapper.selectPublicImageFiles();

		// 이미지가 없는 경우 빈 리스트 반환
		if (images == null || images.isEmpty()) {
			return new ArrayList<>();
		}

		return images;
	}

	/**
	 * 매일 0시에 이미지 URL 캐시를 갱신합니다.
	 */
	@Scheduled(cron = "0 0 0 * * *")
	@CacheEvict(value = "publicImageUrls", allEntries = true)
	public void evictImageCache() {
		log.info("공개된 이미지 주소들을 삭제합니다.");
	}
}
