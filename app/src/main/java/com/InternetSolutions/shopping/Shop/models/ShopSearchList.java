package com.InternetSolutions.shopping.Shop.models;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ShopSearchList {

    private String id;
    private String image;
    private String regPrice;
    private String salePrice;
    private String title;
    private int ratingStar;
    private String ratingText;
    private ArrayList<String> spinnerData;
    private ArrayList<String> spinnerValue;

    public int getRatingStar() {
        return ratingStar;
    }

    public void setRatingStar(int ratingStar) {
        this.ratingStar = ratingStar;
    }

    public String getRatingText() {
        return ratingText;
    }

    public void setRatingText(String ratingText) {
        this.ratingText = ratingText;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRegPrice() {
        return regPrice;
    }

    public void setRegPrice(String regPrice) {
        this.regPrice = regPrice;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getSpinnerData() {
        return this.spinnerData;
    }

    public void setSpinnerData(JSONArray spinnerData) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (spinnerData != null) {
            for (int i = 0; i < spinnerData.length(); i++) {
                try {
                    arrayList.add(spinnerData.getJSONObject(i).getString("value"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        this.spinnerData = arrayList;
    }

    public ArrayList<String> getSpinnerValue() {
        return this.spinnerValue;
    }

    public void setSpinnerValue(JSONArray spinnerData) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (spinnerData != null) {
            for (int i = 0; i < spinnerData.length(); i++) {
                try {
                    arrayList.add(spinnerData.getJSONObject(i).getString("key"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        this.spinnerValue = arrayList;
    }
}
