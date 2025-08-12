package site.petful.configservice.jasypt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * JasyptConfig가 정상적으로 동작하는지 확인하는 통합 테스트. Spring 컨텍스트를 로드하므로 application.yml(또는 properties) 파일에
 * jasypt.encryptor.password 값이 설정되어 있어야 합니다.
 */
@SpringBootTest(properties = {
    "spring.cloud.config.server.enabled=false"
})
public class JasyptConfigTest {

    // JasyptConfig에서 @Bean으로 등록한 StringEncryptor를 주입받습니다.
    @Autowired
    private StringEncryptor jasyptStringEncryptor;

    @Test
    @DisplayName("Private Key 파일을 읽어 암호화된 ENC() 문자열 생성")
    void generateEncryptedPrivateKey() throws IOException {
        // ⚠️ 중요: 이 값은 실제 application.yml의 jasypt.encryptor.password와 반드시 같아야 합니다.
        final String SECRET_KEY = "secret_key";
        final String ALGORITHM = "PBEWithMD5AndDES";

        // 암호화할 ssh private key 준비
        ClassPathResource resource = new ClassPathResource("config_server_rsa.txt");
        String privateKey = StreamUtils.copyToString(resource.getInputStream(),
            StandardCharsets.UTF_8);

        // 암호화
        String encryptedText = jasyptStringEncryptor.encrypt(privateKey);
        String decryptedText = jasyptStringEncryptor.decrypt(encryptedText);

        // 결과
        System.out.println("privateKey Text: " + privateKey);
        System.out.printf("🔑 Encrypted Value: ENC(%s)\n", encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);
    }

    @Test
    @DisplayName("Jasypt 암호화 및 복호화 테스트")
    void jasypt_encryption_decryption_test() {
        // given
        String originalText = "이것은 나의 비밀 값입니다! 1234";

        // when
        String encryptedText = jasyptStringEncryptor.encrypt(originalText);
        String decryptedText = jasyptStringEncryptor.decrypt(encryptedText);

        // then
        System.out.println("Original Text: " + originalText);
        System.out.println("Encrypted Text: " + encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);

        // 1. 암호화된 값은 원본과 달라야 합니다.
        assertThat(encryptedText).isNotEqualTo(originalText);

        // 2. 복호화된 값은 원본과 같아야 합니다.
        assertThat(decryptedText).isEqualTo(originalText);
    }
}