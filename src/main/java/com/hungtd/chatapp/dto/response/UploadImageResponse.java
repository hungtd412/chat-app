package com.hungtd.chatapp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadImageResponse {
    String imageUrl;
    String cloudinaryId;
}
