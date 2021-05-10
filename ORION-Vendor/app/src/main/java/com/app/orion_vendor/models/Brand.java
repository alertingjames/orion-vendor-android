package com.app.orion_vendor.models;

public class Brand {
    int id = 1;
    int userId = 1;
    int storeId = 1;
    String name = "";
    String category = "";
    String logoUrl = "";
    String registeredTime = "";
    String status = "";

    public Brand(){

    }

    public void setRegisteredTime(String registeredTime) {
        this.registeredTime = registeredTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegisteredTime() {
        return registeredTime;
    }

    public String getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getLogoUrl() {
        return logoUrl;
    }
}
