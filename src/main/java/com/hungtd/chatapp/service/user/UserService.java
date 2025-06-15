package com.hungtd.chatapp.service.user;

import com.hungtd.chatapp.dto.request.UpdateEmailRequest;
import com.hungtd.chatapp.dto.request.UpdatePasswordRequest;
import com.hungtd.chatapp.dto.request.UserCreationRequest;
import com.hungtd.chatapp.dto.request.UpdateNameDobRequest;
import com.hungtd.chatapp.dto.response.UploadImageResponse;
import com.hungtd.chatapp.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    User create(UserCreationRequest request);

    List<User> getAll();

    User currentUser();

    User get(Long id);


    void delete(Long id);

    boolean isExistById(Long userId);

    UploadImageResponse updateAvatar(MultipartFile image);

    User updateNameDob(UpdateNameDobRequest request);

    User updatePassword(UpdatePasswordRequest request);

    User updateEmail(UpdateEmailRequest request);

    void restoreDefaultAvatar();
}
