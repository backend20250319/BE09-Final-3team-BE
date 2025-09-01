
package site.petful.advertiserservice.common.ftp;

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

    public String upload(String path, MultipartFile file) {
        return FtpUploader.uploadFile(host, port, username, password, baseFolder+path, file);
    }

    public String getFileUrl(String path, String filename) {
        if (viewUrl.endsWith("/")) {
            return viewUrl + path + filename;
        } else {
            return viewUrl + "/" + path + filename;
        }
    }
}