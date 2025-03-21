package com.example.chatapp.models.request;

import java.util.List;

public class ChatModels {

    public static class CreateChatPrivateInput {
        private String user1;
        private String user2;

        public CreateChatPrivateInput(String user1, String user2) {
            this.user1 = user1;
            this.user2 = user2;
        }
    }

    public static class CreateChatGroupInput {
        private String user_id_create;
        private String group_name;
        private List<String> list_id;

        public CreateChatGroupInput(String user_id_create, String group_name, List<String> list_id) {
            this.user_id_create = user_id_create;
            this.group_name = group_name;
            this.list_id = list_id;
        }
    }

    public static class UpgradeChatInfoInput {
        private String user_admin_id;
        private String group_chat_id;
        private String group_name_update;
        private String group_avatar;

        public UpgradeChatInfoInput(String user_admin_id, String group_chat_id,
                                    String group_name_update, String group_avatar) {
            this.user_admin_id = user_admin_id;
            this.group_chat_id = group_chat_id;
            this.group_name_update = group_name_update;
            this.group_avatar = group_avatar;
        }
    }

    public static class AddMemberToChatInput {
        private String admin_chat_id;
        private String chat_id;
        private String user_add_id;

        public AddMemberToChatInput(String admin_chat_id, String chat_id, String user_add_id) {
            this.admin_chat_id = admin_chat_id;
            this.chat_id = chat_id;
            this.user_add_id = user_add_id;
        }
    }

    public static class DelMenForChatInput {
        private String admin_chat_id;
        private String chat_id;
        private String user_del_id;

        public DelMenForChatInput(String admin_chat_id, String chat_id, String user_del_id) {
            this.admin_chat_id = admin_chat_id;
            this.chat_id = chat_id;
            this.user_del_id = user_del_id;
        }
    }

    public static class ChangeAdminGroupChatInput {
        private String old_admin_id;
        private String group_chat_id;
        private String new_admin_id;

        public ChangeAdminGroupChatInput(String old_admin_id, String group_chat_id, String new_admin_id) {
            this.old_admin_id = old_admin_id;
            this.group_chat_id = group_chat_id;
            this.new_admin_id = new_admin_id;
        }
    }

    public static class DelChatInput {
        private String admin_chat_id;
        private String chat_id;

        public DelChatInput(String admin_chat_id, String chat_id) {
            this.admin_chat_id = admin_chat_id;
            this.chat_id = chat_id;
        }
    }
}