package com.hungtd.chatapp.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi chưa được phân loại!", 500),

    //AUTH 900X
    INVALID_KEY(9000, "Token không hợp lệ!", 400),
    UNAUTHENTICATED(9001, "Người dùng chưa xác thực!", 401),
    UNAUTHORIZED(9002, "Bạn không có quyền truy cập vào tài nguyên này!", 403),
    SESSION_EXPIRED(9003, "Phiên đăng nhập hiện tại đã hết hạn, vui lòng đăng nhập lại!", 403),
    INVALID_TOKEN_TYPE(9004, "Loại token không hợp lệ cho thao tác này!", 401),
    EMPTY_REFRESHTOKEN(9005, "Refresh token không được để trống!", 400),
    EMPTY_ACCESSTOKEN(9006, "Access token không được để trống!", 400),

    //EMAIL 100X
    EMAIL_EXISTED(1000, "Email đã tồn tại!", 409),
    EMPTY_EMAIL(1001, "Email không được để trống!", 400),

    //USERNAME 101X
    USERNAME_EXISTED(1010, "Tài khoản đã tồn tại!", 409),
    EMPTY_USERNAME(1011, "Tài khoản không được để trống!", 400),
    MIN_USERNAME_LENGTH(1012, "Tài khoản phải có ít nhất 3 ký tự!", 400),
    MAX_USERNAME_LENGTH(1013, "Tài khoản không được vượt quá 16 ký tự!", 400),

    //FIRSTNAME 102X
    EMPTY_FIRSTNAME(1020, "Họ người dùng không được để trống!", 400),
    MIN_FIRSTNAME_LENGTH(1021, "Họ người dùng phải có ít nhất 3 ký tự!", 400),
    MAX_FIRSTNAME_LENGTH(1022, "Họ người dùng không được vượt quá 20 ký tự!", 400),

    //LASTNAME 103X
    EMPTY_LASTNAME(1030, "Tên người dùng không được để trống!", 400),
    MIN_LASTNAME_LENGTH(1031, "Tên người dùng phải có ít nhất 3 ký tự!", 400),
    MAX_LASTNAME_LENGTH(1032, "Tên người dùng không được vượt quá 20 ký tự!", 400),

    //DOB 104X
    EMPTY_DOB(1040, "Ngày sinh không được để trống!", 400),
    MIN_AGE(1041, "Người dùng phải từ 18 tuổi trở lên!", 400),

    //PASSWORD 105X
    EMPTY_PASSWORD(1050, "Mật khẩu không được để trống!", 400),
    MIN_PASSWORD_LENGTH(1051, "Mật khẩu phải có ít nhất 3 ký tự!", 400),

    //USER 106X
    USER_NOT_EXISTED(1060, "Người dùng không tồn tại!", 404)
    ;

    int code;
    String message;
    int httpStatusCode;
}
