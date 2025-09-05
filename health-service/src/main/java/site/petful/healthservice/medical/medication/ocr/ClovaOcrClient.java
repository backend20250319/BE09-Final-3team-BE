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

	private final RestTemplate restTemplate;

	public ClovaOcrClient() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(5000);
		factory.setReadTimeout(10000);
		this.restTemplate = new RestTemplate(factory);
	}

	public String extractTextFromImage(File imageFile) throws IOException {
		// 설정이 없으면 Mock 응답 반환
		if (invokeUrl == null || invokeUrl.trim().isEmpty() ||
			secretKey == null || secretKey.trim().isEmpty() ||
			templateId == null || templateId.trim().isEmpty()) {
			log.info("=== MOCK OCR 응답 반환 (설정값 부족) ===");
			return createMockOcrResponse();
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
	
	private String createMockOcrResponse() {
		// 실제 Clova OCR 응답과 동일한 구조의 Mock 데이터
		return "{\"uid\":\"1acdd85e0dd6497282051103ce54dbda\",\"name\":\"name\",\"inferResult\":\"SUCCESS\",\"message\":\"SUCCESS\",\"matchedTemplate\":{\"id\":38632,\"name\":\"처방전4\"},\"validationResult\":{\"result\":\"NO_REQUESTED\"},\"fields\":[{\"name\":\"1번 성분명\",\"valueType\":\"ALL\",\"inferText\":\"Amoxicillin (항생제)\",\"inferConfidence\":0.98230004},{\"name\":\"1번 용량\",\"valueType\":\"ALL\",\"inferText\":\"50mg\",\"inferConfidence\":0.9996},{\"name\":\"1번 용법\",\"valueType\":\"ALL\",\"inferText\":\"경구루 여, 하루 2회\",\"inferConfidence\":0.9957},{\"name\":\"1번 처방일수\",\"valueType\":\"ALL\",\"inferText\":\"3일\",\"inferConfidence\":0.9993},{\"name\":\"2번 성분명\",\"valueType\":\"ALL\",\"inferText\":\"Firocoxib (소염진통제)\",\"inferConfidence\":0.9979},{\"name\":\"2번 용량\",\"valueType\":\"ALL\",\"inferText\":\"57mg\",\"inferConfidence\":0.9996},{\"name\":\"2번 용법\",\"valueType\":\"ALL\",\"inferText\":\"경구두 여, 하루 1회\",\"inferConfidence\":0.9855333},{\"name\":\"2번 처방일수\",\"valueType\":\"ALL\",\"inferText\":\"7일\",\"inferConfidence\":1.0}]}";
	}
}
