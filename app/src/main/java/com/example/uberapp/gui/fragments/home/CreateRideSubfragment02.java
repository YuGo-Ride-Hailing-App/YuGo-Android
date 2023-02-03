package com.example.uberapp.gui.fragments.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.uberapp.R;
import com.example.uberapp.core.dto.VehicleTypeDTO;
import com.example.uberapp.core.model.VehicleCategory;
import com.example.uberapp.core.model.VehicleType;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.ImageService;
import com.example.uberapp.core.services.VehicleTypeService;
import com.example.uberapp.gui.adapters.VehicleTypeAdapter;

import org.osmdroid.bonuspack.routing.Road;

import java.util.List;

import io.reactivex.Observable;


public class CreateRideSubfragment02 extends Fragment {

    VehicleTypeService vehicleTypeService;
    List<VehicleType> vehicleTypes;
    ImageService imageService;
    ListView listView;
    VehicleTypeAdapter adapter;
    View view;
    CheckBox babyTransportCheckBox;
    CheckBox petTransportCheckBox;
    VehicleType vehicleType = null;
    boolean isBabyTransport = false;
    boolean isPetTransport = false;

    private Road road;

    public void setRoad(Road rd){
        this.road = rd;
    }
    public interface OnRidePropertiesChangedListener{
        void onVehicleTypeChanged(VehicleType vehicleType);
        void onPetTransportChanged(boolean isPetTransport);
        void onBabyTransportChanged(boolean isBabyTransport);
    }
    OnRidePropertiesChangedListener propertiesChangedListener;

    public void setVehicleType(String vehicleType, boolean isBabyTransport, boolean isPetTransport){
        for(VehicleType vt : vehicleTypes){
            if(vt.getVehicleCategory() == VehicleCategory.valueOf(vehicleType)){
                this.vehicleType = vt;
                this.isBabyTransport = isBabyTransport;
                this.isPetTransport = isPetTransport;
                petTransportCheckBox.setChecked(isPetTransport);
                babyTransportCheckBox.setChecked(isBabyTransport);
                propertiesChangedListener.onBabyTransportChanged(isBabyTransport);
                propertiesChangedListener.onPetTransportChanged(isPetTransport);
                propertiesChangedListener.onVehicleTypeChanged(this.vehicleType);
                break;
            }
        }
        listView.findViewById(R.id.standardListItem).setBackgroundResource(0);
        listView.findViewById(R.id.vanListItem).setBackgroundResource(0);
        listView.findViewById(R.id.luxListItem).setBackgroundResource(0);
        if(this.vehicleType.getVehicleCategory() == VehicleCategory.STANDARD){
            listView.findViewById(R.id.standardListItem).setBackgroundResource(R.drawable.vehicle_type_card_shape_selected);
        }else if (this.vehicleType.getVehicleCategory() == VehicleCategory.VAN) {
            listView.findViewById(R.id.vanListItem).setBackgroundResource(R.drawable.vehicle_type_card_shape_selected);
        }else {
            listView.findViewById(R.id.luxListItem).setBackgroundResource(R.drawable.vehicle_type_card_shape_selected);
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        propertiesChangedListener = (OnRidePropertiesChangedListener) getParentFragment();
        vehicleTypeService = APIClient.getClient().create(VehicleTypeService.class);
        imageService = APIClient.getClient().create(ImageService.class);
    }
    private Observable<VehicleType> fetchImage(VehicleTypeDTO vehicleTypeDTO){
        return this.imageService.getImage(vehicleTypeDTO.imgPath)
                .flatMap(responseBody ->{
                    VehicleType vehicleType = new VehicleType();
                    byte[] bytes = responseBody.bytes();
                    responseBody.close();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    vehicleType.setIcon(bitmap);
                    vehicleType.setVehicleCategory(VehicleCategory.valueOf(vehicleTypeDTO.vehicleType));
                    vehicleType.setPricePerUnit(vehicleTypeDTO.pricePerKm);
                    vehicleType.setId(vehicleTypeDTO.id);
                    return Observable.just(vehicleType);
                });
    }
    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_ride_subfragment02, container, false);

        listView = view.findViewById(R.id.listViewVehicleType);
        adapter = new VehicleTypeAdapter((Activity) getContext(), vehicleTypes, road);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, v, position, id) -> {
            for(int i = 0; i < parent.getCount(); i++){
                parent.getChildAt(i).findViewById(R.id.vehicleTypeCardBackgroundHolder).setBackgroundResource(0);
            }
            vehicleType = (VehicleType) parent.getItemAtPosition(position);
            propertiesChangedListener.onVehicleTypeChanged(vehicleType);
            v.findViewById(R.id.vehicleTypeCardBackgroundHolder).setBackgroundResource(R.drawable.vehicle_type_card_shape_selected);
        });
        babyTransportCheckBox = view.findViewById(R.id.checkBoxIncludeBaby);
        babyTransportCheckBox.setChecked(isBabyTransport);
        babyTransportCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isBabyTransport = isChecked;
                propertiesChangedListener.onBabyTransportChanged(isBabyTransport);
            }
        });
        petTransportCheckBox = view.findViewById(R.id.checkBoxIncludePets);
        petTransportCheckBox.setChecked(isPetTransport);
        petTransportCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isPetTransport = isChecked;
                propertiesChangedListener.onPetTransportChanged(isPetTransport);
            }
        });


        return view;
    }
}