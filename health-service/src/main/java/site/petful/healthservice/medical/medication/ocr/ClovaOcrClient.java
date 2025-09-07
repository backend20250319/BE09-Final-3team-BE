package site.petful.healthservice.medical.medication.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import jakarta.annotation.PostConstruct;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;

import java.io.File;
import java.util.HashMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ClovaOcrClient {

	@Value("${clova.ocr.invoke-url}")
	private String invokeUrl;

	@Value("${clova.ocr.secret-key}")
	private String secretKey;

	@Value("${clova.ocr.template-id}")
	private String templateId;

	@Value("${clova.ocr.connect-timeout:5000}")
	private int connectTimeout;

	@Value("${clova.ocr.read-timeout:10000}")
	private int readTimeout;

	private RestTemplate restTemplate;

	@PostConstruct
	public void init() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeout);
		factory.setReadTimeout(readTimeout);
		this.restTemplate = new RestTemplate(factory);
	}

	public String extractTextFromImage(File imageFile) throws IOException {
		// OCR API 설정 검증
		if (invokeUrl == null || invokeUrl.trim().isEmpty() ||
			secretKey == null || secretKey.trim().isEmpty() ||
			templateId == null || templateId.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.OCR_PROCESSING_FAILED,
				"OCR API 설정이 올바르지 않습니다. 설정을 확인해주세요.");
		}
		
		// 실제 OCR API 호출
		log.info("=== 실제 Clova OCR API 호출 ===");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.set("X-OCR-SECRET", secretKey);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new FileSystemResource(imageFile));

		// Build OCR message JSON
		Map<String, Object> requestJsonMap = new HashMap<>();
		requestJsonMap.put("version", "V2");
		requestJsonMap.put("requestId", UUID.randomUUID().toString());
		requestJsonMap.put("timestamp", System.currentTimeMillis());
		requestJsonMap.put("lang", "ko");
		// format은 파일 확장자 기준으로 설정
		String name = imageFile.getName();
		String format = "jpg";
		int dot = name.lastIndexOf('.');
		if (dot > -1 && dot < name.length() - 1) {
			format = name.substring(dot + 1).toLowerCase();
		}
		requestJsonMap.put("images", List.of(Map.of("format", format, "name", name)));
		// templateId 사용
		if (templateId != null && !templateId.isBlank()) {
			requestJsonMap.put("templateIds", List.of(templateId));
		}
		String requestJson = new ObjectMapper().writeValueAsString(requestJsonMap);

		// Add message part explicitly as application/json per gateway spec
		HttpHeaders jsonPartHeaders = new HttpHeaders();
		jsonPartHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> messagePart = new HttpEntity<>(requestJson, jsonPartHeaders);
		body.add("message", messagePart);

		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(invokeUrl, request, String.class);
			return response.getBody();
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			String detail = "HTTP " + e.getStatusCode() + " body=" + e.getResponseBodyAsString();
			throw new IOException(detail, e);
		} catch (ResourceAccessException e) {
			String detail = "resource access error (timeout/DNS): " + e.getMessage();
			throw new IOException(detail, e);
		}
	}
	
}
