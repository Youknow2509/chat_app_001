package com.example.chatapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.chatapp.R;
import com.example.chatapp.activities.OnboardingActivity;
import com.example.chatapp.activities.Profile;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.utils.session.SessionManager;

public class Setting extends Fragment {

    private View root;
    private TextView profileName;
    private TextView profileStatus;
    private ImageView imgProfile;
    private LinearLayout logoutElement;
    private LinearLayout accountElement;

    //
    private SessionManager sessionManager;
    private UserProfileSession userProfileSession;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.root  =  inflater.inflate(R.layout.fragment_setting, container, false);

        initVariable();
        bindView();
        setDataToView();
        setEventInElementView();

        return root;
    }

    // init variable use
    private void initVariable() {
        this.sessionManager = new SessionManager(this.getContext());
        this.userProfileSession = this.sessionManager.getUserProfile();
    }

    // bind view to obj
    private void bindView() {
        this.profileName = root.findViewById(R.id.profile_name);
        this.profileStatus = root.findViewById(R.id.profile_status);
        this.imgProfile = root.findViewById(R.id.profile_image);
        this.logoutElement = root.findViewById(R.id.logout_section);
        this.accountElement = root.findViewById(R.id.account_section);
    }

    // set data to view
    private void setDataToView() {
        this.profileName.setText(userProfileSession.getName());
        this.profileStatus.setText(userProfileSession.getStatus());
        // show image view with url

    }

    // set event in element view
    private void setEventInElementView() {
        this.logoutElement.setOnClickListener(l -> {
            this.sessionManager.logout();

            // redirect to OnboardingActivity
            startActivity(new Intent(this.getContext(), OnboardingActivity.class));
            this.getActivity().finish();
        });
        this.accountElement.setOnClickListener(l->{
            startActivity(new Intent(this.getContext(), Profile.class));
        });
    }
}
