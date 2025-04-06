package com.example.chatapp.dto;

public class MediaMessageDTO extends MessageDTO {

    private String mediaType;
    private String mediaUrl;
    public MediaMessageDTO(String content, String chatId, String mediaType, String mediaUrl) {
        super(content, chatId, "media");
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
    }




}
