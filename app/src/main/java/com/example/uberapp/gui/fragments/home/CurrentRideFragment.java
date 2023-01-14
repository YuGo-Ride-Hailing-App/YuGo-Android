package com.example.uberapp.gui.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;

import com.example.uberapp.R;
import com.example.uberapp.core.dto.RideDetailedDTO;
import com.example.uberapp.core.dto.UserDetailedDTO;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.ImageService;
import com.example.uberapp.core.services.UserService;
import com.example.uberapp.core.services.auth.TokenManager;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentRideFragment extends Fragment {
    private static final String ARG_RIDE = "ride";
    UserService userService = APIClient.getClient().create(UserService.class);
    ImageService imageService = APIClient.getClient().create(ImageService.class);
    RideDetailedDTO ride;

    public CurrentRideFragment() {
    }

    public static CurrentRideFragment newInstance(RideDetailedDTO ride) {
        CurrentRideFragment fragment = new CurrentRideFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE, ride);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ride = (RideDetailedDTO) getArguments().get(ARG_RIDE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_ride, container, false);
        ImageView profilePic = view.findViewById(R.id.driverProfilePic);
        Call<UserDetailedDTO> userCall;
        if (TokenManager.getRole().equals("DRIVER")){
            userCall = userService.getPassenger(ride.getPassengers().get(0).getId());
        }
        else{
            userCall = userService.getDriver(ride.getDriver().getId());
        }
        userCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserDetailedDTO> call, @NonNull Response<UserDetailedDTO> response) {
                UserDetailedDTO user = response.body();

                Call<ResponseBody> profilePictureCall = imageService.getProfilePicture(user.getProfilePicture());
                profilePictureCall.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        try {
                            byte[] bytes = response.body().bytes();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            profilePic.setImageBitmap(bitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                    }
                });

                profilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopupMenu(v, user);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<UserDetailedDTO> call, @NonNull Throwable t) {

            }
        });
        return view;
    }

    public void showPopupMenu(View view, UserDetailedDTO user){
        Context wrapper = new ContextThemeWrapper(getActivity(), R.style.popupBgStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper,view);
        Menu menu = popupMenu.getMenu();
        menu.add(0 , 0, 0,user.getName() + " " + user.getSurname());
        MenuItem call = menu.add(1 , 1, 1, "Call (" + user.getTelephoneNumber() + ")");
        call.setOnMenuItemClickListener(item -> {
            Uri number = Uri.parse("tel:" + user.getTelephoneNumber());
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            startActivity(callIntent);
            return true;
        });
        menu.add(2 , 2, 2,"Message");
        menu.add(3 , 3, 3,"Report");
        menu.setGroupDividerEnabled(true);
        popupMenu.show();
    }
}