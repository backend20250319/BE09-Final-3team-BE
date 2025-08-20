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

	private final RestTemplate restTemplate = new RestTemplate();

	public String extractTextFromImage(File imageFile) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.set("X-OCR-SECRET", secretKey);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new FileSystemResource(imageFile));

		Map<String, Object> requestJson = Map.of(
			"version", "V2",
			"requestId", UUID.randomUUID().toString(),
			"timestamp", System.currentTimeMillis(),
			"lang", "ko",
			"images", List.of(Map.of("format", "jpg", "name", imageFile.getName())),
			"templateIds", List.of(templateId)
		);

		body.add("message", new ObjectMapper().writeValueAsString(requestJson));

		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(invokeUrl, request, String.class);
		return response.getBody();
	}
}
