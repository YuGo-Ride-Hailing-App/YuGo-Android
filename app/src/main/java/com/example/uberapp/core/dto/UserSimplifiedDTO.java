package com.example.uberapp.core.dto;

import java.io.Serializable;

public class UserSimplifiedDTO implements Serializable {
    private Integer id;
    private String email;

    public UserSimplifiedDTO() {
    }

    public UserSimplifiedDTO(Integer id, String email) {
        this.id = id;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
