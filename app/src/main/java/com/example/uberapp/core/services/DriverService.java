package com.example.uberapp.core.services;

import com.example.uberapp.core.dto.AllRidesDTO;
import com.example.uberapp.core.dto.DocumentDTO;
import com.example.uberapp.core.dto.DriverStatus;
import com.example.uberapp.core.dto.StatisticsDTO;
import com.example.uberapp.core.dto.UserDetailedDTO;
import com.example.uberapp.core.dto.VehicleDTO;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface DriverService {
    @GET("/api/driver/{id}")
    Call<UserDetailedDTO> getDriver(@Path("id") Integer id);

    @GET("/api/driver/{id}/rides")
    Call<AllRidesDTO> getDriverRides(@Path("id") Integer id);

    @PUT("/api/driver/{id}")
    Call<UserDetailedDTO> updateDriver(@Path("id") Integer id, @Body UserDetailedDTO userDetailedDTO);

    @GET("/api/driver/{driverId}/vehicle")
    Call<VehicleDTO> getVehicle(@Path("driverId") Integer driverId);

    @GET("/api/driver/{driverId}/statistics")
    Call<StatisticsDTO> getDriverStatistics(@Path("driverId") Integer driverId);

    @Multipart
    @POST("api/driver/{driverId}/document/{type}")
    Call<DocumentDTO> createDocument(@Path("driverId") Integer driverId, @Path("type") String type, @Part MultipartBody.Part filePart);

    @GET("api/driver/{driverId}/documents")
    Call<DocumentDTO[]> getDocuments(@Path("driverId") Integer driverId);

    @DELETE("api/driver/document/{documentId}")
    Call<ResponseBody> deleteDocument(@Path("documentId") Integer documentId);

    @PUT("api/driver/status/{driverId}")
    Call<Void> updateDriverStatus(@Path("driverId") Integer driverId, @Body DriverStatus driverStatus);

    @GET("api/driver/status/{driverId}")
    Call<DriverStatus> getDriverStatus(@Path("driverId") Integer driverId);
}
