package com.InternetSolutions.shopping.helper;

import android.view.View;

import com.InternetSolutions.shopping.modelsList.myAdsModel;

public interface MyAdsOnclicklinstener {

    void onItemClick(myAdsModel item);
    void delViewOnClick(View v, int position);
    void editViewOnClick(View v, int position);

}
