package com.example.uberapp.gui.fragments.home;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.uberapp.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment implements LocationListener{
    private static final String ARG_CURRENT_LOCATION = "showCurrentLocation";
    private MapView map;
    private LocationManager locationManager;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private boolean showCurrentLocation = false;

    public interface OnLocationSelectedListener{
        void onLocationSelectedEvent(GeoPoint point);
    }
    private OnLocationSelectedListener parentComponent;
    private Polyline path;
    public static MapFragment newInstance(boolean showCurrentLocation) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_CURRENT_LOCATION, showCurrentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showCurrentLocation = (boolean) getArguments().get(ARG_CURRENT_LOCATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        parentComponent = (OnLocationSelectedListener) getParentFragment();
        Context context = getContext();
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        map = view.findViewById(R.id.mapView);

        MapEventsReceiver eventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                parentComponent.onLocationSelectedEvent(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        MapEventsOverlay overlay = new MapEventsOverlay(eventsReceiver);
        map.getOverlays().add(overlay);

        loadMap();
        IMapController mapController = map.getController();
        mapController.setZoom(14.0);
        mapController.setCenter(new GeoPoint(44.97639, 19.61222));

        return view;
    }

    public void loadMap() {
        map.getTileProvider().clearTileCache();
        Configuration.getInstance().setCacheMapTileCount((short) 12);
        Configuration.getInstance().setCacheMapTileOvershoot((short) 12);
        map.setTileSource(new OnlineTileSourceBase("", 1, 20,
                512, ".png",
                new String[]{"https://a.tile.openstreetmap.org/"}) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                return getBaseUrl()
                        + MapTileIndex.getZoom(pMapTileIndex)
                        + "/" + MapTileIndex.getX(pMapTileIndex)
                        + "/" + MapTileIndex.getY(pMapTileIndex)
                        + mImageFilenameEnding;
            }
        });
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        map.invalidate();
    }

    public void createSecondaryMarker(double latitude, double longitude, String title,Integer drawableID){
        if(map == null || map.getRepository() == null) {
            return;
        }

        for(int i=0;i<map.getOverlays().size();i++){
            Overlay overlay=map.getOverlays().get(i);
            if(overlay instanceof Marker && ((Marker) overlay).getId().equals(title)){
                map.getOverlays().remove(overlay);
            }
        }

        if (getActivity()==null || getActivity().isDestroyed())
            return;
        Marker marker = new Marker(map);
        GeoPoint geoPoint = new GeoPoint(latitude,longitude);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setId(title);
        marker.setPanToView(true);
        Drawable d = ResourcesCompat.getDrawable(getResources(), drawableID, null);
        marker.setIcon(d);
        map.getOverlays().add(marker);
        map.invalidate();
    }
    public void removeMarker(String title){
        if(map == null || map.getRepository() == null) {
            return;
        }

        for(int i=0;i<map.getOverlays().size();i++){
            Overlay overlay=map.getOverlays().get(i);
            if(overlay instanceof Marker && ((Marker) overlay).getId().equals(title)){
                map.getOverlays().remove(overlay);
            }
        }
    }
    public void createMarker(double latitude, double longitude, String title,Integer drawableID){
        if(map == null || map.getRepository() == null) {
            return;
        }

        for(int i=0;i<map.getOverlays().size();i++){
            Overlay overlay=map.getOverlays().get(i);
            if(overlay instanceof Marker && ((Marker) overlay).getId().equals(title)){
                map.getOverlays().remove(overlay);
            }
        }

        Marker marker = new Marker(map);
        GeoPoint geoPoint = new GeoPoint(latitude,longitude);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setId(title);
        marker.setPanToView(true);
        Drawable d = ResourcesCompat.getDrawable(getResources(), drawableID, null);
        marker.setIcon(d);
        map.getOverlays().add(marker);
        map.invalidate();

        IMapController mapController = map.getController();
        mapController.setZoom(14.0);
        mapController.setCenter(geoPoint);
    }

    public void createRoute(double startLatitude,double startLongitude, double endLatitude, double endLongitude){
        if(map == null || map.getRepository() == null) {
            return;
        }

        ExecutorService executorService= Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                RoadManager roadManager = new OSRMRoadManager(getContext(),"Pera");

                ArrayList<GeoPoint> track = new ArrayList<>();
                GeoPoint startPoint = new GeoPoint(startLatitude, startLongitude );
                GeoPoint endPoint = new GeoPoint(endLatitude, endLongitude);
                track.add(startPoint);
                track.add(endPoint);

                Road road = roadManager.getRoad(track);

                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(path != null){
                            map.getOverlays().remove(path);
                        }
                        path = roadOverlay;
                        map.getOverlays().add(path);
                        createMarker(startLatitude,startLongitude,"Departure",R.drawable.start_location_pin);
                        createMarker(endLatitude,endLongitude,"Destination",R.drawable.finish_location_pin);
                        map.invalidate();
                        IMapController mapController = map.getController();
                        mapController.setZoom(14.0);
                        mapController.setCenter(new GeoPoint((startLatitude+endLatitude)/2,
                                (startLongitude+endLongitude)/2));
                    }
                });
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean wifi = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        if (!gps && !wifi) {

        }
        else if (showCurrentLocation){
            if (checkLocationPermission()) {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                    locationManager.requestLocationUpdates(provider , 2000L, (float) 5, this);
                }
                else if(ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(provider, 2000L, (float) 5, this);
                }
            }
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                new AlertDialog.Builder(getActivity())
                        .setTitle("Allow user location")
                        .setMessage("To continue working we need your locations....Allow now?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(map == null || map.getRepository() == null) {
            return;
        }
        createMarker(location.getLatitude(), location.getLongitude(), "Current location",R.drawable.current_location_pin);
    }

}