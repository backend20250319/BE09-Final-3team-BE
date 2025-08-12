package site.petful.configservice.jasypt;

import java.security.SecureRandom;

public class PasswordGenerator {

    // 1. 비밀번호에 사용할 문자셋 정의
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String OTHER_CHAR = "!@#$%&*_+-=";

    // 2. 모든 문자를 하나로 합친 데이터 소스
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;

    // 3. 암호학적으로 안전한 난수 생성기
    private static SecureRandom random = new SecureRandom();

    public static String generateRandomPassword(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("Password length must be at least 4 characters");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(PASSWORD_ALLOW_BASE.length());
            char rndChar = PASSWORD_ALLOW_BASE.charAt(rndCharAt);
            sb.append(rndChar);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        int passwordLength = 12; // 12자리 비밀번호 생성
        String newPassword = generateRandomPassword(passwordLength);
        System.out.println("생성된 랜덤 비밀번호: " + newPassword);
    }
}