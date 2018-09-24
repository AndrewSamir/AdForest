package com.InternetSolutions.shopping.Shop;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.InternetSolutions.shopping.Shop.ShopDetails.ShopDetailActivity;
import com.InternetSolutions.shopping.Shop.adapters.ItemShopSearchListAdapter;
import com.InternetSolutions.shopping.Shop.models.ShopSearchList;
import com.InternetSolutions.shopping.utills.Network.RestService;
import com.InternetSolutions.shopping.utills.SettingsMain;
import com.InternetSolutions.shopping.utills.UrlController;

public class Shop_SearchListFragment extends Fragment {
    RecyclerView shopSearchRecylerView;
    ItemShopSearchListAdapter itemShopSearchListAdapter;
    ArrayList<ShopSearchList> shopSearchListArrayList = new ArrayList<>();
    RestService restService;
    SettingsMain settingsMain;
    Context context;
    Button btn_loadMore;
    int nextPage = 1;
    boolean hasNextPage = false;
    Spinner spinner;
    ShopSearchList shopSearchList = new ShopSearchList();
    TextView tv_show_count;
    String sort_order = "";

    public Shop_SearchListFragment() {
        // Required empty public constructor
    }

    public static void setBadgeCount(Context context, LayerDrawable icon, String count) {

        BadgeDrawable badge;

        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_badge);
        if (reuse != null && reuse instanceof BadgeDrawable) {
            badge = (BadgeDrawable) reuse;
        } else {
            badge = new BadgeDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_badge, badge);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_search_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        settingsMain = new SettingsMain(context);

        if (settingsMain.getAppOpen()) {
            restService = UrlController.createService(RestService.class);
        } else
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), getActivity());

        shopSearchRecylerView = view.findViewById(R.id.shopSearchRecylerView);
        btn_loadMore = view.findViewById(R.id.btn_loadMore);
        spinner = view.findViewById(R.id.spinner);
        tv_show_count = view.findViewById(R.id.tv_show_count);

        shopSearchRecylerView.setHasFixedSize(true);
        shopSearchRecylerView.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(shopSearchRecylerView, false);
        GridLayoutManager MyLayoutManager = new GridLayoutManager(getActivity(), 2);
        MyLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        shopSearchRecylerView.setLayoutManager(MyLayoutManager);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    adforest_sortSearch(shopSearchList.getSpinnerValue().get(position));
                    sort_order = shopSearchList.getSpinnerValue().get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasNextPage)
                    adforest_loadMoreShopItems();
            }
        });

        setHasOptionsMenu(true);
        adforest_loadShopItems();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.shop, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem action_cart = menu.findItem(R.id.action_cart);
        LayerDrawable icon = (LayerDrawable) action_cart.getIcon();
        setBadgeCount(context, icon, "9");
        action_cart.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                replaceFragment(new Shop_CartFragment(), "Shop_CartFragment");
                return true;
            }
        });
    }

    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    private void adforest_loadShopItems() {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());
            Call<ResponseBody> myCall = restService.getShopItemDetails(UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info ShopItems Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info ShopItems obj", "" + response.getJSONObject("data"));
                                JSONObject responseJsonObject = response.getJSONObject("data");
                                getActivity().setTitle(responseJsonObject.getString("page_title"));
                                tv_show_count.setText(responseJsonObject.getString("show_count"));
                                adforest_intiShopItemsList(responseJsonObject.getJSONArray("products"));

                                JSONObject jsonObjectPagination = responseJsonObject.getJSONObject("pagination");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");
                                nextPage = jsonObjectPagination.getInt("next_page");
                                if (hasNextPage) {
                                    btn_loadMore.setVisibility(View.VISIBLE);
                                    btn_loadMore.setText(responseJsonObject.getString("load_more_btn"));
                                }
                                shopSearchList.setSpinnerData(responseJsonObject.getJSONArray("sort_order"));
                                shopSearchList.setSpinnerValue(responseJsonObject.getJSONArray("sort_order"));

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, shopSearchList.getSpinnerData());
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info FeaturedMore ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info FeaturedMore err", String.valueOf(t));
                        Log.d("info FeaturedMore err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_intiShopItemsList(JSONArray jsonArray) {


        shopSearchListArrayList.clear();

        adforest_shopList(jsonArray);

        itemShopSearchListAdapter = new ItemShopSearchListAdapter(getContext(), shopSearchListArrayList, "");
        shopSearchRecylerView.setAdapter(itemShopSearchListAdapter);
        itemShopSearchListAdapter.setOnItemClickListener(new ItemShopSearchListAdapter.shopSearchClickListener() {
            @Override
            public void onItemClick(ShopSearchList item) {
                Intent intent = new Intent(getActivity(), ShopDetailActivity.class);
                intent.putExtra("product_id", item.getId());
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.right_enter, R.anim.left_out);
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
                shopSearchListModel.setRatingStar(jsonObject.getJSONObject("rating").getInt("stars"));
                shopSearchListModel.setRatingText(jsonObject.getJSONObject("rating").getString("text"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            shopSearchListArrayList.add(shopSearchListModel);
        }
    }

    private void adforest_loadMoreShopItems() {
        if (SettingsMain.isConnectingToInternet(getActivity())) {
            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("page_number", nextPage);
            if (!sort_order.equals(""))
                params.addProperty("sort_order", sort_order);

            Log.d("info loadMoreShop Load", params.toString());

            Call<ResponseBody> myCall = restService.getMoreShopItems(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info loadMoreShop Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info loadMoreShop obj", "" + response.getJSONObject("data"));

                                adforest_shopList(response.getJSONObject("data").getJSONArray("products"));
                                tv_show_count.setText(response.getJSONObject("data").getString("show_count"));

                                itemShopSearchListAdapter.notifyDataSetChanged();

                                JSONObject jsonObjectPagination = response.getJSONObject("data").getJSONObject("pagination");
                                nextPage = jsonObjectPagination.getInt("next_page");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                if (!hasNextPage)
                                    btn_loadMore.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info loadMoreShop ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info loadMoreShop err", String.valueOf(t));
                        Log.d("info loadMoreShop err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_sortSearch(String sort_order) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {
            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("sort_order", sort_order);
            Log.d("info sort_order Send", params.toString());

            Call<ResponseBody> myCall = restService.getMoreShopItems(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info sort_order Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info sort_order obj", "" + response.getJSONObject("data"));

                                tv_show_count.setText(response.getJSONObject("data").getString("show_count"));
                                adforest_intiShopItemsList(response.getJSONObject("data").getJSONArray("products"));
                                itemShopSearchListAdapter.notifyDataSetChanged();

                                JSONObject jsonObjectPagination = response.getJSONObject("data").getJSONObject("pagination");
                                nextPage = jsonObjectPagination.getInt("next_page");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                if (!hasNextPage)
                                    btn_loadMore.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info loadMoreShop ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info loadMoreShop err", String.valueOf(t));
                        Log.d("info loadMoreShop err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
        }
    }
}
