
package site.petful.advertiserservice.common.ftp;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;


public class FtpUploader {
    public static String uploadFile(String host, int port, String username, String password, String baseFolder, MultipartFile file) {
        FTPClient ftpClient = new FTPClient();

        try (InputStream input = file.getInputStream()) {
            ftpClient.connect(host, port);
            
            boolean loginSuccess = ftpClient.login(username, password);
            
            if (!loginSuccess) {
                return null;
            }
            
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 파일명을 안전한 영문명으로 변경
            String originalFilename = file.getOriginalFilename();
            String safeFilename = generateSafeFilename(originalFilename);
            
            // baseFolder 끝에 /가 있는지 확인하고 경로 구성
            String remoteFilePath;
            if (baseFolder.endsWith("/")) {
                remoteFilePath = baseFolder + safeFilename;
            } else {
                remoteFilePath = baseFolder + "/" + safeFilename;
            }
            
            // 디렉토리 존재 확인 및 생성
            String[] pathParts = baseFolder.split("/");
            String currentPath = "";
            for (String part : pathParts) {
                if (!part.isEmpty()) {
                    currentPath += "/" + part;
                    if (!ftpClient.changeWorkingDirectory(currentPath)) {
                        ftpClient.makeDirectory(currentPath);
                    }
                }
            }
            
            // 업로드 디렉토리로 이동
            ftpClient.changeWorkingDirectory(baseFolder);
            
            boolean done = ftpClient.storeFile(safeFilename, input);
            
            if (!done) {
                ftpClient.logout();
                ftpClient.disconnect();
                return null;
            }
            
            ftpClient.logout();
            ftpClient.disconnect();
            return safeFilename; // 생성된 파일명 반환

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 안전한 파일명 생성 (영문, 숫자, 언더스코어만 허용)
     */
    private static String generateSafeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "file_" + System.currentTimeMillis() + ".jpg";
        }
        
        // 파일 확장자 추출
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        
        // 안전한 파일명 생성 (타임스탬프 + 랜덤 숫자)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int)(Math.random() * 1000));
        
        return "file_" + timestamp + "_" + random + extension;
    }
}