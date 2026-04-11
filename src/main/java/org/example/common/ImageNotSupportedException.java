package org.example.common;

import org.springframework.http.HttpStatus;

public class ImageNotSupportedException extends ApiException {
    public ImageNotSupportedException() {
        super(HttpStatus.BAD_REQUEST, "이미지 파일은 지원하지 않습니다. 텍스트 형태의 코드를 직접 입력해 주세요.");
    }
}