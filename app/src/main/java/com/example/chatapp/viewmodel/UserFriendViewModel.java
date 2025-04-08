package com.example.chatapp.viewmodel;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatapp.api.ApiManager;
import com.example.chatapp.dto.UserFriendDto;
import com.example.chatapp.models.WebRTCMessage;
import com.example.chatapp.models.response.ResponseData;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFriendViewModel extends ViewModel {
    private MutableLiveData<List<UserFriendDto>> userFriendList = new MutableLiveData<>();
    public LiveData<List<UserFriendDto>> userFriendLiveData = userFriendList;

    public void loadUserFriendData(ApiManager apiManager, String token) {
        apiManager.getListFriend(token, 10,1 ,new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseData<Object> responseData = response.body();
                    userFriendList.setValue(processResponse(responseData));
                }
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                userFriendList.setValue(null);
            }
        });
    }

    private List<UserFriendDto> processResponse(ResponseData<Object> responseData) {
        List<UserFriendDto> userFriendDtos = new ArrayList<>();
        if (responseData.getCode() == 20001 && "success".equals(responseData.getMessage())) {
            Gson gson = new Gson();
            JsonElement dataElement = gson.toJsonTree(responseData.getData());
            if(dataElement != null && dataElement.isJsonArray()) {
                JsonArray dataArray = dataElement.getAsJsonArray();
                Type listType = new TypeToken<List<UserFriendDto>>() {}.getType();
                userFriendDtos = gson.fromJson(dataArray, listType);
            }
        }
        return userFriendDtos;
    }
}
