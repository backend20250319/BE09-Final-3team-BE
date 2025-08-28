
package site.petful.userservice.common.ftp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FtpService {
    @Value("${ftp.host}")
    private String host;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;

    @Value("${ftp.base-folder}")
    private String baseFolder;

    @Value("${ftp.view-url}")
    private String viewUrl;

    public String upload(MultipartFile file) {
        return FtpUploader.uploadFile(host, port, username, password, baseFolder, file);
    }

    public String getFileUrl(String filename) {
        // view-url이 http://dev.macacolabs.site:8008/3/user/ 형태라면
        // 이미 base-folder 경로가 포함되어 있으므로 filename만 추가
        if (viewUrl.endsWith("/")) {
            return viewUrl + filename;
        } else {
            return viewUrl + "/" + filename;
        }
    }
}