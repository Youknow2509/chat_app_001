package com.example.chatapp.api;

public class EndPoint {

    // Chat related endpoints
    public static final String ADD_MEMBER_TO_CHAT = "/v1/chat/add-member-to-chat";
    public static final String CHANGE_ADMIN_GROUP_CHAT = "/v1/chat/change-admin-group-chat";
    public static final String CREATE_CHAT_GROUP = "/v1/chat/create-chat-group";
    public static final String CREATE_CHAT_PRIVATE = "/v1/chat/create-chat-private";
    public static final String DEL_CHAT = "/v1/chat/del-chat";
    public static final String DEL_MEMBER_FROM_CHAT = "/v1/chat/del-men-from-chat";
    public static final String GET_CHAT_INFO = "/v1/chat/get-chat-info";
    public static final String GET_LIST_CHAT_FOR_USER = "/v1/chat/get-list-chat-for-user";
    public static final String GET_LIST_CHAT_GROUP_FOR_USER = "/v1/chat/get-list-chat-group-for-user";
    public static final String GET_LIST_CHAT_PRIVATE_FOR_USER = "/v1/chat/get-list-chat-private-for-user";
    public static final String GET_USER_IN_CHAT = "/v1/chat/get-user-in-chat";
    public static final String UPGRADE_CHAT_INFO = "/v1/chat/upgrade-chat-info";

    // Microservice related endpoints
    public static final String GET_CHATS_GROUP_USER = "/v1/mservice/get-chats-group-user";
    public static final String GET_CHATS_PRIVATE_USER = "/v1/mservice/get-chats-private-user";
    public static final String GET_CHATS_USER = "/v1/mservice/get-chats-user";
    public static final String GET_DETAIL_FRIEND_REQ = "/v1/mservice/get-detail-friend-req";
    public static final String GET_USER_IN_CHAT_MICROSERVICE = "/v1/mservice/get-user-in-chat";

    // Token related endpoints
    public static final String CREATE_REFRESH_TOKEN = "/v1/token/create_refresh_token";
    public static final String CREATE_TOKEN = "/v1/token/create_token";
    public static final String VALID_TOKEN = "/v1/token/valid_token";

    // User Info related endpoints
    public static final String ACCEPT_FRIEND_REQUEST = "/v1/user/accept_friend_request";
    public static final String CREATE_FRIEND_REQUEST = "/v1/user/create_friend_request";
    public static final String DELETE_FRIEND = "/v1/user/delete_friend";
    public static final String END_FRIEND_REQUEST = "/v1/user/end_friend_request";
    public static final String FIND_USER = "/v1/user/find_user";
    public static final String FORGOT_PASSWORD = "/v1/user/forgot_password";
    public static final String GET_LIST_FRIEND_REQUEST = "/v1/user/get_list_friend_request";
    public static final String GET_USER_INFO = "/v1/user/get_user_info";
    public static final String LOGIN = "/v1/user/login";
    public static final String LOGOUT = "/v1/user/logout";
    public static final String REFRESH_TOKEN = "/v1/user/refresh_token";
    public static final String REGISTER = "/v1/user/register";
    public static final String REJECT_FRIEND_REQUEST = "/v1/user/reject_friend_request";
    public static final String UPDATE_PASSWORD = "/v1/user/update_password";
    public static final String UPDATE_USER_INFO = "/v1/user/update_user_info";
    public static final String UPDATE_USER_NAME_AND_AVATAR = "/v1/user/update_user_name_and_avatar";
    public static final String UPGRADE_PASSWORD_REGISTER = "/v1/user/upgrade_password_register";
    public static final String VERIFY_ACCOUNT = "/v1/user/verify_account";
    public static final String VERIFY_FORGOT_PASSWORD = "/v1/user/verify_forgot_password/{email}/{token}";

    // Private constructor to prevent instantiation
    private EndPoint() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
}
