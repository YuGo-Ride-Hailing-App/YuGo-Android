package com.example.uberapp.gui.fragments.account;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.uberapp.R;
import com.example.uberapp.core.model.User;
import com.example.uberapp.core.tools.UserMockup;

public class DriverInfoFragment extends Fragment {


    public DriverInfoFragment() {
        // Required empty public constructor
    }
    public static DriverInfoFragment newInstance() {
        DriverInfoFragment fragment = new DriverInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver_info, container, false);
        User user = UserMockup.getUsers().get(1);

        EditText name = view.findViewById(R.id.editTextFirstName);
        name.setText(user.getName());

        EditText lastName = view.findViewById(R.id.editTextLastName);
        lastName.setText(user.getLastName());

        EditText phoneNumber = view.findViewById(R.id.editTextPhone);
        phoneNumber.setText(user.getPhoneNumber());

        EditText email = view.findViewById(R.id.editTextEmail);
        email.setText(user.getEmail());

        EditText address = view.findViewById(R.id.editTextAddress);
        address.setText(user.getAddress());

        ActivityResultLauncher<PickVisualMediaRequest> pickDriversLicnece =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        Button buttonDriversLicence = view.findViewById(R.id.buttonPickDriversLicence);
        buttonDriversLicence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDriversLicnece.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        ActivityResultLauncher<PickVisualMediaRequest> pickRegistrationLicence =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        Button buttonRegistrationLicence = view.findViewById(R.id.buttonPickRegistrationLicence);
        buttonRegistrationLicence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickRegistrationLicence.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
        return view;
    }
}