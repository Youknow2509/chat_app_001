package com.example.chatapp.viewmodel;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatapp.api.ApiManager;
import com.example.chatapp.dto.UserDto;
import com.example.chatapp.models.User;
import com.example.chatapp.models.response.ResponseData;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.lang.invoke.MutableCallSite;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends ViewModel {
    private MutableLiveData<List<UserDto>> userList = new MutableLiveData<>();
    private LiveData<List<UserDto>> userListLiveData = userList;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private View view;

    public void setView (View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }

    public void getUser(ApiManager apiManager, String token, String email) {
        isLoading.setValue(true);

        apiManager.findUser(token, email, 10, 1, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                isLoading.postValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    ResponseData<Object> responseData = response.body();
                    if (responseData.getCode() == 20001) {
                        userList.postValue(processResponseData(responseData));
                    } else {
                        errorMessage.postValue(responseData.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    private List<UserDto> processResponseData(ResponseData<Object> responseData) {
        List<UserDto> users = new ArrayList<>();
        if (responseData.getCode() == 20001) {
            Gson gson = new Gson();
            JsonElement dataElement = gson.toJsonTree(responseData.getData());

            if (dataElement.isJsonArray()) {
                JsonArray jsonArray = dataElement.getAsJsonArray();
                Type listType = new TypeToken<List<UserDto>>() {}.getType();
                users = gson.fromJson(jsonArray, listType);
            }
        }
        return users;
    }


    public LiveData<List<UserDto>> getUserListLiveData() {
        return userListLiveData;
    }
}
