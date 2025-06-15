package com.hungtd.chatapp.util;

import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class FileUploadUtil {
    // Class constants and utility methods will be defined here
    // The @UtilityClass annotation will:
    // 1. Make the class final
    // 2. Create a private constructor to avoid us to create instance of this class
    // 3. Make all methods and fields static

    public final long MAX_FILE_SIZE = 20 * 1024 * 2014; //20MB

    public final String IMAGE_PATTERN = "(.+(\\.(?i)(jpg|png|gif|bmp))$)";

    public boolean isAllowedExtenstion(final String fileName, final String pattern) {
        final Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(fileName);

        return matcher.matches();
    }

    public void assertAllowed(MultipartFile file, String pattern) {
        final long size = file.getSize();

        if (size > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.MAX_FILE_SIZE);
        }

        final String fileName = file.getOriginalFilename();

        if (!isAllowedExtenstion(fileName, pattern)) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}
