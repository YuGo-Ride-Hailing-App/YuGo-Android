package com.example.uberapp.gui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.uberapp.R;
import com.example.uberapp.core.model.VehicleCategory;
import com.example.uberapp.core.model.VehicleType;
import com.example.uberapp.core.tools.VehicleTypeMockup;

import org.osmdroid.bonuspack.routing.Road;

import java.util.List;

public class VehicleTypeAdapter extends BaseAdapter {

    public Activity activity;
    private List<VehicleType> data;
    private List<View> items;
    private Road road;
    public VehicleTypeAdapter(Activity activity, List<VehicleType> data, Road road){
        this.activity = activity;
        this.data = data;
        this.road = road;
    }
    @Override
    public int getCount() {
        return VehicleTypeMockup.getVehicleTypes().size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return data.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        VehicleType vht = data.get(i);
        View v = view;
        if(vht.getVehicleCategory() == VehicleCategory.STANDARD){
            v.setId(R.id.standardListItem);
        } else if (vht.getVehicleCategory() == VehicleCategory.VAN) {
            v.setId(R.id.vanListItem);
        }else{
            v.setId(R.id.luxListItem);
        }

        if(view == null){
            v = LayoutInflater.from(activity).inflate(R.layout.list_item_vehicle_type, null);
        }

        ImageView icon = (ImageView) v.findViewById(R.id.imageViewVehicle);
        TextView category = (TextView) v.findViewById(R.id.textViewVehicleCategory);
        TextView priceTextView = (TextView) v.findViewById(R.id.textViewPrice);
        category.setText(vht.getVehicleCategory().toString());
        double distance = road.mLength;
        double price = Math.round(vht.getPricePerUnit() * distance * 100) / 100.0;
        priceTextView.setText("$"+String.valueOf(price));
        icon.setImageBitmap(vht.getIcon());

        return v;
    }
}
