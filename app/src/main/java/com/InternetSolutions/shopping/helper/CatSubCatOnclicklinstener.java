package com.InternetSolutions.shopping.helper;

import android.view.View;

import com.InternetSolutions.shopping.modelsList.catSubCatlistModel;

public interface CatSubCatOnclicklinstener {
    void onItemClick(catSubCatlistModel item);
    void onItemTouch(catSubCatlistModel item);
    void addToFavClick(View v, String position);

}
