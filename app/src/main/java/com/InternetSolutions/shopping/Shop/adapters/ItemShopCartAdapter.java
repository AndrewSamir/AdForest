package com.InternetSolutions.shopping.Shop.adapters;

/**
 * Created by taimu on 2/22/2018.
 */

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.InternetSolutions.shopping.R;
import com.InternetSolutions.shopping.Shop.models.shopCartModel;

public class ItemShopCartAdapter extends RecyclerView.Adapter<ItemShopCartAdapter.CustomViewHolder> {
    private ArrayList<shopCartModel> feedItemList;
    private Context mContext;
    private ShopCartClickListener shopCartClickListener;

    public ItemShopCartAdapter(Context context, ArrayList<shopCartModel> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_cart, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final shopCartModel feedItem = feedItemList.get(i);
        customViewHolder.tv_discount.setPaintFlags(customViewHolder.tv_discount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        View.OnClickListener listener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shopCartClickListener.onDeleteClick(feedItem);
            }
        };
        customViewHolder.img_cart_delete.setOnClickListener(listener2);
    }
    public void setOnItemClickListener(ShopCartClickListener onItemClickListener) {
        this.shopCartClickListener = onItemClickListener;
    }
    @Override
    public int getItemCount() {
        return feedItemList.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView image_view;
        TextView tv_shop_cart_title, tv_discount, tv_total_price, tv_total_cart_items;
        ImageButton img_minus_cart, img_plus_cart, img_cart_delete;

        CustomViewHolder(View view) {
            super(view);
            this.image_view = view.findViewById(R.id.image_view);
            this.tv_shop_cart_title = view.findViewById(R.id.tv_shop_cart_title);
            this.tv_discount = view.findViewById(R.id.tv_discount);
            this.tv_total_price = view.findViewById(R.id.tv_total_price);
            this.tv_total_cart_items = view.findViewById(R.id.tv_total_cart_items);
            this.img_minus_cart = view.findViewById(R.id.img_minus_cart);
            this.img_plus_cart = view.findViewById(R.id.img_plus_cart);
            this.img_cart_delete = view.findViewById(R.id.img_cart_delete);

        }
    }
    public interface ShopCartClickListener {
        void onDeleteClick(shopCartModel item);
    }
}