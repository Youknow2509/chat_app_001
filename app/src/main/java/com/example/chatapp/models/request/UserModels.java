package com.example.chatapp.models.request;

public class UserModels {

    public static class UpdateUserInfoInput {
        private String user_id;
        private String user_nickname;
        private String user_avatar;

        public UpdateUserInfoInput(String user_id, String user_nickname, String user_avatar) {
            this.user_id = user_id;
            this.user_nickname = user_nickname;
            this.user_avatar = user_avatar;
        }
    }

    public static class UserChangePasswordInput {
        private String user_id;
        private String old_password;
        private String new_password;

        public UserChangePasswordInput(String user_id, String old_password, String new_password) {
            this.user_id = user_id;
            this.old_password = old_password;
            this.new_password = new_password;
        }
    }

    public static class CreateFriendRequestInput {
        private String user_id;
        private String email_friend;

        public CreateFriendRequestInput(String user_id, String email_friend) {
            this.user_id = user_id;
            this.email_friend = email_friend;
        }
    }

    public static class AcceptFriendRequestInput {
        private String request_id;
        private String user_accept_id;

        public AcceptFriendRequestInput(String request_id, String user_accept_id) {
            this.request_id = request_id;
            this.user_accept_id = user_accept_id;
        }
    }

    public static class RejectFriendRequestInput {
        private String request_id;
        private String user_accept_id;

        public RejectFriendRequestInput(String request_id, String user_accept_id) {
            this.request_id = request_id;
            this.user_accept_id = user_accept_id;
        }
    }

    public static class EndFriendRequestInput {
        private String request_id;
        private String user_id;

        public EndFriendRequestInput(String request_id, String user_id) {
            this.request_id = request_id;
            this.user_id = user_id;
        }
    }

    public static class DeleteFriendInput {
        private String user_id;
        private String friend_email;

        public DeleteFriendInput(String user_id, String friend_email) {
            this.user_id = user_id;
            this.friend_email = friend_email;
        }
    }
}