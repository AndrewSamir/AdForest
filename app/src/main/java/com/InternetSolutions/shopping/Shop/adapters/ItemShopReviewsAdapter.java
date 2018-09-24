package com.InternetSolutions.shopping.Shop.adapters;

/**
 * Created by taimu on 2/22/2018.
 */

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import com.InternetSolutions.shopping.R;
import com.InternetSolutions.shopping.Shop.models.ShopSearchList;
import com.InternetSolutions.shopping.Shop.models.shopReviewsModel;
import com.InternetSolutions.shopping.utills.SettingsMain;

public class ItemShopReviewsAdapter extends RecyclerView.Adapter<ItemShopReviewsAdapter.CustomViewHolder> {
    private ArrayList<shopReviewsModel> feedItemList;
    private Context mContext;

    public ItemShopReviewsAdapter(Context context, ArrayList<shopReviewsModel> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_reviews, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final shopReviewsModel feedItem = feedItemList.get(i);
        SettingsMain.adforest_changeRattingBarcolor(customViewHolder.user_ratingBar, mContext);

        customViewHolder.tv_user_name.setText(feedItem.getName());
        customViewHolder.tv_review_date.setText(feedItem.getDate());
        customViewHolder.tv_reviews_description.setText(feedItem.getDescription());
        customViewHolder.user_ratingBar.setRating(Float.parseFloat(feedItem.getRating()));

        if (!TextUtils.isEmpty(feedItem.getProfileImage()))
            Picasso.with(mContext).load(feedItem.getProfileImage())
                    .resize(270, 270)
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.image_view);
    }

    @Override
    public int getItemCount() {
        return feedItemList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image_view;
        TextView tv_user_name, tv_review_date,tv_reviews_description;
        RatingBar user_ratingBar;

        CustomViewHolder(View view) {
            super(view);
            this.image_view = view.findViewById(R.id.image_view);
            this.tv_user_name = view.findViewById(R.id.tv_user_name);
            this.tv_review_date = view.findViewById(R.id.tv_review_date);
            this.user_ratingBar = view.findViewById(R.id.user_ratingBar);
            this.tv_reviews_description = view.findViewById(R.id.tv_reviews_description);
        }
    }
}