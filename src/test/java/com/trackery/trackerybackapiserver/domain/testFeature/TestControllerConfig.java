package com.trackery.trackerybackapiserver.domain.testFeature;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import jakarta.validation.Valid;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.testController

 fileName       : TestControllerConfig
 author         : durururuk
 date           : 25. 2. 15.
 description    : 테스트 환경에서만 사용될 컨트롤러입니다.
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 15.        durururuk       최초 생성*/
@TestConfiguration
public class TestControllerConfig {

	@RestController
	@RequestMapping("/test")
	public static class Controller {

		/**
		 * ApiException 예외 발생시키는 메서드
		 */
		@GetMapping("/api-exception")
		public void throwApiException() {
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		/**
		 * dto를 받아서 ValidationException 테스트하기 위한 메서드
		 *
		 * @param dto @NotBlank가 있는 String 열이 있는 dto
		 * @return 테스트 실패 시 테스트 실패 메시지 String 반환
		 */
		@PostMapping("/validation-exception")
		public String throwValidationException(@RequestBody @Valid ValidationTestDto dto) {
			return "ValidationException 테스트 실패";
		}

		/**
		 * 잘못된 요청을 테스트해서 HttpMessageNotReadableException 테스트하기 위한 메서드
		 *
		 * @param dto ValidationDto와 같은 dto
		 * @return 테스트 실패 시 테스트 실패 메시지 반환
		 */
		@PostMapping("/http-message-not-readable-exception")
		public String throwHttpMessageNotReadableException(@RequestBody ValidationTestDto dto) {
			return "HttpMessageNotReadableException 테스트 실패";
		}
	}
}
