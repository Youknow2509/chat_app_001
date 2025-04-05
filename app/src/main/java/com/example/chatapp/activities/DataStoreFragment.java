package com.example.chatapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.FragmentDataStorageBinding;
import com.example.chatapp.utils.store.StoreUtils2;

public class DataStoreFragment extends AppCompatActivity {

    private FragmentDataStorageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentDataStorageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long t = StoreUtils2.getTotalDataSize(this);
        Log.d("vinh", "getTotalDataSize: " + t);
        Toast.makeText(this, "Total size: " + StoreUtils2.formatSize(t), Toast.LENGTH_SHORT).show();
        // Set event listeners
//        binding.buttonClearSQLite.setOnClickListener(v -> clearSQLiteData());
//        binding.buttonClearImages.setOnClickListener(v -> clearImagesData());
//        binding.buttonClearVideos.setOnClickListener(v -> clearVideosData());
//        binding.buttonClearRecords.setOnClickListener(v -> clearRecordsData());
//        binding.buttonClearAllMedia.setOnClickListener(v -> clearAllMediaData());

    }

}
