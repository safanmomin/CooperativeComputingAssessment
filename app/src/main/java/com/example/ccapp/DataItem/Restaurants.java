package com.example.ccapp.DataItem;

public class Restaurants {
    private String restaurantID;
    private String restaurantTitle;
    private String restaurantType;

    public Restaurants(String restaurantID, String restaurantTitle, String restaurantType) {
        this.restaurantID = restaurantID;
        this.restaurantTitle = restaurantTitle;
        this.restaurantType = restaurantType;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public String getRestaurantTitle() {
        return restaurantTitle;
    }

    public void setRestaurantTitle(String restaurantTitle) {
        this.restaurantTitle = restaurantTitle;
    }

    public String getRestaurantType() {
        return restaurantType;
    }

    public void setRestaurantType(String restaurantType) {
        this.restaurantType = restaurantType;
    }
}
