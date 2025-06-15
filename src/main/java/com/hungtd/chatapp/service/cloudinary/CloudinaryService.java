package com.hungtd.chatapp.service.cloudinary;

import com.hungtd.chatapp.dto.response.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    CloudinaryResponse uploadFile(MultipartFile file, String folderName);
    CloudinaryResponse uploadVideo(MultipartFile file, String folderName);
    void delete(String publicId);

}
