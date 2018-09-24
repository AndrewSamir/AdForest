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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.InternetSolutions.shopping.R;
import com.InternetSolutions.shopping.Shop.models.ShopSearchList;
import com.InternetSolutions.shopping.utills.SettingsMain;

public class ItemShopSearchListAdapter extends RecyclerView.Adapter<ItemShopSearchListAdapter.CustomViewHolder> {

    private ArrayList<ShopSearchList> feedItemList;
    private Context mContext;
    private shopSearchClickListener shopSearchClickListener;
    private String checkView;

    public ItemShopSearchListAdapter(Context context, ArrayList<ShopSearchList> feedItemList, String checkView) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        this.checkView = checkView;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = null;
        if (checkView.equals("relatedItem")) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shop_related, parent, false);
        } else
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shop_search_list, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final ShopSearchList feedItem = feedItemList.get(i);
        SettingsMain.adforest_changeRattingBarcolor(customViewHolder.ratingBar, mContext);

        if (feedItem.getSalePrice().equals("") && feedItem.getRegPrice().equals("")) {
            customViewHolder.tv_discount.setVisibility(View.INVISIBLE);
            customViewHolder.tv_total_price.setVisibility(View.INVISIBLE);
        } else if (!feedItem.getSalePrice().equals("") && !feedItem.getRegPrice().equals("")) {
            customViewHolder.tv_discount.setPaintFlags(customViewHolder.tv_discount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            customViewHolder.tv_discount.setVisibility(View.VISIBLE);
            customViewHolder.tv_discount.setText(feedItem.getRegPrice());
            customViewHolder.tv_total_price.setVisibility(View.VISIBLE);
            customViewHolder.tv_total_price.setText(feedItem.getSalePrice());
        } else if (!feedItem.getRegPrice().equals("") && feedItem.getSalePrice().equals("")) {
            customViewHolder.tv_total_price.setVisibility(View.VISIBLE);
            customViewHolder.tv_total_price.setText(feedItem.getRegPrice());
            customViewHolder.tv_discount.setVisibility(View.INVISIBLE);

        }
        customViewHolder.tv_title.setText(feedItem.getTitle());
        customViewHolder.ratingBar.setRating(Float.parseFloat(Integer.toString(feedItem.getRatingStar())));
        customViewHolder.tv_totalReviews.setText(feedItem.getRatingText());

        if (!TextUtils.isEmpty(feedItem.getImage())) {
            Picasso.with(mContext).load(feedItem.getImage())
                    .resize(270, 270)
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.imageView);
        }
        View.OnClickListener listener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopSearchClickListener.onItemClick(feedItem);
            }
        };
        customViewHolder.listingLayout.setOnClickListener(listener2);
    }

    public void setOnItemClickListener(shopSearchClickListener onItemClickListener) {
        this.shopSearchClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return feedItemList.size();
    }


    public interface shopSearchClickListener {
        void onItemClick(ShopSearchList item);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tv_title, tv_total_price, tv_discount, tv_totalReviews;
        RatingBar ratingBar;
        RelativeLayout listingLayout;

        CustomViewHolder(View view) {
            super(view);
            this.imageView = view.findViewById(R.id.image_view);
            this.tv_title = view.findViewById(R.id.tv_title);
            this.tv_total_price = view.findViewById(R.id.tv_total_price);
            this.ratingBar = view.findViewById(R.id.ratingBar);
            this.tv_discount = view.findViewById(R.id.tv_discount);
            this.tv_totalReviews = view.findViewById(R.id.tv_totalReviews);
            this.listingLayout = view.findViewById(R.id.listingLayout);
        }
    }
}