package com.trackery.trackerybackapiserver.domain.docs.controller;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.docs.controller
 * fileName       : DocsController
 * author         : Nari-Lee
 * date           : 25. 6. 12.
 * description    : Spring REST Docs 문서를 제공하는 컨트롤러입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 12.		Nari-Lee		최초 생성
 * 25. 6. 12.		Nari-Lee		핸들러에서 컨트롤러로 변경
 * 25. 6. 16.		Nari-Lee		체크스타일 적용
 */
@RestController
@RequestMapping("/api/docs")
public class DocsController {

	/**
	 * API 문서를 제공하는 REST 컨트롤러
	 * 클래스패스의 REST DOCS의 정적 HTML 파일을 읽어서 프론트엔드로 전달
	 */
	@GetMapping({"/index.html", ""})
	public ResponseEntity<String> getDocs() {
		try {
			Resource resource = new ClassPathResource("static/docs/index.html");
			if (resource.exists()) {
				String content = Files.readString(resource.getFile().toPath());
				return ResponseEntity.ok()
					.contentType(MediaType.TEXT_HTML)
					.body(content);
			}
			throw new ApiException(ErrorCode.NOT_FOUND);
		} catch (IOException e) {
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
}
