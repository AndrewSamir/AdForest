package com.scriptsbundle.adforest.Shop.adapters;

/**
 * Created by taimu on 2/22/2018.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

import com.scriptsbundle.adforest.R;
import com.scriptsbundle.adforest.Shop.models.shopFeatureDetailsModel;

public class ItemDetailsFeatureListAdapter extends RecyclerView.Adapter<ItemDetailsFeatureListAdapter.CustomViewHolder> {
    private ArrayList<shopFeatureDetailsModel> feedItemList;
    private Context mContext;

    public ItemDetailsFeatureListAdapter(Context context, ArrayList<shopFeatureDetailsModel> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_features_views, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final shopFeatureDetailsModel feedItem = feedItemList.get(i);
        customViewHolder.tv_key.setText(feedItem.getTextViewKey());
        customViewHolder.tv_value.setText(feedItem.getTextViewValue());

    }

    @Override
    public int getItemCount() {
        return feedItemList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView tv_key, tv_value;

        CustomViewHolder(View view) {
            super(view);
            tv_key=view.findViewById(R.id.tv_key);
            tv_value=view.findViewById(R.id.tv_value);
        }
    }
}