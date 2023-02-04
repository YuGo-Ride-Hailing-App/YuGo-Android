package com.example.uberapp.gui.fragments.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.uberapp.R;
import com.example.uberapp.core.model.LocationInfo;
import com.example.uberapp.core.services.APIMaps;
import com.example.uberapp.core.services.MapService;
import com.example.uberapp.gui.adapters.RecommendedAddressAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class CreateRideSubfragment01 extends Fragment {

    private ListView recommendedAddressList;
    private MapService mapService;
    private RecommendedAddressAdapter adapter;
    private EditText destinationEditText;
    private EditText departureEditText;
    private View view;

    private View btnManualDeparture;
    private View btnManualDestination;
    LocationInfo departure;
    LocationInfo destination;
    private String recommendedAddressContext;

    public interface OnRouteChangedListener{
        void onRideRouteChanged(LocationInfo departure, LocationInfo destination);
        void enableManualDeparturePicker();
        void enableManualDestinationPicker();
    }
    OnRouteChangedListener routeChangedListener;

    public CreateRideSubfragment01() {
        // Required empty public constructor
    }

    public void setDepartureAddress(LocationInfo locationInfo){
        departureEditText.setText(locationInfo.getAddress());
        departure = locationInfo;
        if (departure!=null && destination!=null)
            routeChangedListener.onRideRouteChanged(departure, destination);
    }
    public void setDestinationAddress(LocationInfo locationInfo){
        destinationEditText.setText(locationInfo.getAddress());
        destination = locationInfo;
        if (departure!=null && destination!=null)
            routeChangedListener.onRideRouteChanged(departure, destination);
    }

    @SuppressLint("CheckResult")
    private void loadRecommendedAddresses(String searchText){
        mapService.search(searchText).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(responseBody -> {
                    List<LocationInfo> data = new ArrayList<>();
                    Gson gson = new Gson();
                    JsonArray jsonArray = gson.fromJson(responseBody.string(), JsonArray.class);
                    for(int i = 0; i < jsonArray.size(); i++){
                        JsonObject jsonAddress = (JsonObject) jsonArray.get(i);
                        String addressName = jsonAddress.getAsJsonPrimitive("display_name").getAsString();
                        double lat = jsonAddress.getAsJsonPrimitive("lat").getAsDouble();
                        double lng = jsonAddress.getAsJsonPrimitive("lon").getAsDouble();
                        LocationInfo locationInfo = new LocationInfo(addressName, lat, lng);
                        data.add(locationInfo);
                    }
                    adapter.changeDataSet(data);
                }, throwable -> Toast.makeText(view.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show());
    }

    private void selectionChanged(){
        if(recommendedAddressContext.equals("DEPARTURE")){
            departureEditText.setText(departure.getAddress());
        }else if(recommendedAddressContext.equals("DESTINATION")){
            destinationEditText.setText(destination.getAddress());
        }
        routeChangedListener.onRideRouteChanged(departure, destination);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        routeChangedListener = (OnRouteChangedListener) getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_ride_subfragment01, container, false);

        btnManualDeparture = view.findViewById(R.id.btnManualDeparture);
        btnManualDeparture.setOnClickListener(v -> routeChangedListener.enableManualDeparturePicker());
        btnManualDestination = view.findViewById(R.id.btnManualDestination);
        btnManualDestination.setOnClickListener(v -> routeChangedListener.enableManualDestinationPicker());

        mapService= APIMaps.getClient().create(MapService.class);

        adapter = new RecommendedAddressAdapter((Activity) getContext(), new ArrayList<>());
        recommendedAddressList = view.findViewById(R.id.recommendedAddressList);
        recommendedAddressList.setAdapter(adapter);
        recommendedAddressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocationInfo locationInfo = (LocationInfo) parent.getItemAtPosition(position);
                if(recommendedAddressContext.equals("DEPARTURE")){
                    departure = locationInfo;
                }else if(recommendedAddressContext.equals("DESTINATION")){
                    destination = locationInfo;
                }
                selectionChanged();
            }
        });

        departureEditText = view.findViewById(R.id.editTextStartingLocation);
        if(departure != null){
            departureEditText.setText(departure.getAddress());
        }
        destinationEditText = view.findViewById(R.id.editTextDestination);
        if(destination != null){
            destinationEditText.setText(destination.getAddress());
        }

        departureEditText.setFocusableInTouchMode(true);
        departureEditText.requestFocus();

        departureEditText.setOnKeyListener(new View.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && keyCode == KeyEvent.KEYCODE_ENTER){
                    recommendedAddressContext = "DEPARTURE";
                    loadRecommendedAddresses(departureEditText.getText().toString());

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        destinationEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && keyCode == KeyEvent.KEYCODE_ENTER){
                    recommendedAddressContext = "DESTINATION";
                    loadRecommendedAddresses(destinationEditText.getText().toString());
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        return view;
    }
}