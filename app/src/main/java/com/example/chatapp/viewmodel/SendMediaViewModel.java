package com.example.chatapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.chatapp.activities.ChatConversationActivity;
import com.example.chatapp.utils.cloudinary.CloudinaryManager;

import java.io.File;
import java.util.Map;

public class SendMediaViewModel extends AndroidViewModel {
    private MutableLiveData<String> stringUrlResult = new MutableLiveData<>();

    private CloudinaryManager cloudinaryManager;

    private static final String TAG = "SendMediaViewModel";
    private Context context;


    public SendMediaViewModel(@NonNull Application application) {
        super(application);
        context = getApplication().getApplicationContext();
        cloudinaryManager = CloudinaryManager.getInstance(this.context);
    }

    public MutableLiveData<String> getStringUrlResult() {
        return stringUrlResult;
    }

    /**
     * Upload image to Cloudinary
     */
    private void uploadImage(File imageFile, String userId) {
        String folder = "/users/" + userId + "/images/";
        cloudinaryManager.uploadImage(
                Uri.fromFile(imageFile).toString(),
                folder,
                new CloudinaryManager.CloudinaryCallback<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {

                            // Get public ID and URL
                            String publicId = (String) result.get("public_id");
                            String url = (String) result.get("url");

                            Log.d(TAG, "Public Id: " + publicId);
                            Log.d(TAG, "URL: " + url);

                            Log.d(TAG, "Public Id: " + publicId);
                            Log.d(TAG, "URL: " + url);

                            // Set the URL to LiveData
                            stringUrlResult.postValue(url);
                    }

                    @Override
                    public void onError(String errorMsg) {

                            Toast.makeText(context, "Upload failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Upload failed: " + errorMsg);
                    }

                    @Override
                    public void onProgress(int progress) {
                    }
                });
    }
}
