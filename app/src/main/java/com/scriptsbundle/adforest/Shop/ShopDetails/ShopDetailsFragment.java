package com.scriptsbundle.adforest.Shop.ShopDetails;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlResImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.scriptsbundle.adforest.R;
import com.scriptsbundle.adforest.Shop.adapters.ItemDetailsFeatureListAdapter;
import com.scriptsbundle.adforest.Shop.adapters.ItemShopReviewsAdapter;
import com.scriptsbundle.adforest.Shop.adapters.ItemShopSearchListAdapter;
import com.scriptsbundle.adforest.Shop.models.ShopSearchList;
import com.scriptsbundle.adforest.Shop.models.shopFeatureDetailsModel;
import com.scriptsbundle.adforest.Shop.models.shopReviewsModel;
import com.scriptsbundle.adforest.utills.Network.RestService;
import com.scriptsbundle.adforest.utills.SettingsMain;
import com.scriptsbundle.adforest.utills.UrlController;
import ss.com.bannerslider.banners.Banner;
import ss.com.bannerslider.banners.RemoteBanner;
import ss.com.bannerslider.views.BannerSlider;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShopDetailsFragment extends Fragment implements View.OnClickListener {

    ImageView imageArrowDescription, imageArrowFeature, imageArrowReviews;
    NestedScrollView nestedScrollView;
    RecyclerView shopFeaturesRecylerView, shopReviewsRecylerView, shopSearchRecylerView;
    ArrayList<shopFeatureDetailsModel> featureArrayList = new ArrayList<>();
    ArrayList<shopReviewsModel> reviewsModelArrayList = new ArrayList<>();
    ArrayList<String> ratingFromSelectValue = new ArrayList<>();
    ArrayList<String> ratingFromSelectKey = new ArrayList<>();
    ArrayList<ShopSearchList> shopSearchListArrayList = new ArrayList<>();

    ItemShopSearchListAdapter itemShopSearchListAdapter;
    ItemDetailsFeatureListAdapter itemDetailsFeatureListAdapter;
    ItemShopReviewsAdapter itemShopReviewsAdapter;
    RestService restService;
    SettingsMain settingsMain;
    Context context;
    BannerSlider banner_slider1;
    TextView tv_title, tv_reviews_rating_text, tv_salePrice, tv_RegularPrice, tv_shortDescKey, tv_shortDesc_value,
            tv_product_description_title, tv_product_description_value, tv_review_title, tv_ratingText, textArea_information,
            tv_releatedProducts, tv_reviews_title, tv_no_reviews, tv_quantity, tv_total_cart_items, tv_feature_text;
    RatingBar productRatingBar;
    LinearLayout shortDescLayout, ratingLayout, layoutCustomFields, reviewForum, selectReviewLayout;
    JSONObject responseDataObject, cart_section;
    List<Banner> bannerList;
    HtmlTextView tv_tags;
    SimpleRatingBar reviewRatingBar;
    Button btn_submitReview, btn_loadMore, addToCartButton;
    Spinner selectReview;
    EditText textArea_review;
    CardView reviewsLayout;
    ImageButton img_minus_cart, img_plus_cart;
    String product_id;
    String reviewType = "";
    private ExpandableLayout expandable_description, expandable_feature, expandableReviews;
    private RelativeLayout expandProductDescription, expandProductFeature, expandUserReviews, relatedProductsLayout;

    public ShopDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settingsMain = new SettingsMain(getContext());

        expandable_description = view.findViewById(R.id.expandable_description);
        expandable_feature = view.findViewById(R.id.expandable_feature);
        expandableReviews = view.findViewById(R.id.expandableReviews);

        expandProductFeature = view.findViewById(R.id.expandProductDescription);
        expandProductDescription = view.findViewById(R.id.expandProductFeature);
        expandUserReviews = view.findViewById(R.id.expandUserReviews);

        imageArrowDescription = view.findViewById(R.id.imageArrowDescription);
        imageArrowFeature = view.findViewById(R.id.imageArrowFeature);
        imageArrowReviews = view.findViewById(R.id.imageArrowReviews);

        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        shopFeaturesRecylerView = view.findViewById(R.id.shopFeaturesRecylerView);
        shopReviewsRecylerView = view.findViewById(R.id.shopReviewsRecylerView);

        expandProductDescription.setOnClickListener(this);
        expandProductFeature.setOnClickListener(this);
        expandUserReviews.setOnClickListener(this);

        context = getContext();
        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            product_id = bundle.getString("product_id", "0");
        }
        bannerList = new ArrayList<>();
        banner_slider1 = view.findViewById(R.id.banner_slider1);
        tv_title = view.findViewById(R.id.tv_title);
        productRatingBar = view.findViewById(R.id.productRatingBar);
        tv_reviews_rating_text = view.findViewById(R.id.tv_reviews_rating_text);
        tv_salePrice = view.findViewById(R.id.tv_salePrice);
        tv_RegularPrice = view.findViewById(R.id.tv_RegularPrice);
        tv_shortDescKey = view.findViewById(R.id.tv_shortDescKey);
        tv_shortDesc_value = view.findViewById(R.id.tv_shortDesc_value);
        shortDescLayout = view.findViewById(R.id.shortDescLayout);
        ratingLayout = view.findViewById(R.id.ratingLayout);
        tv_product_description_title = view.findViewById(R.id.tv_product_description_title);
        tv_product_description_value = view.findViewById(R.id.tv_product_description_value);
        tv_review_title = view.findViewById(R.id.tv_review_title);
        tv_ratingText = view.findViewById(R.id.tv_ratingText);
        tv_releatedProducts = view.findViewById(R.id.tv_releatedProducts);
        textArea_information = view.findViewById(R.id.textArea_information);
        layoutCustomFields = view.findViewById(R.id.layoutCustomFields);
        reviewForum = view.findViewById(R.id.reviewForum);
        selectReviewLayout = view.findViewById(R.id.selectReviewLayout);
        tv_tags = view.findViewById(R.id.tv_tags);
        reviewRatingBar = view.findViewById(R.id.reviewRatingBar);
        btn_submitReview = view.findViewById(R.id.btn_submitReview);
        selectReview = view.findViewById(R.id.selectReview);
        textArea_review = view.findViewById(R.id.textArea_review);
        reviewsLayout = view.findViewById(R.id.reviewsLayout);
        tv_reviews_title = view.findViewById(R.id.tv_reviews_title);
        btn_loadMore = view.findViewById(R.id.btn_loadMore);
        tv_no_reviews = view.findViewById(R.id.tv_no_reviews);
        tv_quantity = view.findViewById(R.id.tv_quantity);
        tv_feature_text = view.findViewById(R.id.tv_feature_text);
        tv_total_cart_items = view.findViewById(R.id.tv_total_cart_items);
        img_minus_cart = view.findViewById(R.id.img_minus_cart);
        img_plus_cart = view.findViewById(R.id.img_plus_cart);
        addToCartButton = view.findViewById(R.id.addToCartButton);
        relatedProductsLayout = view.findViewById(R.id.relatedProductsLayout);
        addToCartButton.setOnClickListener(this);
        img_minus_cart.setOnClickListener(this);
        img_plus_cart.setOnClickListener(this);
        btn_loadMore.setOnClickListener(this);

        btn_submitReview.setOnClickListener(this);
        shopSearchRecylerView = view.findViewById(R.id.shopSearchRecylerView);
        shopSearchRecylerView.setHasFixedSize(true);
        shopSearchRecylerView.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(shopSearchRecylerView, false);
        GridLayoutManager MyLayoutManager = new GridLayoutManager(context, 1);
        MyLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        shopSearchRecylerView.setLayoutManager(MyLayoutManager);

        selectReview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    reviewType = ratingFromSelectKey.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (settingsMain.getAppOpen()) {
            restService = UrlController.createService(RestService.class);
        } else
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), context);

        adforest_getData(product_id);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.expandProductDescription:
                if (expandable_description.isExpanded()) {
                    expandable_description.collapse();
                    imageArrowDescription.animate().rotation(0).start();
                } else {
                    imageArrowDescription.animate().rotation(180).start();
                    expandable_description.expand();
                    expandable_feature.collapse();
                    imageArrowFeature.animate().rotation(0).start();
                    expandableReviews.collapse();
                    imageArrowReviews.animate().rotation(0).start();
                }
                break;
            case R.id.expandProductFeature:
                if (expandable_feature.isExpanded()) {
                    expandable_feature.collapse();
                    imageArrowFeature.animate().rotation(0).start();
                } else {
                    imageArrowFeature.animate().rotation(180).start();
                    expandable_feature.expand();
                    expandableReviews.collapse();
                    imageArrowReviews.animate().rotation(0).start();
                    expandable_description.collapse();
                    imageArrowDescription.animate().rotation(0).start();
                }
                break;
            case R.id.expandUserReviews:
                if (expandableReviews.isExpanded()) {
                    expandableReviews.collapse();
                    imageArrowReviews.animate().rotation(0).start();
                } else {
                    imageArrowReviews.animate().rotation(180).start();
                    expandableReviews.expand();
                    expandable_description.collapse();
                    imageArrowDescription.animate().rotation(0).start();
                    expandable_feature.collapse();
                    imageArrowFeature.animate().rotation(0).start();
                }
                break;
            case R.id.img_minus_cart:
                int currentValue = Integer.parseInt(tv_total_cart_items.getText().toString());
                if (currentValue > 1) {
                    currentValue = currentValue - 1;
                    tv_total_cart_items.setText(currentValue + "");
                }
                break;
            case R.id.img_plus_cart:
                int currentValuePlus = Integer.parseInt(tv_total_cart_items.getText().toString());
                try {
                    if (currentValuePlus < cart_section.getInt("max_quantity")) {
                        currentValuePlus += 1;
                        tv_total_cart_items.setText(currentValuePlus + "");
                    }
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            case R.id.btn_submitReview:
                boolean b = true;
                if (textArea_review.getText().toString().trim().isEmpty()) {
                    textArea_review.setError("");
                    textArea_review.requestFocus();
                    b = false;
                }
                if (selectReview.getVisibility() == View.VISIBLE) {
                    if (selectReview.getSelectedItemPosition() == 0) {
                        selectReview.requestFocus();
                        View selectedView = selectReview.getSelectedView();
                        if (selectedView != null && selectedView instanceof TextView) {
                            selectReview.requestFocus();
                            TextView selectedTextView = (TextView) selectedView;
                            selectedTextView.setError("");
                            selectedTextView.setTextColor(Color.RED);
                        }
                        b = false;
                    }
                }
                if (b) {
                    adforest_submitReview();
                }
                break;
            case R.id.btn_loadMore:
                ReviewsFragment reviewsFragment = new ReviewsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("product_id", product_id);
                reviewsFragment.setArguments(bundle);
                replaceFragment(reviewsFragment, "ReviewsFragment");
                break;
        }
    }

    private void adforest_submitReview() {
        if (SettingsMain.isConnectingToInternet(context)) {

            SettingsMain.showDilog(context);
            JsonObject params = new JsonObject();
            params.addProperty("product_id", product_id);
            params.addProperty("stars", reviewRatingBar.getRating());
            params.addProperty("desc", textArea_review.getText().toString());
            if (selectReview.getVisibility() == View.VISIBLE)
                params.addProperty("type", reviewType);


            Log.d("info review post", params.toString());
            Call<ResponseBody> myCall = restService.postShopItemReviews(params, UrlController.AddHeaders(context));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info reviewPost Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Toast.makeText(context, response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                ReviewsFragment reviewsFragment = new ReviewsFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("product_id", product_id);
                                reviewsFragment.setArguments(bundle);
                                replaceFragment(reviewsFragment, "ReviewsFragment");

                            } else {
                                Toast.makeText(context, response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(context, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(context, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info productDetail ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info productDetail err", String.valueOf(t));
                        Log.d("info productDetail err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(context, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_getData(String product_id) {
        if (SettingsMain.isConnectingToInternet(context)) {

            SettingsMain.showDilog(context);
            JsonObject params = new JsonObject();
            params.addProperty("product_id", product_id);

            Log.d("info product post", params.toString());
            Call<ResponseBody> myCall = restService.getShopProductDetail(params, UrlController.AddHeaders(context));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info productDetail Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info productDetail obj", "" + response.getJSONObject("data"));
                                responseDataObject = response.getJSONObject("data");
                                cart_section = responseDataObject.getJSONObject("cart_section");
                                getActivity().setTitle(responseDataObject.getString("page_title"));
                                adforest_setAllViewsText();
                                adforest_reviewsSection();
                                if (responseDataObject.getBoolean("related_products_show")) {
                                    relatedProductsLayout.setVisibility(View.GONE);
                                    adforest_intiShopItemsList();
                                }
                                adforest_intiFeatureList(responseDataObject.getJSONArray("attributes"));

                            } else {
                                Toast.makeText(context, response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(context, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(context, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info productDetail ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info productDetail err", String.valueOf(t));
                        Log.d("info productDetail err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(context, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_setAllViewsText() {
        JSONObject jsonObject = null;
        try {
            jsonObject = responseDataObject.getJSONObject("product");
            tv_title.setText(jsonObject.getString("title"));
            SettingsMain.adforest_changeRattingBarcolor(productRatingBar, context);
            if (!jsonObject.getString("price").equals("")) {
                tv_RegularPrice.setVisibility(View.VISIBLE);
                tv_RegularPrice.setText(jsonObject.getString("price"));
            }
            if (!jsonObject.getString("sale_price").equals("")) {
                tv_salePrice.setPaintFlags(tv_salePrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tv_salePrice.setVisibility(View.VISIBLE);
                tv_salePrice.setText(jsonObject.getString("price"));
                tv_RegularPrice.setVisibility(View.VISIBLE);
                tv_RegularPrice.setText(jsonObject.getString("sale_price"));
            }

            JSONObject shortDescriptio = jsonObject.getJSONObject("short_desc");
            if (!shortDescriptio.getString("value").equals("")) ;
            {
                shortDescLayout.setVisibility(View.VISIBLE);
                tv_shortDescKey.setText(shortDescriptio.getString("key"));
                tv_shortDesc_value.setText(shortDescriptio.getString("value"));
            }

            bannerList.clear();
            if (jsonObject.getJSONArray("gallery").length() == 0) {
                bannerList.add(new RemoteBanner(jsonObject.getString("image")));
            } else {
                for (int i = 0; i < jsonObject.getJSONArray("gallery").length(); i++) {
                    bannerList.add(new RemoteBanner(jsonObject.getJSONArray("gallery").get(i).toString()));
                }
            }
            if (bannerList.size() > 0) {
                banner_slider1.setBanners(bannerList);
            }

            JSONObject ratingObject = responseDataObject.getJSONObject("rating");
            if (ratingObject.getBoolean("is_show")) {
                ratingLayout.setVisibility(View.VISIBLE);
                productRatingBar.setRating(Float.parseFloat(Integer.toString(ratingObject.getInt("stars"))));
                tv_reviews_rating_text.setText(ratingObject.getString("text"));
            }

            JSONObject jsonObjectDescription = jsonObject.getJSONObject("desc");
            tv_product_description_title.setText(jsonObjectDescription.getString("key"));
            tv_product_description_value.setText(jsonObjectDescription.getString("value"));

            layoutCustomFields.removeAllViews();
            JSONArray jsonObjectCustomFields = jsonObject.getJSONArray("short_features");
            if (jsonObjectCustomFields.length() > 0) {
                layoutCustomFields.setVisibility(View.VISIBLE);
                for (int item = 0; item < jsonObjectCustomFields.length(); item++) {
                    HtmlTextView htmlTextView = new HtmlTextView(context);
                    htmlTextView.setPadding(0, 0, 0, 10);
                    htmlTextView.setHtml("<b>" + jsonObjectCustomFields.getJSONObject(item).getString("key") + "</b> : " +
                            jsonObjectCustomFields.getJSONObject(item).getString("value"), new HtmlResImageGetter(htmlTextView));
                    htmlTextView.setTextColor(Color.BLACK);
                    layoutCustomFields.addView(htmlTextView);
                }
            }

            JSONObject jsonObjectTags = jsonObject.getJSONObject("tags");
            if (!jsonObjectTags.getString("value").equals("")) {
                tv_tags.setVisibility(View.VISIBLE);
                tv_tags.setHtml("<b><font color=\"black\">" + jsonObjectTags.getString("key") + "</b> : " +
                                jsonObjectTags.getString("value")
                        , new HtmlResImageGetter(tv_tags));
            }

            tv_quantity.setText(cart_section.getString("quantity_text"));
            addToCartButton.setText(cart_section.getString("text"));
            tv_total_cart_items.setText(cart_section.getInt("current_quantity") + "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void adforest_reviewsSection() {
        JSONObject jsonObject = null;
        try {
            jsonObject = responseDataObject;
            if (jsonObject.getBoolean("show_review_section")) {

                if (jsonObject.getJSONObject("product").getBoolean("reviews_allowed") && !settingsMain.getAppOpen()) {
                    reviewForum.setVisibility(View.VISIBLE);
                    JSONObject ratingFormObject = responseDataObject.getJSONObject("rating_details").getJSONObject("form").getJSONObject("stars");
                    tv_review_title.setText(responseDataObject.getJSONObject("rating_details").getJSONObject("form").getString("title"));
                    tv_ratingText.setText(ratingFormObject.getString("title"));
                    if (ratingFormObject.getBoolean("select_is_show")) {
                        selectReviewLayout.setVisibility(View.VISIBLE);
                        for (int ii = 0; ii < ratingFormObject.getJSONArray("select").length(); ii++) {
                            JSONArray jsonArraySelect = ratingFormObject.getJSONArray("select");
                            ratingFromSelectKey.add(jsonArraySelect.getJSONObject(ii).getString("key"));
                            ratingFromSelectValue.add(jsonArraySelect.getJSONObject(ii).getString("value"));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, ratingFromSelectValue);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        selectReview.setAdapter(adapter);
                        textArea_review.setHint(ratingFormObject.getString("textarea"));
                        btn_submitReview.setText(ratingFormObject.getString("btn"));
                    }
                }
                JSONObject ratingObject = jsonObject.getJSONObject("rating_details").getJSONObject("rating").getJSONObject("review_data");
                tv_reviews_title.setText(jsonObject.getJSONObject("rating_details").getJSONObject("rating").getString("title"));
                if (ratingObject.getBoolean("has_reviews")) {
                    reviewsLayout.setVisibility(View.VISIBLE);
                    if (ratingObject.getJSONObject("pagination").getBoolean("has_next_page")) {
                        btn_loadMore.setVisibility(View.VISIBLE);
                        btn_loadMore.setText(ratingObject.getString("load_more"));
                    }
                    adforest_intiReviewsList(ratingObject.getJSONArray("reviews"));
                } else {
                    reviewsLayout.setVisibility(View.VISIBLE);
                    tv_no_reviews.setText(ratingObject.getString("message"));
                    btn_loadMore.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adforest_intiFeatureList(JSONArray jsonArray) {

        try {
            tv_feature_text.setText(responseDataObject.getString("attributes_title"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        shopFeaturesRecylerView.setHasFixedSize(true);
        shopFeaturesRecylerView.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(shopFeaturesRecylerView, false);
        GridLayoutManager MyLayoutManager = new GridLayoutManager(context, 1);
        MyLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        shopFeaturesRecylerView.setLayoutManager(MyLayoutManager);

        featureArrayList.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            shopFeatureDetailsModel viewProfileModel = new shopFeatureDetailsModel();
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                viewProfileModel.setTextViewKey(jsonObject.getString("key"));
                viewProfileModel.setTextViewValue(jsonObject.getString("val"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            featureArrayList.add(viewProfileModel);

        }
        itemDetailsFeatureListAdapter = new ItemDetailsFeatureListAdapter(context, featureArrayList);
        shopFeaturesRecylerView.setAdapter(itemDetailsFeatureListAdapter);
    }

    private void adforest_intiReviewsList(JSONArray jsonArray) {

        shopReviewsRecylerView.setHasFixedSize(true);
        shopReviewsRecylerView.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(shopReviewsRecylerView, false);
        GridLayoutManager MyLayoutManager = new GridLayoutManager(context, 1);
        MyLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        shopReviewsRecylerView.setLayoutManager(MyLayoutManager);

        reviewsModelArrayList.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            shopReviewsModel shopReviewsModel = new shopReviewsModel();
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                shopReviewsModel.setReviewId(jsonObject.getString("comment_ID"));
                shopReviewsModel.setProfileImage(jsonObject.getString("user_img"));
                shopReviewsModel.setName(jsonObject.getString("comment_author"));
                shopReviewsModel.setDescription(jsonObject.getString("comment_content"));
                shopReviewsModel.setDate(jsonObject.getString("comment_date"));
                shopReviewsModel.setRating(Integer.toString(jsonObject.getInt("rating_stars")));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            reviewsModelArrayList.add(shopReviewsModel);

        }
        itemShopReviewsAdapter = new ItemShopReviewsAdapter(context, reviewsModelArrayList);
        shopReviewsRecylerView.setAdapter(itemShopReviewsAdapter);
    }

    private void adforest_intiShopItemsList() {
        JSONArray jsonArray = null;
        shopSearchListArrayList.clear();
        try {
            tv_releatedProducts.setText(responseDataObject.getString("related_products_title"));
            jsonArray = responseDataObject.getJSONArray("related_products");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adforest_shopList(jsonArray);

        itemShopSearchListAdapter = new ItemShopSearchListAdapter(context, shopSearchListArrayList, "relatedItem");
        shopSearchRecylerView.setAdapter(itemShopSearchListAdapter);
        itemShopSearchListAdapter.setOnItemClickListener(new ItemShopSearchListAdapter.shopSearchClickListener() {
            @Override
            public void onItemClick(ShopSearchList item) {
                adforest_getData(item.getId());
                nestedScrollView.scrollTo(0, 0);
            }
        });
    }

    private void adforest_shopList(JSONArray jsonArray) {

        for (int i = 0; i < jsonArray.length(); i++) {
            ShopSearchList shopSearchListModel = new ShopSearchList();
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                shopSearchListModel.setId(jsonObject.getString("ID"));
                shopSearchListModel.setTitle(jsonObject.getString("title"));
                shopSearchListModel.setImage(jsonObject.getString("thumbnail"));
                shopSearchListModel.setRegPrice(jsonObject.getString("reg_price"));
                shopSearchListModel.setSalePrice(jsonObject.getString("sale_price"));
                shopSearchListModel.setRatingStar(3);
                shopSearchListModel.setRatingText("3 revies");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            shopSearchListArrayList.add(shopSearchListModel);
        }
    }

    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }
}
