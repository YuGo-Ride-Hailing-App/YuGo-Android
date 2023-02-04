package com.example.uberapp.gui.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;

import com.example.uberapp.R;
import com.example.uberapp.core.auth.TokenManager;
import com.example.uberapp.core.dto.FavoritePathDTO;
import com.example.uberapp.core.dto.RideDetailedDTO;
import com.example.uberapp.core.dto.UserDetailedDTO;
import com.example.uberapp.core.dto.UserSimplifiedDTO;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.DriverService;
import com.example.uberapp.core.services.ImageService;
import com.example.uberapp.core.services.PassengerService;
import com.example.uberapp.gui.dialogs.AddReviewDialog;
import com.example.uberapp.gui.dialogs.AddToFavoritesDialog;
import com.example.uberapp.gui.dialogs.NewRideDialog;
import com.example.uberapp.gui.dialogs.ReasonDialog;
import com.example.uberapp.gui.dialogs.ReviewListDialog;
import com.example.uberapp.gui.fragments.home.MapFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHistoryAdapter extends BaseAdapter {

    public Activity activity;
    public List<RideDetailedDTO> rides;
    public HashMap<Integer,MapView> mapViews;
    DriverService driverService = APIClient.getClient().create(DriverService.class);
    PassengerService passengerService = APIClient.getClient().create(PassengerService.class);
    ImageService imageService = APIClient.getClient().create(ImageService.class);

    public HashMap<Integer,Boolean> isFirst;
    public DriverHistoryAdapter(Activity activity, List<RideDetailedDTO>rides){
        this.activity = activity;
        this.rides = rides;
        this.mapViews=new HashMap<>();
        this.isFirst=new HashMap<>();
        for(int i=0;i<=rides.size();i++){
            MapView mapView= new MapView(activity);
            mapViews.put(i,mapView);
        }
    }

    @Override
    public int getCount() {
        return rides.size();
    }

    @Override
    public Object getItem(int i) {
        return rides.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private String parseDuration(Duration duration){
        String minute=String.format("%02d",Math.abs(duration.toMinutes()));
        return minute+"min";

    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        RideDetailedDTO vht = rides.get(i);
        View v = view;
        if(view == null) {
            v = LayoutInflater.from(activity).inflate(R.layout.list_item_driver_history, null);
        }

        //LAYOUT ITEMS
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        TextView nameLb = (TextView) v.findViewById(R.id.nameLb);
        TextView startLocation = (TextView) v.findViewById(R.id.startText);
        TextView endLocation = (TextView) v.findViewById(R.id.destText);
        TextView startTime = (TextView) v.findViewById(R.id.startTime);
        TextView endTime = (TextView) v.findViewById(R.id.endTime);
        TextView price = (TextView) v.findViewById(R.id.priceTag);
        TextView distance = (TextView) v.findViewById(R.id.distanceText);
        TextView personNum = (TextView) v.findViewById(R.id.personNum);
        TextView dateTitle = (TextView) v.findViewById(R.id.dateTitleHistory);
        ShapeableImageView profilePic= (ShapeableImageView) v.findViewById(R.id.profilePicHistory);
        ImageView babyTransportFalse= (ImageView) v.findViewById(R.id.babyIconDriverHistory);
        ImageView babyTransportTrue= (ImageView) v.findViewById(R.id.babyIconDriverHistoryTrue);
        ImageView petTransportFalse= (ImageView) v.findViewById(R.id.petIconDriverHistory);
        ImageView petTransportTrue= (ImageView) v.findViewById(R.id.petIconDriverHistoryTrue);
        LinearLayout linearLayout= (LinearLayout) v.findViewById(R.id.profilesHistory);
        ImageButton reviewBtn=(ImageButton) v.findViewById(R.id.showRatingsHistory);
        ImageButton createRideBtn=(ImageButton) v.findViewById(R.id.createRideHistory);
        ImageButton addToFavoritesBtn=(ImageButton) v.findViewById(R.id.addToFavoritesHistory);

        if (TokenManager.getRole().equals("DRIVER")){
            createRideBtn.setVisibility(View.GONE);
            addToFavoritesBtn.setVisibility(View.GONE);

        }

        //SET PET AND BABY TRANSPORT
        if (vht.isBabyTransport())
            babyTransportFalse.setVisibility(View.GONE);
        else
            babyTransportTrue.setVisibility(View.GONE);
        if (vht.isPetTransport())
            petTransportFalse.setVisibility(View.GONE);
        else
            petTransportTrue.setVisibility(View.GONE);


        //SET START AND END TIME OF THE RIDE
        LocalDateTime fromTime = null;
        LocalDateTime toTime = null;
        if (vht.getStartTime()!=null)
            fromTime = LocalDateTime.parse(vht.getStartTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (vht.getEndTime()!=null)
            toTime = LocalDateTime.parse(vht.getEndTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        //SHOW DATE IF IT IS DIFFERENT
        if (i!=0)
            if(fromTime.toLocalDate().isEqual(LocalDateTime.parse(rides.get(i-1).getStartTime(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()))
                dateTitle.setVisibility(View.GONE);



        // SET OTHER RIDE PARAMETERS
        startLocation.setText(cleanUpLocation(vht.getLocations().get(0).getDeparture().getAddress(),0));
        startLocation.setTooltipText(cleanUpLocation(vht.getLocations().get(0).getDeparture().getAddress(),2));
        endLocation.setText(cleanUpLocation(vht.getLocations().get(0).getDestination().getAddress(),0));
        endLocation.setTooltipText(cleanUpLocation(vht.getLocations().get(0).getDestination().getAddress(),2));
        startTime.setText(fromTime.format(DateTimeFormatter.ofPattern("hh:mm")));
        if (toTime!=null)
            endTime.setText(toTime.format(DateTimeFormatter.ofPattern("hh:mm")));
        price.setText("$" + String.format("%.2f", vht.getTotalCost()));
        dateTitle.setText(fromTime.format(formatter));
        personNum.setText(String.valueOf(vht.getPassengers().size()));


        //GET MAIN USER NAME AND DETAILS
        Call<UserDetailedDTO> userCall;
        if (TokenManager.getRole().equals("DRIVER")){
            userCall = passengerService.getPassenger(vht.getPassengers().get(0).getId());
        }
        else{
            userCall = driverService.getDriver(vht.getDriver().getId());
        }
        View finalV1 = v;
        userCall.enqueue(new Callback<>() {
             @Override
             public void onResponse(@NonNull Call<UserDetailedDTO> call, @NonNull Response<UserDetailedDTO> response) {
                 //SET MAIN USER NAME AND PICTURE
                 UserDetailedDTO user = response.body();
                 nameLb.setText(user.getName() + " " + user.getSurname());
                 Call<ResponseBody> profilePictureCall = imageService.getProfilePicture(user.getProfilePicture());
                 profilePictureCall.enqueue(new Callback<>() {
                     @Override
                     public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                         try {
                             byte[] bytes = new byte[0];
                             bytes = response.body().bytes();
                             Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                             profilePic.setImageBitmap(bitmap);
                         } catch (IOException e) {
                             throw new RuntimeException(e);
                         }
                     }
                     @Override
                     public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                         }
                 });

             }
             @Override
             public void onFailure(Call<UserDetailedDTO> call, Throwable t) {}
         });

        GeoPoint startPoint = new GeoPoint(vht.getLocations().get(0).getDeparture().getLatitude(),vht.getLocations().get(0).getDeparture().getLongitude() );
        GeoPoint endPoint = new GeoPoint(vht.getLocations().get(0).getDestination().getLatitude(),vht.getLocations().get(0).getDestination().getLongitude() );

        final float scale = activity.getResources().getDisplayMetrics().density;
        LinearLayout mapLayout = (LinearLayout) v.findViewById(R.id.mapHistoryLayout);
        if (mapLayout.findViewWithTag(vht.getId()+"_map")==null) {
            mapLayout.removeAllViews();
            mapLayout.addView(mapViews.get(i));
            mapViews.get(i).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (150 * scale + 0.5f)));
            mapViews.get(i).setTag(vht.getId() + "_map");
            loadMap(mapViews.get(i));
            IMapController mapController = mapViews.get(i).getController();
            mapController.setZoom(14.0);
            mapController.setCenter(new GeoPoint(44.97639, 19.61222));
            //SET ROUTE DISTANCE

            getLength(startPoint.getLatitude(), startPoint.getLongitude(), endPoint.getLatitude(), endPoint.getLongitude(), mapViews.get(i), new DriverHistoryAdapter.CallbackLengthHistory() {
                @Override
                public void onSuccess(Double value) {
                    distance.setText(Double.toString(Math.round(value * 100) / 100.0) + "km");
                }
            });
        }


        //SET OTHER PASSENGER ICONS
        linearLayout.removeAllViews();
        for (UserSimplifiedDTO user:vht.getPassengers()) {
            Call<UserDetailedDTO> userPictureCall=passengerService.getPassenger(user.getId());
            userPictureCall.enqueue(new Callback<UserDetailedDTO>() {
                @Override
                public void onResponse(Call<UserDetailedDTO> call, Response<UserDetailedDTO> response) {
                    UserDetailedDTO detailedUser=response.body();
                    int pixels = (int) (40 * scale + 0.5f);
                    Call<ResponseBody> profilePictureCall = imageService.getProfilePicture(detailedUser.getProfilePicture());
                    profilePictureCall.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            try {
                                byte[] bytes = new byte[0];
                                bytes = response.body().bytes();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                ShapeableImageView imageView = new ShapeableImageView(activity);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        pixels,pixels
                                );
                                params.setMarginStart((int) (5*scale+0.5f));
                                params.setMarginEnd((int) (5*scale+0.5f));
                                imageView.setLayoutParams(params);
                                imageView.setTag(vht.getId()+"_"+detailedUser.getId());
                                imageView.setAdjustViewBounds(true);
                                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                imageView.setShapeAppearanceModel((ShapeAppearanceModel.builder().setAllCornerSizes(20 * scale + 0.5f).build()));
                                imageView.setTooltipText(detailedUser.getName()+" "+detailedUser.getSurname()+"\n"+detailedUser.getEmail()+"\n"+detailedUser.getTelephoneNumber());
                                imageView.setImageBitmap(bitmap);
                                if (linearLayout.findViewWithTag(vht.getId()+"_"+detailedUser.getId())==null)
                                    linearLayout.addView(imageView);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {}
                    });
                }

                @Override
                public void onFailure(Call<UserDetailedDTO> call, Throwable t) {}
            });
        }
        ImageButton arrow = v.findViewById(R.id.arrowBtnDriverHistory);
        RelativeLayout hiddenView = v.findViewById(R.id.hiddenDriverHistory);
        View finalV = v;
        View finalV2 = v;
        arrow.setOnClickListener(view_click -> {
            // If the CardView is already expanded, set its visibility
            // to gone and change the expand less icon to expand more.
            if (hiddenView.getVisibility() == View.VISIBLE) {
                // The transition of the hiddenView is carried out by the TransitionManager class.
                // Here we use an object of the AutoTransition Class to create a default transition
                //TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
                hiddenView.setVisibility(View.GONE);
                arrow.setImageResource(R.drawable.icon_arrow_down);
            }

            // If the CardView is not expanded, set its visibility to
            // visible and change the expand more icon to expand less.
            else {
                if (!isFirst.containsKey(i)) {
                    createRoute(startPoint.getLatitude(), startPoint.getLongitude(), endPoint.getLatitude(), endPoint.getLongitude(), finalV2, mapViews.get(i));
                    isFirst.put(i,true);
                }
                hiddenView.setVisibility(View.VISIBLE);
                arrow.setImageResource(R.drawable.icon_arrow_up);
            }
        });
        addToFavoritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoritePathDTO favorite=new FavoritePathDTO("",vht.getLocations(),vht.getPassengers(),vht.getVehicleType(),vht.isBabyTransport(),vht.isPetTransport());
                Dialog dialog = new AddToFavoritesDialog(activity,favorite);
                dialog.show();
            }
        });
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TokenManager.getRole().equals("PASSENGER")){
                    Dialog dialog=new AddReviewDialog(activity,vht.getId());
                    dialog.show();
                }
                else{
                    Dialog dialog=new ReviewListDialog(activity,vht.getId());
                    dialog.show();
                }
        }});


        return v;
    }
    public void createMarker(double latitude, double longitude, String title,Integer drawableID,MapView map,View v){
        if(map == null || map.getRepository() == null||activity.isDestroyed())
            return;

        for(int i=0;i<map.getOverlays().size();i++){
            Overlay overlay=map.getOverlays().get(i);
            if(overlay instanceof Marker && ((Marker) overlay).getId().equals(title)){
                map.getOverlays().remove(overlay);
            }
        }
        if (map==null)
            return;
        else if (map.getRepository()==null) {
            return;
        }
        Marker marker = new Marker(map);
        GeoPoint geoPoint = new GeoPoint(latitude,longitude);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setId(title);
        marker.setPanToView(true);
        Drawable d = ResourcesCompat.getDrawable(v.getResources(), drawableID, null);
        marker.setIcon(d);
        map.getOverlays().add(marker);
        map.invalidate();
        IMapController mapController = map.getController();
        mapController.setZoom(14.0);
        mapController.setCenter(geoPoint);
    }

    public void createRoute(double startLatitude,double startLongitude, double endLatitude, double endLongitude,View v,MapView map){
        if(map == null || map.getRepository() == null) {
            return;
        }

        ExecutorService executorService= Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                RoadManager roadManager = new OSRMRoadManager(v.getContext(),"Pera");

                ArrayList<GeoPoint> track = new ArrayList<>();
                GeoPoint startPoint = new GeoPoint(startLatitude, startLongitude );
                GeoPoint endPoint = new GeoPoint(endLatitude, endLongitude);
                track.add(startPoint);
                track.add(endPoint);

                Road road = roadManager.getRoad(track);

                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                ((Activity)v.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.getOverlays().add(roadOverlay);
                        if(map!=null)
                            if(map.getRepository()!=null)
                                createMarker(startLatitude, startLongitude, "Departure", R.drawable.start_location_pin, map, v);
                        if (map!=null)
                            if(map.getRepository()!=null)
                                createMarker(endLatitude, endLongitude, "Destination", R.drawable.finish_location_pin, map, v);
                        map.invalidate();
                        IMapController mapController = map.getController();
                        mapController.setZoom(12.0);
                        mapController.setCenter(new GeoPoint((startLatitude+endLatitude)/2,
                                (startLongitude+endLongitude)/2));
                    }
                });
            }
        });

    }
    public void loadMap(MapView map) {
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
        map.invalidate();
    }
    public Double getLength(double startLatitude,double startLongitude,double endLatitude,double endLongitude,MapView map,final DriverHistoryAdapter.CallbackLengthHistory callback){
        if(map == null || map.getRepository() == null) {
            return null;
        }
        final Double[] length = {0.0};

        ExecutorService executorService= Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                RoadManager roadManager = new OSRMRoadManager(activity,"RoadManager");

                ArrayList<GeoPoint> track = new ArrayList<>();
                GeoPoint startPoint = new GeoPoint(startLatitude, startLongitude );
                GeoPoint endPoint = new GeoPoint(endLatitude, endLongitude);
                track.add(startPoint);
                track.add(endPoint);

                Road road = roadManager.getRoad(track);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(road.mLength);
                    }
                });
            }
        });
        return length[0];
    }

    private String cleanUpLocation(String location,int dataCols){
        if (location.contains(",")){
            String[] partialLocations=location.split(",");
            String result="";
            for (int i=0;i< partialLocations.length;i++){
                if(i<=dataCols){
                    if (i==partialLocations.length-1 || i==dataCols)
                        result=result+partialLocations[i];
                    else
                        result=result+partialLocations[i]+", ";
                }
            }

            return result;
        }
        return location;

    }
    private interface CallbackLengthHistory {
        void onSuccess(Double value);
    }



}
