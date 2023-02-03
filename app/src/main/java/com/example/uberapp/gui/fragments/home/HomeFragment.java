package com.example.uberapp.gui.fragments.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.uberapp.R;
import com.example.uberapp.core.LocalSettings;
import com.example.uberapp.core.dto.AllRidesDTO;
import com.example.uberapp.core.dto.FavoritePathDTO;
import com.example.uberapp.core.dto.LocationDTO;
import com.example.uberapp.core.dto.MessageDTO;
import com.example.uberapp.core.dto.PathDTO;
import com.example.uberapp.core.dto.RideDetailedDTO;
import com.example.uberapp.core.dto.VehicleDTO;
import com.example.uberapp.core.model.LocationInfo;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.APIMaps;
import com.example.uberapp.core.services.APIRouting;
import com.example.uberapp.core.services.DriverService;
import com.example.uberapp.core.services.MapService;
import com.example.uberapp.core.services.RideService;
import com.example.uberapp.core.auth.TokenManager;
import com.example.uberapp.core.services.RoutingService;
import com.example.uberapp.core.services.VehicleService;
import com.example.uberapp.gui.dialogs.AddReviewDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class HomeFragment extends Fragment implements CurrentRideFragment.OnEndCurrentRideListener, CreateRideSheet.OnRouteChangedListener, MapFragment.OnLocationSelectedListener {
    private MapFragment mapFragment;
    private CurrentRideFragment currentRideFragment;
    private final RideService rideService = APIClient.getClient().create(RideService.class);
    private RideDetailedDTO currentRide;
    private RideDetailedDTO nextRide;
    private ExtendedFloatingActionButton startRideButton;
    private CardView onlineButton;
    private Fragment parentFragment;
    private FragmentManager fragmentManager;
    private CreateRideSheet createRideSheet;
    private Vibrator vibrator;

    private VehicleDTO vehicle;

    VehicleService vehicleService = APIClient.getClient().create(VehicleService.class);
    DriverService driverService = APIClient.getClient().create(DriverService.class);
    MapService mapService = APIMaps.getClient().create(MapService.class);

    private StompClient mStompClient;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        createNotificationChannel();
    }

    public void loadFavoritePath(FavoritePathDTO favoritePath){
        createRideSheet = new CreateRideSheet();
        getChildFragmentManager().beginTransaction().replace(R.id.homeFragmentContentHolder, createRideSheet).commit();
        createRideSheet.loadFavoritePath(favoritePath);
        createRideSheet.show(getChildFragmentManager(), null);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel vehicleArrivalChannel = new NotificationChannel("VehicleArrivalNotificationID", "Vehicle arrival", NotificationManager.IMPORTANCE_DEFAULT);
            vehicleArrivalChannel.setDescription("Notifies you that driver has arrived at pickup location.");

            NotificationChannel rideOfferChannel = new NotificationChannel("RideOfferNotificationID", "Ride offer", NotificationManager.IMPORTANCE_DEFAULT);
            rideOfferChannel.setDescription("Notifies you about new ride offer.");

            NotificationChannel messageChannel = new NotificationChannel("MessageNotificationID", "Message notification", NotificationManager.IMPORTANCE_DEFAULT);
            rideOfferChannel.setDescription("Notifies you about received message.");

            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(vehicleArrivalChannel);
            notificationManager.createNotificationChannel(rideOfferChannel);
            notificationManager.createNotificationChannel(messageChannel);
        }
    }

    private ScheduledExecutorService vehiclesExecutor = Executors.newScheduledThreadPool(1);
    private void showVehicles(){
        vehiclesExecutor.shutdown();
        vehiclesExecutor = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> showVehiclesHandler = vehiclesExecutor.scheduleWithFixedDelay(() -> {
            getActivity().runOnUiThread(() -> {
                Call<List<VehicleDTO>> vehiclesRequest = vehicleService.getVehicles();

                vehiclesRequest.enqueue(new Callback<List<VehicleDTO>>() {
                    @Override
                    public void onResponse(Call<List<VehicleDTO>> call, Response<List<VehicleDTO>> response) {
                        List<VehicleDTO> vehicles = response.body();
                        for(VehicleDTO vehicleDTO : vehicles){
                            Call<RideDetailedDTO> activeRide = rideService.getActiveDriverRide(vehicleDTO.getDriverId());

                            activeRide.enqueue(new Callback<>() {
                                @Override
                                public void onResponse(Call<RideDetailedDTO> call, Response<RideDetailedDTO> response) {
                                    if(response.body() == null){
                                        if(vehicle != null && Objects.equals(vehicle.getId(), vehicleDTO.getId())){
                                            mapFragment.createSecondaryMarker(vehicleDTO.getCurrentLocation().getLatitude(), vehicleDTO.getCurrentLocation().getLongitude(), "vehicle-" + vehicleDTO.getId(), R.drawable.current_location_pin);
                                        }else{
                                            mapFragment.createSecondaryMarker(vehicleDTO.getCurrentLocation().getLatitude(), vehicleDTO.getCurrentLocation().getLongitude(), "vehicle-" + vehicleDTO.getId(), R.drawable.busy_vehicle_pin);
                                        }
                                    }else{
                                        if(vehicle == null || !Objects.equals(vehicle.getId(), vehicleDTO.getId())){
                                            mapFragment.createSecondaryMarker(vehicleDTO.getCurrentLocation().getLatitude(), vehicleDTO.getCurrentLocation().getLongitude(), "vehicle-" + vehicleDTO.getId(), R.drawable.available_vehicle_pin);
                                        }
                                    }

                                }

                                @Override
                                public void onFailure(Call<RideDetailedDTO> call, Throwable t) {

                                }
                            });

//                            if(vehicle == null || !Objects.equals(vehicle.getId(), vehicleDTO.getId())){
//                                mapFragment.createSecondaryMarker(vehicleDTO.getCurrentLocation().getLatitude(), vehicleDTO.getCurrentLocation().getLongitude(), "vehicle-" + String.valueOf(vehicleDTO.getId()), R.drawable.current_location_pin);
//                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<VehicleDTO>> call, Throwable t) {

                    }
                });
            });
        }, 0, 1, TimeUnit.SECONDS);


    }

    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        startRideButton = view.findViewById(R.id.buttonStartRide);
        onlineButton = view.findViewById(R.id.online_offline_button);

        //fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager = getChildFragmentManager();

        Call<RideDetailedDTO> activeRide;
        if (TokenManager.getRole().equals("DRIVER")) {
            activeRide = rideService.getActiveDriverRide(TokenManager.getUserId());
            mapFragment = MapFragment.newInstance(false);
            Call<VehicleDTO> vehicleRequest = driverService.getVehicle(TokenManager.getUserId());
            vehicleRequest.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<VehicleDTO> call, Response<VehicleDTO> response) {
                    vehicle = response.body();
                    initStartRideButton();
                    onlineButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(Call<VehicleDTO> call, Throwable t) {

                }
            });

        } else {
            if (savedInstanceState == null) {
                createRideSheet = new CreateRideSheet();
                getChildFragmentManager().beginTransaction().add(R.id.homeFragmentContentHolder, createRideSheet).commit();
            }
            activeRide = rideService.getActivePassengerRide(TokenManager.getUserId());
            mapFragment = MapFragment.newInstance(false);

            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://" + LocalSettings.localIP + ":9000/api/socket/websocket");

            mStompClient.topic("/ride-topic/notify-passenger-vehicle-arrival/" + TokenManager.getUserId()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(topicMessage -> {
                        Toast.makeText(getContext(), "Vehicle has arrived and pickup location", Toast.LENGTH_SHORT).show();
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "VehicleArrivalNotificationID")
                                .setSmallIcon(R.drawable.icon_notification)
                                .setContentTitle("Vehicle has arrived at pickup location.")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        notificationManager.notify(99, builder.build());
                    }, throwable -> System.out.println(throwable.getMessage()));

            mStompClient.topic("/ride-topic/notify-passenger-vehicle-location/"+ TokenManager.getUserId()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(topicMessage -> {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            if(currentRide != null){
                                Gson gson = new Gson();
                                JsonObject coordinates = gson.fromJson(topicMessage.getPayload(), JsonObject.class);
                                double latitude = coordinates.get("latitude").getAsDouble();
                                double longitude = coordinates.get("longitude").getAsDouble();
                                LocationDTO destination = currentRide.getLocations().get(0).getDestination();
                                getActivity().runOnUiThread(() -> mapFragment.createRoute(latitude, longitude, destination.getLatitude(), destination.getLongitude()));
                            }
                        });
                    }, throwable -> System.out.println(throwable.getMessage()));

            mStompClient.topic("/ride-topic/notify-passenger-end-ride/"+ TokenManager.getUserId()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(topicMessage -> {
                        Gson gson= new Gson();
                        JsonObject jsonObject=gson.fromJson(topicMessage.getPayload(), JsonObject.class);
                        Integer rideID=jsonObject.getAsJsonPrimitive("rideID").getAsInt();
                        createRideSheet = new CreateRideSheet();
                        getChildFragmentManager().beginTransaction().replace(R.id.homeFragmentContentHolder, createRideSheet).commit();
                        mapFragment = MapFragment.newInstance(true);
                        fragmentManager.beginTransaction().replace(R.id.fragment_home_map, mapFragment).commit();
                        fragmentManager.beginTransaction().remove(currentRideFragment).commit();
                        if (TokenManager.getRole().equals("PASSENGER")){
                            Dialog dialog=new AddReviewDialog((Activity) getContext(),rideID);
                            dialog.show();
                        }

                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                            System.out.println(throwable.getMessage());
                        }
                    });
            mStompClient.topic("/ride-topic/notify-passenger-start-ride/"+ TokenManager.getUserId()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(topicMessage -> {
                        mapFragment = MapFragment.newInstance(false);
                        fragmentManager.beginTransaction().replace(R.id.fragment_home_map, mapFragment).commit();
                        Call<RideDetailedDTO> activeRideNotified;
                        activeRideNotified = rideService.getActivePassengerRide(TokenManager.getUserId());
                        activeRideNotified.enqueue(new Callback<>() {
                           @Override
                           public void onResponse(@NonNull Call<RideDetailedDTO> call, @NonNull Response<RideDetailedDTO> response) {
                               if (response.code() == 200) {
                                   RideDetailedDTO ride = response.body();
                                   currentRide = ride;
                                   currentRideFragment =  CurrentRideFragment.newInstance(ride, parentFragment);
                                   fragmentManager.beginTransaction().replace(R.id.fragment_current_ride, currentRideFragment).commit();
                                   LocationDTO departure = ride.getLocations().get(0).getDeparture();
                                   LocationDTO destination = ride.getLocations().get(0).getDestination();
                                   mapFragment.createRoute(departure.getLatitude(), departure.getLongitude(),
                                           destination.getLatitude(), destination.getLongitude());

                               }}

                            @Override
                            public void onFailure(Call<RideDetailedDTO> call, Throwable t) {

                            }
                        });

                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                            System.out.println(throwable.getMessage());
                        }
                    });
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
        }

        fragmentManager.beginTransaction().replace(R.id.fragment_home_map, mapFragment).commit();
        parentFragment = this;
        activeRide.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RideDetailedDTO> call, @NonNull Response<RideDetailedDTO> response) {
                if (response.code() == 200) {
                    RideDetailedDTO ride = response.body();
                    currentRideFragment =  CurrentRideFragment.newInstance(ride, parentFragment);
                    fragmentManager.beginTransaction().replace(R.id.fragment_current_ride, currentRideFragment).commit();
                    LocationDTO departure = ride.getLocations().get(0).getDeparture();
                    LocationDTO destination = ride.getLocations().get(0).getDestination();
                    mapFragment.createRoute(departure.getLatitude(), departure.getLongitude(),
                            destination.getLatitude(), destination.getLongitude());

                }
                else{
                    if (TokenManager.getRole().equals("DRIVER")) {
                        // TODO kada se vozac uloguje proveriti da li ima ACCEPTED voznje, ako ima postaviti start ride dugme na VISIBLE
                    }
                    else{
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RideDetailedDTO> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Ups, something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
        showVehicles();
        return view;
    }

    public void initStartRideButton(){
        startRideButton.setOnClickListener(v -> {
            Call<RideDetailedDTO> startRide = rideService.startRide(currentRide.getId());
            startRide.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<RideDetailedDTO> call, Response<RideDetailedDTO> response) {
                    currentRideFragment = CurrentRideFragment.newInstance(currentRide, parentFragment);
                    fragmentManager.beginTransaction().replace(R.id.fragment_current_ride, currentRideFragment).commit();
                    startRideButton.setVisibility(View.GONE);
                    LocationDTO departure = currentRide.getLocations().get(0).getDeparture();
                    LocationDTO destination = currentRide.getLocations().get(0).getDestination();
                    simulateRide(departure.getLatitude(), departure.getLongitude(), destination.getLatitude(), destination.getLongitude(), vehicle.getId());
                }

                @Override
                public void onFailure(Call<RideDetailedDTO> call, Throwable t) {
                    Toast.makeText(getContext(), "Ups, something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateVehicleLocation(GeoPoint point, Integer vehicleID){
        LocationDTO location = new LocationDTO("some address", point.getLatitude(), point.getLongitude());
        Call<Void> updateLocationRequest = vehicleService.updateVehicleLocation(vehicleID, location);
        updateLocationRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
    private ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(1);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    @SuppressLint("CheckResult")
    private void simulateRide(double departureLatitude, double departureLongitude, double destinationLatitude, double destinationLongitude, Integer vehicleID){
        timerExecutor.shutdown();
        executor.shutdown();
        timerExecutor = Executors.newScheduledThreadPool(1);
        executor = Executors.newSingleThreadExecutor();

        RoutingService routingService = APIRouting.getClient().create(RoutingService.class);
        routingService.getRoute(departureLatitude, departureLongitude, destinationLatitude, destinationLongitude)
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(responseBody -> {
                    executor.execute(() -> {
                        Gson gson = new Gson();
                        JsonObject response;
                        try {
                            response = gson.fromJson(responseBody.string(), JsonObject.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        Queue<GeoPoint> points = new LinkedList<>();

                        JsonArray routes = response.getAsJsonArray("routes");
                        JsonObject route = (JsonObject) routes.get(0);
                        JsonArray legs = route.getAsJsonArray("legs");
                        JsonObject leg = (JsonObject) legs.get(0);
                        JsonArray steps = leg.getAsJsonArray("steps");
                        for(JsonElement step : steps){
                            JsonObject geometry = step.getAsJsonObject().getAsJsonObject("geometry");
                            JsonArray coordinates = geometry.getAsJsonArray("coordinates");
                            for(JsonElement coord : coordinates){
                                double latitude = coord.getAsJsonArray().get(1).getAsDouble();
                                double longitude = coord.getAsJsonArray().get(0).getAsDouble();
                                points.add(new GeoPoint(latitude, longitude));
                            }
                        }
                        ScheduledFuture<?> validateStepHandler = timerExecutor.scheduleWithFixedDelay(() -> {
                            GeoPoint point = points.remove();
                            if(points.isEmpty()){
                                timerExecutor.shutdown();
                            }
                            updateVehicleLocation(point, vehicleID);
                            getActivity().runOnUiThread(() -> mapFragment.createRoute(point.getLatitude(), point.getLongitude(), destinationLatitude, destinationLongitude));

                        }, 0, 1, TimeUnit.SECONDS);

                    });

                },throwable -> Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show());
    }
    public void acceptRide(RideDetailedDTO ride){
        if(currentRide == null){
            currentRide = ride;
            LocationDTO departure = ride.getLocations().get(0).getDeparture();
            startRideButton.setVisibility(View.VISIBLE);
            simulateRide(vehicle.getCurrentLocation().getLatitude(), vehicle.getCurrentLocation().getLongitude(), departure.getLatitude(), departure.getLongitude(), vehicle.getId());
        }else {
            nextRide = ride;
        }
    }

    @Override
    public void endCurrentRide() {
        if(nextRide != null){
            currentRide = nextRide;
            nextRide = null;
            startRideButton.setVisibility(View.VISIBLE);
        }else{
            currentRide = null;
        }

        if(TokenManager.getRole().equals("DRIVER"))
            mapFragment = MapFragment.newInstance(false);
        else
            mapFragment = MapFragment.newInstance(true);
        fragmentManager.beginTransaction().replace(R.id.fragment_home_map, mapFragment).commit();
        fragmentManager.beginTransaction().remove(currentRideFragment).commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapFragment.onPause();
    }

    @Override
    public void onRideRouteChanged(LocationInfo departure, LocationInfo destination) {
        if(departure != null && destination != null){
            mapFragment.createRoute(departure.getLatitude(), departure.getLongitude(), destination.getLatitude(), destination.getLongitude());
        }
        else if(departure != null){
            mapFragment.createMarker(departure.getLatitude(), departure.getLongitude(),"Departure",R.drawable.start_location_pin);

        }
        else if(destination != null){
            mapFragment.createMarker(destination.getLatitude(),destination.getLongitude(),"Destination",R.drawable.finish_location_pin);
        }
    }

    @Override
    public void enableManualDestinationPicker() {
        isManualDepartureSelectionEnabled = false;
        isManualDestinationSelectionEnabled = true;
    }

    @Override
    public void enableManualDeparturePicker() {
        isManualDepartureSelectionEnabled = true;
        isManualDestinationSelectionEnabled = false;
    }

    @Override
    public void refreshPage() {
        this.createRideSheet = new CreateRideSheet();
        mapFragment.removeMarker("Departure");
        mapFragment.removeMarker("Destination");
        getChildFragmentManager().beginTransaction().replace(R.id.homeFragmentContentHolder, createRideSheet).commit();
    }

    private boolean isManualDestinationSelectionEnabled;
    private boolean isManualDepartureSelectionEnabled;
    @Override
    public void onLocationSelectedEvent(GeoPoint point) {
        if(isManualDepartureSelectionEnabled){
            isManualDepartureSelectionEnabled = false;
            Call<ResponseBody> addressRequest = mapService.reverseSearch(Double.toString(point.getLatitude()), Double.toString(point.getLongitude()));
            addressRequest.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Gson gson = new Gson();
                    JsonObject responseJson = null;
                    try {
                        responseJson = gson.fromJson(response.body().string(), JsonObject.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String address = responseJson.get("display_name").getAsString();
                    LocationInfo locationInfo = new LocationInfo(address, point.getLatitude(), point.getLongitude());
                    createRideSheet.setDeparture(locationInfo);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });

        } else if (isManualDestinationSelectionEnabled) {
            isManualDestinationSelectionEnabled = false;
            Call<ResponseBody> addressRequest = mapService.reverseSearch(Double.toString(point.getLatitude()), Double.toString(point.getLongitude()));
            addressRequest.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Gson gson = new Gson();
                    JsonObject responseJson = null;
                    try {
                        responseJson = gson.fromJson(response.body().string(), JsonObject.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String address = responseJson.get("display_name").getAsString();
                    LocationInfo locationInfo = new LocationInfo(address, point.getLatitude(), point.getLongitude());
                    createRideSheet.setDestination(locationInfo);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }
}