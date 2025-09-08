package com.trackery.trackerybackapiserver.domain.image.integration;

import static org.hibernate.validator.internal.util.Contracts.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.trackery.trackerybackapiserver.domain.aws.service.SqsMessageConsumer;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;

import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.integration
 * fileName       : ImageIntegrationTest
 * author         : durururuk
 * date           : 25. 9. 7.
 * description    : 통합 테스트 기본 템플릿
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 7.      durururuk       최초 생성
 */
@Slf4j
@SpringBootTest(properties = "spring.profiles.active=dev")
class ImageIntegrationTest {
	@Autowired
	private ImageService imageService;

	@MockitoBean
	private SqsMessageConsumer sqsMessageConsumer;

	@Test
	void contextLoads() {
		List<ImageThumbnailDto> images = imageService.getImagesBySido(31L, 2L);

		assertNotNull(images);
		log.info(images.toString());
	}
}