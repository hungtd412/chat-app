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
    INVALID_EMAIL(1013, "Email không hợp lệ!", 400),
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
    USER_NOT_EXISTED(1060, "Người dùng không tồn tại!", 404),

    //FRIEND REQUEST 107X
    CANNOT_SEND_FRIEND_REQUEST_TO_SELF(1070, "Không thể gửi lời mời kết bạn cho chính mình!", 400),
    FRIEND_REQUEST_ALREADY_SENT(1071, "Đã gửi lời mời kết bạn cho người dùng này!", 400),
    FRIEND_REQUEST_ALREADY_RECEIVED(1072, "Bạn đã nhận được lời mời kết bạn từ người dùng này!", 400),
    FRIEND_REQUEST_NOT_FOUND(1073, "Không tìm thấy lời mời kết bạn!", 404),
    FRIEND_REQUEST_ALREADY_PROCESSED(1074, "Lời mời kết bạn đã được xử lý!", 400),

    //CHAT 108X
    CONVERSATION_NOT_FOUND(1080, "Cuộc hội thoại không tồn tại!", 404),
    USER_NOT_IN_CONVERSATION(1081, "Bạn không phải là thành viên của cuộc hội thoại này!", 403),
    RECEIVER_NOT_FOUND(1082, "Không tìm thấy người nhận!", 400),
    INVALID_CONVERSATION_TYPE(1083, "Loại cuộc hội thoại không hợp lệ!", 400),
    MESSAGE_SENDING_ERROR(1084, "Có lỗi xảy ra khi gửi tin nhắn!", 500),
    EMPTY_CONVERSATION_ID(1085, "ID của cuộc hội thoại không được để trống!", 400),
    MISSING_CONTENT_FIELD(1086, "Trường content bị thiếu!", 400),
    EMPTY_MESSAGE_TYPE(1087, "Loại tin nhắn không được để trống!", 400),

    //UPLOAD FILE 109X
    UPLOAD_FILE_FAIL(1090, "Có lỗi xảy ra khi tải lên file!", 500),
    DELETE_FILE_FAIL(1091, "Có lỗi xảy ra khi xóa file!", 500),
    INVALID_FILE_TYPE(1092, "Định dạng file không hợp lệ!", 400),
    MAX_FILE_SIZE(1093, "Kích thước file tối đa là 20MB!", 400)
    ;



    int code;
    String message;
    int httpStatusCode;
}