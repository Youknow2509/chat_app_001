package com.example.chatapp.models.request;

public class AccountModels {

    public static class UpgradeNameAndAvatarRegisterInput {
        private String mail;
        private String token;
        private String url_avatar;
        private String user_name;

        public UpgradeNameAndAvatarRegisterInput(String mail, String token, String url_avatar, String user_name){
            this.mail = mail;
            this.token = token;
            this.url_avatar = url_avatar;
            this.user_name = user_name;
        }
    }

    public static class RegisterInput {
        private String verify_key;
        private String verify_purpose;
        private int verify_type;

        public RegisterInput(String verify_key, String verify_purpose, int verify_type) {
            this.verify_key = verify_key;
            this.verify_purpose = verify_purpose;
            this.verify_type = verify_type;
        }
    }

    public static class VerifyInput {
        private String verify_code;
        private String verify_key;

        public VerifyInput(String verify_code, String verify_key) {
            this.verify_code = verify_code;
            this.verify_key = verify_key;
        }
    }

    public static class UpdatePasswordInput {
        private String password;
        private String token;

        public UpdatePasswordInput(String password, String token) {
            this.password = password;
            this.token = token;
        }
    }

    public static class LoginInput {
        private String user_account;
        private String user_password;

        public LoginInput(String user_account, String user_password) {
            this.user_account = user_account;
            this.user_password = user_password;
        }
    }

    public static class RefreshTokenInput {
        private String access_token;
        private String refresh_token;

        public RefreshTokenInput(String access_token, String refresh_token) {
            this.access_token = access_token;
            this.refresh_token = refresh_token;
        }
    }
}