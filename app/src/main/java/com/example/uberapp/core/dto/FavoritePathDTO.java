package com.example.uberapp.core.dto;

import java.io.Serializable;
import java.util.List;

public class FavoritePathDTO implements Serializable {
    private String favoriteName;
    private List<PathDTO> locations;
    private List<UserSimplifiedDTO> passengers;
    private String vehicleType;
    private Boolean babyTransport;
    private Boolean petTransport;
    private Integer id;

    public FavoritePathDTO() {
    }

    public FavoritePathDTO(String favoriteName, List<PathDTO> locations, List<UserSimplifiedDTO> passengers, String vehicleType, Boolean babyTransport, Boolean petTransport, Integer id) {
        this.favoriteName = favoriteName;
        this.locations = locations;
        this.passengers = passengers;
        this.vehicleType = vehicleType;
        this.babyTransport = babyTransport;
        this.petTransport = petTransport;
        this.id = id;
    }
    public FavoritePathDTO(String favoriteName, List<PathDTO> locations, List<UserSimplifiedDTO> passengers, String vehicleType, Boolean babyTransport, Boolean petTransport) {
        this.favoriteName = favoriteName;
        this.locations = locations;
        this.passengers = passengers;
        this.vehicleType = vehicleType;
        this.babyTransport = babyTransport;
        this.petTransport = petTransport;
    }

    public String getFavoriteName() {
        return favoriteName;
    }

    public void setFavoriteName(String favoriteName) {
        this.favoriteName = favoriteName;
    }

    public List<PathDTO> getLocations() {
        return locations;
    }

    public void setLocations(List<PathDTO> locations) {
        this.locations = locations;
    }

    public List<UserSimplifiedDTO> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<UserSimplifiedDTO> passengers) {
        this.passengers = passengers;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Boolean getBabyTransport() {
        return babyTransport;
    }

    public void setBabyTransport(Boolean babyTransport) {
        this.babyTransport = babyTransport;
    }

    public Boolean getPetTransport() {
        return petTransport;
    }

    public void setPetTransport(Boolean petTransport) {
        this.petTransport = petTransport;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
