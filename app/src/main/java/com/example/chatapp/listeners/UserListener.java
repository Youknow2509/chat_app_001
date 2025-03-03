package com.example.chatapp.listeners;

import com.example.chatapp.models.User;

public interface UserListener {
    void onUserClick(User user);

    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);

    void onMultipleUsersAction(Boolean isMultipleUsersSelected);
}
