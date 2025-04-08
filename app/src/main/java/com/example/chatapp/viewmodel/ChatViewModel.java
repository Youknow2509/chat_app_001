package com.example.chatapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.dto.ChatDTO;
import com.example.chatapp.models.UserDetail;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.models.request.UserModels;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.cloudinary.CloudinaryManager;
import com.example.chatapp.utils.file.MediaUtils;
import com.example.chatapp.utils.session.SessionManager;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends ViewModel {
    private MutableLiveData<List<ChatDTO>> _chatList = new MutableLiveData<>();
    public LiveData<List<ChatDTO>> chatList = _chatList;

    public void loadChatData(ApiManager apiManager, String token) {
        apiManager.getListChatPrivateForUser(token, 10, 1, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _chatList.postValue(processResponse(response.body()));
                }
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                _chatList.postValue(null);
            }
        });
    }

    private List<ChatDTO> processResponse(ResponseData<Object> response) {
        List<ChatDTO> result = new ArrayList<>();
        if (response.getCode() == 20001 && "success".equals(response.getMessage())) {
            Gson gson = new Gson();
            JsonElement jsonElement = JsonParser.parseString(gson.toJson(response.getData()));

            // Thêm kiểm tra null và kiểu dữ liệu
            if (jsonElement != null && jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                Type listType = new TypeToken<List<ChatDTO>>() {}.getType();
                result = gson.fromJson(jsonArray, listType);
            }
        }
        return result;
    }

}