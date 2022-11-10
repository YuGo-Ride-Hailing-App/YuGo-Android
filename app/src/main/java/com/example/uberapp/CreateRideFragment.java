package com.example.uberapp;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class CreateRideFragment extends DialogFragment {


    public CreateRideFragment() {
        // Required empty public constructor
        super(R.layout.fragment_create_ride);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_create_ride, container, false);

    }
    public static String TAG = "CreateRideFragmentDialog";
}