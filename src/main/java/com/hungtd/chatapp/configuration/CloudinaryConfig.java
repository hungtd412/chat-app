package com.hungtd.chatapp.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    public static String CLOUDINARY_DEFAULT_AVATAR_URL = "https://res.cloudinary.com/da1zrkrmi/image/upload/v1749956764/default-avatar_nhpbje.jpg";

    public static String CLOUDINARY_DEFAULT_AVATAR_PUBLICID= "default-avatar_nhpbje";

    public static String CLOUDINARY_DEFAULT_GROUP_URL = "https://res.cloudinary.com/da1zrkrmi/image/upload/v1751256457/default_group_image_la4psw.png";

    public static String CLOUDINARY_DEFAULT_GROUP_PUBLICID= "default_group_image_la4psw";

    @Bean
    public Cloudinary getCloudinary(){
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

}
