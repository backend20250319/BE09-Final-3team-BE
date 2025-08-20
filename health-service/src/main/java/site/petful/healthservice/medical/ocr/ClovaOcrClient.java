package site.petful.healthservice.medical.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ClovaOcrClient {

	@Value("${clova.ocr.invoke-url}")
	private String invokeUrl;

	@Value("${clova.ocr.secret-key}")
	private String secretKey;

	@Value("${clova.ocr.template-id}")
	private String templateId;

	private final RestTemplate restTemplate;

	public ClovaOcrClient() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(5000);
		factory.setReadTimeout(10000);
		this.restTemplate = new RestTemplate(factory);
	}

	public String extractTextFromImage(File imageFile) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.set("X-OCR-SECRET", secretKey);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new FileSystemResource(imageFile));

		// Build OCR message JSON
		Map<String, Object> requestJsonMap = new java.util.HashMap<>();
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

		// OCR 요청 JSON 로그 추가
		System.out.println("=== OCR REQUEST JSON ===");
		System.out.println("templateIds: [" + templateId + "]");
		System.out.println("full request: " + requestJson);
		System.out.println("=========================");

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
