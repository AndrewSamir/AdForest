package com.InternetSolutions.shopping.helper;

import com.InternetSolutions.shopping.modelsList.PackagesModel;

public interface OnItemClickListenerPackages {
    void onItemClick(PackagesModel item);
    void onItemTouch();
    void onItemSelected(PackagesModel packagesModel,int spinnerPosition);
}
