package com.example.chatapp.api;

public class EndPoint {

    // Chat related endpoints
    public static final String ADD_MEMBER_TO_CHAT = "/api/v1/chat/add-member-to-chat";
    public static final String CHANGE_ADMIN_GROUP_CHAT = "/api/v1/chat/change-admin-group-chat";
    public static final String CREATE_CHAT_GROUP = "/api/v1/chat/create-chat-group";
    public static final String CREATE_CHAT_PRIVATE = "/api/v1/chat/create-chat-private";
    public static final String DEL_CHAT = "/api/v1/chat/del-chat";
    public static final String DEL_MEMBER_FROM_CHAT = "/api/v1/chat/del-men-from-chat";
    public static final String GET_CHAT_INFO = "/api/v1/chat/get-chat-info";
    public static final String GET_LIST_CHAT_FOR_USER = "/api/v1/chat/get-list-chat-for-user";
    public static final String GET_LIST_CHAT_GROUP_FOR_USER = "/api/v1/chat/get-list-chat-group-for-user";
    public static final String GET_LIST_CHAT_PRIVATE_FOR_USER = "/api/v1/chat/get-list-chat-private-for-user";
    public static final String GET_USER_IN_CHAT = "/api/v1/chat/get-user-in-chat";
    public static final String UPGRADE_CHAT_INFO = "/api/v1/chat/upgrade-chat-info";

    // Microservice related endpoints
    public static final String GET_CHATS_GROUP_USER = "/api/v1/mservice/get-chats-group-user";
    public static final String GET_CHATS_PRIVATE_USER = "/api/v1/mservice/get-chats-private-user";
    public static final String GET_CHATS_USER = "/api/v1/mservice/get-chats-user";
    public static final String GET_DETAIL_FRIEND_REQ = "/api/v1/mservice/get-detail-friend-req";
    public static final String GET_USER_IN_CHAT_MICROSERVICE = "/api/v1/mservice/get-user-in-chat";

    // Token related endpoints
    public static final String CREATE_REFRESH_TOKEN = "/api/v1/token/create_refresh_token";
    public static final String CREATE_TOKEN = "/api/v1/token/create_token";
    public static final String VALID_TOKEN = "/api/v1/token/valid_token";

    // User Info related endpoints
    public static final String ACCEPT_FRIEND_REQUEST = "/api/v1/user/accept_friend_request";
    public static final String CREATE_FRIEND_REQUEST = "/api/v1/user/create_friend_request";
    public static final String DELETE_FRIEND = "/api/v1/user/delete_friend";
    public static final String END_FRIEND_REQUEST = "/api/v1/user/end_friend_request";
    public static final String FIND_USER = "/api/v1/user/find_user";
    public static final String FORGOT_PASSWORD = "/api/v1/user/forgot_password";
    public static final String GET_LIST_FRIEND_REQUEST = "/api/v1/user/get_list_friend_request";
    public static final String GET_USER_INFO = "/api/v1/user/get_user_info";
    public static final String LOGIN = "/api/v1/user/login";
    public static final String LOGOUT = "/api/v1/user/logout";
    public static final String REFRESH_TOKEN = "/api/v1/user/refresh_token";
    public static final String REGISTER = "/api/v1/user/register";
    public static final String REJECT_FRIEND_REQUEST = "/api/v1/user/reject_friend_request";
    public static final String UPDATE_PASSWORD = "/api/v1/user/update_password";
    public static final String UPDATE_USER_INFO = "/api/v1/user/update_user_info";
    public static final String UPDATE_USER_NAME_AND_AVATAR = "/api/v1/user/update_user_name_and_avatar";
    public static final String UPGRADE_PASSWORD_REGISTER = "/api/v1/user/upgrade_password_register";
    public static final String VERIFY_ACCOUNT = "/api/v1/user/verify_account";
    public static final String VERIFY_FORGOT_PASSWORD = "/api/v1/user/verify_forgot_password/{email}/{token}";
    
    public static final String FB_TOKEN = "/api/v1/token";
    // Private constructor to prevent instantiation
    private EndPoint() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
}
