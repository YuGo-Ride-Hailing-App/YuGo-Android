package com.example.uberapp.gui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberapp.R;
import com.example.uberapp.core.LocalSettings;
import com.example.uberapp.core.auth.TokenManager;
import com.example.uberapp.core.dto.LocationDTO;
import com.example.uberapp.core.dto.MessageDTO;
import com.example.uberapp.core.dto.RideDetailedDTO;
import com.example.uberapp.core.dto.UserDetailedDTO;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.RideService;
import com.example.uberapp.core.services.UserService;
import com.example.uberapp.gui.dialogs.ExitAppDialog;
import com.example.uberapp.gui.dialogs.NewRideDialog;
import com.example.uberapp.gui.fragments.account.AccountFragment;
import com.example.uberapp.gui.fragments.history.UserHistoryFragment;
import com.example.uberapp.gui.fragments.home.HomeFragment;
import com.example.uberapp.gui.fragments.inbox.UserInboxFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class DriverMainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, NewRideDialog.OnAcceptRideListener {
    BottomNavigationView bottomNavigationView;
    NewRideDialog nrd;
    private StompClient mStompClient;
    private final RideService rideService = APIClient.getClient().create(RideService.class);
    private final UserService userService = APIClient.getClient().create(UserService.class);
    public CompositeDisposable disposables;
    private void sendNotification(Integer rideID){
        Call<RideDetailedDTO> call = rideService.getRide(rideID);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RideDetailedDTO> call, Response<RideDetailedDTO> response) {
                RideDetailedDTO ride = response.body();
                LocationDTO departure = ride.getLocations().get(0).getDeparture();
                LocationDTO destination = ride.getLocations().get(0).getDestination();

                String notifText = "Departure: " + departure.getAddress().substring(0, 20) + "\n" +
                        "Destination: " + destination.getAddress().substring(0, 20) + "\n" +
                        "Estimated time: " + ride.getEstimatedTimeInMinutes() + "min" + "\n" +
                        "Number of passengers: " + ride.getPassengers().size() + "\n" +
                        "Total cost: $" + ride.getTotalCost();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(DriverMainActivity.this, "RideOfferNotificationID")
                        .setSmallIcon(R.drawable.icon_notification)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notifText))
                        .setContentTitle("You have a new ride offer.")
                        .setContentText(notifText)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(DriverMainActivity.this);
                if (ActivityCompat.checkSelfPermission(DriverMainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                notificationManager.notify(199, builder.build());
            }

            @Override
            public void onFailure(Call<RideDetailedDTO> call, Throwable t) {

            }
        });
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        this.disposables = new CompositeDisposable();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.driverHome);
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://" + LocalSettings.localIP + ":9000/api/socket/websocket");

        mStompClient.topic("/ride-topic/driver-request/"+ TokenManager.getUserId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(topicMessage -> {
                    Gson gson= new Gson();
                    JsonObject jsonObject=gson.fromJson(topicMessage.getPayload(), JsonObject.class);
                    Integer rideID=jsonObject.getAsJsonPrimitive("rideID").getAsInt();

                    sendNotification(rideID);
                    nrd=new NewRideDialog(rideID);
                    nrd.show(getSupportFragmentManager(),NewRideDialog.TAG);

                }, throwable -> System.out.println(throwable.getMessage()));
        mStompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {

                case OPENED:
                    System.out.println("OPENED");
                    break;

                case ERROR:
                    System.out.println(lifecycleEvent.getException().getMessage());
                    break;

                case CLOSED:
                    System.out.println("CLOSED");
                    break;
            }
        });

        mStompClient.connect();

        this.setupMessagesSocket();
    }

    public void setupMessagesSocket(){
        StompClient mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://" + LocalSettings.localIP + ":9000/api/socket/websocket");

        Disposable webSocket = mStompClient.topic("/message-topic/"+ TokenManager.getUserId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(topicMessage -> {
                    Gson gson = new Gson();
                    MessageDTO messageDTO = gson.fromJson(topicMessage.getPayload(), MessageDTO.class);
                    sendMessageNotification(messageDTO);

                }, throwable -> System.out.println(throwable.getMessage()));

        mStompClient.connect();
        this.disposables.add(webSocket);
    }

    public void sendMessageNotification(MessageDTO messageDTO){

        Call<UserDetailedDTO> userCall = userService.getUser(messageDTO.getSenderId());
        userCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserDetailedDTO> call, @NonNull Response<UserDetailedDTO> response) {
                if (response.code() == 200){
                    UserDetailedDTO sender = response.body();
                    String messageContent=messageDTO.getMessage();
                    if (messageDTO.getMessage().length()>40)
                        messageContent=messageContent.substring(0,40);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(DriverMainActivity.this, "MessageNotificationID")
                            .setSmallIcon(R.drawable.icon_notification)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(messageContent))
                            .setContentTitle("Message from: " + sender.getName() + " " + sender.getSurname())
                            .setContentText(messageContent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(DriverMainActivity.this);
                    if (ActivityCompat.checkSelfPermission(DriverMainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    notificationManager.notify(199, builder.build());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDetailedDTO> call, @NonNull Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigationView.getSelectedItemId() == R.id.driverHome) {
            ExitAppDialog ead = new ExitAppDialog(this);
            ead.show();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.flDriverFragment, homeFragment).commit();
            bottomNavigationView.setSelectedItemId(R.id.driverHome);
        }
    }

    HomeFragment homeFragment = new HomeFragment();
    UserInboxFragment userInboxFragment = new UserInboxFragment();
    UserHistoryFragment userHistoryFragment = new UserHistoryFragment();
    AccountFragment driverAccountFragment = new AccountFragment();


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.driverHome:
                getSupportFragmentManager().beginTransaction().replace(R.id.flDriverFragment, homeFragment).commit();
                return true;
            case R.id.driverInbox:
                getSupportFragmentManager().beginTransaction().replace(R.id.flDriverFragment, userInboxFragment).commit();
                return true;
            case R.id.driverHistory:
                getSupportFragmentManager().beginTransaction().replace(R.id.flDriverFragment, userHistoryFragment).commit();
                return true;
            case R.id.driverAccount:
                getSupportFragmentManager().beginTransaction().replace(R.id.flDriverFragment, driverAccountFragment).commit();
                return true;
        }
        return false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mStompClient.disconnect();
        if (nrd!=null)
            nrd.onDestroy();

        disposables.dispose();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (nrd!=null)
            nrd.onPause();
    }

    @Override
    public void acceptRide(RideDetailedDTO ride) {
        homeFragment.acceptRide(ride);
    }
}