package com.example.uberapp.gui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import com.example.uberapp.core.LocalSettings;
import com.example.uberapp.core.auth.TokenManager;
import com.example.uberapp.core.dto.FavoritePathDTO;
import com.example.uberapp.core.dto.MessageDTO;
import com.example.uberapp.core.dto.UserDetailedDTO;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.UserService;
import com.example.uberapp.gui.dialogs.ExitAppDialog;
import com.example.uberapp.gui.fragments.account.AccountFragment;
import com.example.uberapp.R;
import com.example.uberapp.gui.fragments.account.PassengerFavouritesFragment;
import com.example.uberapp.gui.fragments.history.UserHistoryFragment;
import com.example.uberapp.gui.fragments.home.HomeFragment;
import com.example.uberapp.gui.fragments.inbox.UserInboxFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class PassengerMainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    AccountFragment accountFragment = new AccountFragment();
    UserInboxFragment inboxFragment = new UserInboxFragment();
    UserHistoryFragment historyFragment = new UserHistoryFragment();

    PassengerFavouritesFragment passengerFavouritesFragment = new PassengerFavouritesFragment();
    private final UserService userService = APIClient.getClient().create(UserService.class);
    public CompositeDisposable disposables;
    StompClient mStompClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_main);

        this.disposables = new CompositeDisposable();
        bottomNavigationView = findViewById(R.id.passengerBottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.passenger_home);



        this.setupMessagesSocket();
    }

    public void setupMessagesSocket(){
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://" + LocalSettings.localIP + ":9000/api/socket/websocket");

        Disposable webSocket = mStompClient.topic("/message-topic/"+ TokenManager.getUserId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(topicMessage -> {
                    Gson gson = new Gson();
                    MessageDTO messageDTO = gson.fromJson(topicMessage.getPayload(), MessageDTO.class);
                    setupMessageNotification(messageDTO);

                }, throwable -> System.out.println(throwable.getMessage()));

        mStompClient.connect();
        disposables.add(webSocket);
    }
    public void setupMessageNotification(MessageDTO messageDTO){
        Call<UserDetailedDTO> userCall = userService.getUser(messageDTO.getSenderId());
        userCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserDetailedDTO> call, @NonNull Response<UserDetailedDTO> response) {
                if (response.code() == 200){
                    UserDetailedDTO sender = response.body();

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(PassengerMainActivity.this, "MessageNotificationID")
                            .setSmallIcon(R.drawable.icon_notification)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(messageDTO.getMessage().substring(0, 40)))
                            .setContentTitle("Message from: " + sender.getName() + " " + sender.getSurname())
                            .setContentText(messageDTO.getMessage().substring(0, 40) + "...")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(PassengerMainActivity.this);
                    if (ActivityCompat.checkSelfPermission(PassengerMainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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

    public void setup(FavoritePathDTO favoritePathDTO){
        if(favoritePathDTO != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.flPassengerMainFragment, homeFragment).commit();
            getSupportFragmentManager().executePendingTransactions();
            homeFragment.loadFavoritePath(favoritePathDTO);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStompClient.disconnect();
        disposables.dispose();
    }
    @Override
    public void onBackPressed() {
        if (bottomNavigationView.getSelectedItemId()==R.id.passenger_home)
        {
            ExitAppDialog ead=new ExitAppDialog(this);
            ead.show();
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.flPassengerMainFragment, homeFragment).commit();
            bottomNavigationView.setSelectedItemId(R.id.passenger_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.passenger_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.flPassengerMainFragment, homeFragment).commit();
                break;
            case R.id.passanger_account:
                getSupportFragmentManager().beginTransaction().replace(R.id.flPassengerMainFragment, accountFragment).commit();
                break;
            case R.id.passenger_inbox:
                getSupportFragmentManager().beginTransaction().replace(R.id.flPassengerMainFragment, inboxFragment).commit();
                break;
            case R.id.passanger_history:
                getSupportFragmentManager().beginTransaction().replace(R.id.flPassengerMainFragment, historyFragment).commit();
                break;
            case R.id.passenger_favorites:
                    getSupportFragmentManager().beginTransaction().replace(R.id.flPassengerMainFragment, passengerFavouritesFragment).commit();
                    break;
        }
        return true;
    }
}