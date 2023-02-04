package com.example.uberapp.gui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uberapp.R;
import com.example.uberapp.core.dto.AcumulatedReviewsDTO;
import com.example.uberapp.core.dto.ReviewResponseDTO;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.DriverService;
import com.example.uberapp.core.services.ReviewService;
import com.example.uberapp.gui.adapters.DriverHistoryAdapter;
import com.example.uberapp.gui.adapters.ReviewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewListDialog extends Dialog  {
    private Integer rideID;
    ReviewService reviewService = APIClient.getClient().create(ReviewService.class);
    private Button button;

    private Activity a;
    public ReviewListDialog(Activity a, Integer rideID){
        super(a);
        this.a=a;
        this.rideID=rideID;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_reviews_list);
        ListView listView=(ListView) findViewById(R.id.reviewsList);
        button=(Button) findViewById(R.id.exitReviewList);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Call<List<AcumulatedReviewsDTO>> getRideReviews=reviewService.getRideReviews(rideID);
        getRideReviews.enqueue(new Callback<List<AcumulatedReviewsDTO>>() {
            @Override
            public void onResponse(Call<List<AcumulatedReviewsDTO>> call, Response<List<AcumulatedReviewsDTO>> response) {
                List<ReviewResponseDTO> reviews=new ArrayList<>();
                List<AcumulatedReviewsDTO> acumulatedReviews=response.body();
                if (acumulatedReviews.size()!=0){
                for (AcumulatedReviewsDTO acumulatedReview:acumulatedReviews) {
                    if (acumulatedReview.getVehicleReview()!=null){
                        ReviewResponseDTO newReview= acumulatedReview.getVehicleReview();
                        newReview.setType("VEHICLE");
                        reviews.add(newReview);
                    }
                    if (acumulatedReview.getDriverReview()!=null){
                        ReviewResponseDTO newReview= acumulatedReview.getDriverReview();
                        newReview.setType("RIDE");
                        reviews.add(newReview);
                    }
                }
                ReviewAdapter adapter = new ReviewAdapter(a,reviews);
                listView.setAdapter(adapter);
                }
                else {
                    dismiss();
                    Toast.makeText(getContext(), "There is no reviews for this ride" , Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<List<AcumulatedReviewsDTO>> call, Throwable t) {

                Toast.makeText(getContext(), t.getMessage() , Toast.LENGTH_SHORT).show();
                System.out.println(t.getMessage());
            }
        });
    }
}
