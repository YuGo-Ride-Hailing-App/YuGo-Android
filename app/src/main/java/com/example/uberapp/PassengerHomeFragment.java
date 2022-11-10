package com.example.uberapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PassengerHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PassengerHomeFragment extends Fragment {


    public PassengerHomeFragment() {
        // Required empty public constructor
    }

    public static PassengerHomeFragment newInstance(String param1, String param2) {
        PassengerHomeFragment fragment = new PassengerHomeFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_passenger_home, container, false);
        Button button = view.findViewById(R.id.buttonCreateRide);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CreateRideFragment().show(getChildFragmentManager().beginTransaction(), CreateRideFragment.TAG);
            }
        });
        return view;
    }

}