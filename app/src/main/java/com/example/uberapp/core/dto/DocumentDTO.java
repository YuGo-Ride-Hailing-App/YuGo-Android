package com.example.uberapp.core.dto;
public class DocumentDTO {
    private Integer id;
    private String name;
    private Integer driverId;
    private String documentType;

    public DocumentDTO(Integer id, String name, Integer driverId,
                       String documentType) {
        this.id = id;
        this.name = name;
        this.driverId = driverId;
        this.documentType=documentType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
