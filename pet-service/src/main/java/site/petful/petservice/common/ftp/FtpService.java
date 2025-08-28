package site.petful.petservice.common.ftp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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

    public List<String> uploadMultiple(List<MultipartFile> files) {
        List<String> filenames = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = upload(file);
            if (filename != null) {
                filenames.add(filename);
            }
        }
        return filenames;
    }

    public String getFileUrl(String filename) {
        // view-url이 http://dev.macacolabs.site:8008/3/pet/ 형태라면
        // 이미 base-folder 경로가 포함되어 있으므로 filename만 추가
        if (viewUrl.endsWith("/")) {
            return viewUrl + filename;
        } else {
            return viewUrl + "/" + filename;
        }
    }

    public List<String> getFileUrls(List<String> filenames) {
        List<String> urls = new ArrayList<>();
        for (String filename : filenames) {
            urls.add(getFileUrl(filename));
        }
        return urls;
    }
}
