package com.hungtd.chatapp.service.user;

import com.hungtd.chatapp.dto.request.UserCreationRequest;
import com.hungtd.chatapp.dto.request.UserUpdateRequest;
import com.hungtd.chatapp.entity.User;
import java.util.List;

public interface UserService {

    User create(UserCreationRequest request);

    List<User> getAll();

    User currentUser();

    User get(Long id);

    User update(Long id, UserUpdateRequest request);

    void delete(Long id);
}
