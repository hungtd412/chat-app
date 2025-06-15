package com.hungtd.chatapp.service.cloudinary.impl;

import com.hungtd.chatapp.dto.response.CloudinaryResponse;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.service.cloudinary.CloudinaryService;

import com.cloudinary.Cloudinary;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    Cloudinary cloudinary;

    @Override
    @Transactional
    public CloudinaryResponse uploadFile(MultipartFile file, String folderName) {
        try {
            final Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder",
                            "chat-app/" + folderName
                    )
            );

            final String url = (String)uploadResult.get("secure_url");
            final String publicId = (String)uploadResult.get("public_id");
            return CloudinaryResponse.builder()
                    .url(url)
                    .publicId(publicId)
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FILE_FAIL);
        }
    }

    @Override
    @Transactional
    public CloudinaryResponse uploadVideo(MultipartFile file, String folderName) {
        // ...existing code...
        return CloudinaryResponse.builder()
                .build();
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Map.of());
        } catch (IOException e) {
            throw new AppException(ErrorCode.DELETE_FILE_FAIL);
        }
    }
}