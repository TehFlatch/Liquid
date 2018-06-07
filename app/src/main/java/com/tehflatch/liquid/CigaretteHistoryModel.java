package com.tehflatch.aquafy;


public class CigaretteHistoryModel {
    public String date;
    public String id;
    public String price;
    public String brand;
    public String key;
    public int order;

    public CigaretteHistoryModel(String key, String date, String id, String price, String brand, int order) {
        this.date = date;
        this.id = id;
        this.price = price;
        this.order = order;
        this.brand = brand;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
