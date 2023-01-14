package com.example.uberapp.gui.fragments.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.example.uberapp.R;
import com.example.uberapp.core.dto.LocationDTO;
import com.example.uberapp.core.dto.RideDetailedDTO;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.RideService;
import com.example.uberapp.core.services.auth.TokenManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    MapFragment mapFragment;
    RideService rideService;
    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideService = APIClient.getClient().create(RideService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mapFragment = MapFragment.newInstance(true);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_home_map, mapFragment).commit();

        Call<RideDetailedDTO> activeRide;
        if (TokenManager.getRole().equals("DRIVER")){
            activeRide = rideService.getActiveDriverRide(TokenManager.getUserId());
        }
        else{
            activeRide = rideService.getActivePassengerRide(TokenManager.getUserId());
        }
        activeRide.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RideDetailedDTO> call, @NonNull Response<RideDetailedDTO> response) {
                if (response.code() == 200) {
                    RideDetailedDTO ride = response.body();
                    CurrentRideFragment currentRideFragment =  CurrentRideFragment.newInstance(ride);
                    fragmentManager.beginTransaction().replace(R.id.fragment_current_ride, currentRideFragment).commit();

                    LocationDTO departure = ride.getLocations().get(0).getDeparture();
                    LocationDTO destination = ride.getLocations().get(0).getDestination();
                    mapFragment.createMarker(departure.getLatitude(), departure.getLongitude(), "Departure");
                    mapFragment.createMarker(destination.getLatitude(), destination.getLongitude(), "Destination");
                    mapFragment.createRoute(departure.getLatitude(), departure.getLongitude(),
                            destination.getLatitude(), destination.getLongitude());

                }
                else{
                    ExtendedFloatingActionButton createRideButton = view.findViewById(R.id.buttonCreateRide);
                    CardView toggleButton = view.findViewById(R.id.online_offline_button);
                    if (TokenManager.getRole().equals("DRIVER")) {
                        createRideButton.setVisibility(View.GONE);
                        toggleButton.setVisibility(View.VISIBLE);
                    }
                    else{
                        createRideButton.setVisibility(View.VISIBLE);
                        toggleButton.setVisibility(View.GONE);
                        createRideButton.setOnClickListener(view1 -> new CreateRideFragment()
                                .show(getChildFragmentManager().beginTransaction(),
                                        CreateRideFragment.TAG));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RideDetailedDTO> call, @NonNull Throwable t) {

            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }
}