package com.InternetSolutions.shopping.Shop.ShopDetails;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.InternetSolutions.shopping.R;
import com.InternetSolutions.shopping.Shop.adapters.ItemShopReviewsAdapter;
import com.InternetSolutions.shopping.Shop.models.shopReviewsModel;
import com.InternetSolutions.shopping.utills.Network.RestService;
import com.InternetSolutions.shopping.utills.SettingsMain;
import com.InternetSolutions.shopping.utills.UrlController;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewsFragment extends Fragment {
    RecyclerView shopReviewsRecylerView;
    TextView tv_no_reviews;
    Button btn_loadMore;
    ItemShopReviewsAdapter itemShopReviewsAdapter;
    RestService restService;
    SettingsMain settingsMain;
    Context context;
    String product_id;
    ArrayList<shopReviewsModel> reviewsModelArrayList = new ArrayList<>();
    int nextPage = 1;
    boolean has_next_page = false;

    public ReviewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        settingsMain = new SettingsMain(context);
        shopReviewsRecylerView = view.findViewById(R.id.shopReviewsRecylerView);
        shopReviewsRecylerView.setHasFixedSize(true);
        shopReviewsRecylerView.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(shopReviewsRecylerView, false);
        GridLayoutManager MyLayoutManager = new GridLayoutManager(context, 1);
        MyLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        shopReviewsRecylerView.setLayoutManager(MyLayoutManager);


        tv_no_reviews = view.findViewById(R.id.tv_no_reviews);
        btn_loadMore = view.findViewById(R.id.btn_loadMore);
        btn_loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adforest_loadMoreReviews();
            }
        });

        if (settingsMain.getAppOpen()) {
            restService = UrlController.createService(RestService.class);
        } else
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), context);
        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            product_id = bundle.getString("product_id", "0");
        }
        adforest_getReviews();
    }

    private void adforest_loadMoreReviews() {

        if (SettingsMain.isConnectingToInternet(context)) {

            SettingsMain.showDilog(context);
            JsonObject params = new JsonObject();
            params.addProperty("product_id", product_id);
            params.addProperty("page_number", nextPage);

            Log.d("info shopReviews post", params.toString());
            Call<ResponseBody> myCall = restService.getShopItemReviews(params, UrlController.AddHeaders(context));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info shopReviews Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info shopReviews obj", "" + response.getJSONObject("data"));
                                JSONObject jsonObject = response.getJSONObject("data").getJSONObject("review_data");
                                getActivity().setTitle(response.getJSONObject("data").getString("page_title"));
                                adforest_ReviewsList(jsonObject.getJSONArray("reviews"));
                                nextPage = jsonObject.getJSONObject("pagination").getInt("next_page");
                                has_next_page = jsonObject.getJSONObject("pagination").getBoolean("has_next_page");
                                if (!has_next_page) {
                                    btn_loadMore.setVisibility(View.GONE);
                                }
                                itemShopReviewsAdapter.notifyDataSetChanged();

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

    private void adforest_getReviews() {
        if (SettingsMain.isConnectingToInternet(context)) {

            SettingsMain.showDilog(context);
            JsonObject params = new JsonObject();
            params.addProperty("product_id", product_id);

            Log.d("info shopReviews post", params.toString());
            Call<ResponseBody> myCall = restService.getShopItemReviews(params, UrlController.AddHeaders(context));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info shopReviews Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info shopReviews obj", "" + response.getJSONObject("data"));
                                JSONObject jsonObject = response.getJSONObject("data").getJSONObject("review_data");
                                getActivity().setTitle(response.getJSONObject("data").getString("page_title"));
                                adforest_intiReviewsList(jsonObject.getJSONArray("reviews"));
                                nextPage = jsonObject.getJSONObject("pagination").getInt("next_page");
                                has_next_page = jsonObject.getJSONObject("pagination").getBoolean("has_next_page");
                                if (has_next_page) {
                                    btn_loadMore.setVisibility(View.VISIBLE);
                                    btn_loadMore.setText(jsonObject.getString("load_more"));
                                }

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

    private void adforest_ReviewsList(JSONArray jsonArray) {

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
    }

    private void adforest_intiReviewsList(JSONArray jsonArray) {

        reviewsModelArrayList.clear();
        adforest_ReviewsList(jsonArray);
        itemShopReviewsAdapter = new ItemShopReviewsAdapter(context, reviewsModelArrayList);
        shopReviewsRecylerView.setAdapter(itemShopReviewsAdapter);
    }
}
