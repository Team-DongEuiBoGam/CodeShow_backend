package org.example.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryUploadService {

    private final Cloudinary cloudinary;

    // 생성자 주입을 통해 @Value 값을 받아 Cloudinary 객체를 완벽하게 초기화합니다!
    public CloudinaryUploadService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadFile(MultipartFile file) throws IOException {
        Map<String, Object> params = ObjectUtils.asMap(
                "resource_type", "auto" // 이미지뿐만 아니라 다른 파일 형식도 자동으로 맞춰줍니다.
        );

        // 클라우디너리 서버로 파일 전송
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        // 업로드 성공 후 안전한 HTTPS URL을 반환
        return uploadResult.get("secure_url").toString();
    }
}