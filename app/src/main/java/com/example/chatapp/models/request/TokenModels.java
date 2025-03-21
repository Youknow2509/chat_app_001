package com.example.chatapp.models.request;

public class TokenModels {

    public static class JwtInput {
        private String data;

        public JwtInput(String data) {
            this.data = data;
        }
    }
}