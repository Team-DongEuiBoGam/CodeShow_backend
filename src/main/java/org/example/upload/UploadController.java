package org.example.upload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.service.CloudinaryUploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "upload-controller")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final CloudinaryUploadService cloudinaryUploadService;

    public UploadController(CloudinaryUploadService cloudinaryUploadService) {
        this.cloudinaryUploadService = cloudinaryUploadService;
    }

    @Operation(summary = "파일 클라우드 업로드", description = "이미지나 JSON 등의 파일을 업로드합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @Parameter(description = "업로드할 파일", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file) {

        Map<String, String> response = new HashMap<>();

        try {
            String imageUrl = cloudinaryUploadService.uploadFile(file);
            response.put("message", "파일 업로드 성공!");
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("message", "파일 업로드 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}