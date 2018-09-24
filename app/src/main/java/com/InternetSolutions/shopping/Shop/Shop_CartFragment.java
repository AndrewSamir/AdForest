package com.InternetSolutions.shopping.Shop;


import android.content.Context;
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
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Set;

import com.InternetSolutions.shopping.R;
import com.InternetSolutions.shopping.Shop.adapters.ItemShopCartAdapter;
import com.InternetSolutions.shopping.Shop.models.shopCartModel;
import com.InternetSolutions.shopping.utills.SettingsMain;

/**
 * A simple {@link Fragment} subclass.
 */
public class Shop_CartFragment extends Fragment {
    RecyclerView shorCartRecylerView;
    ItemShopCartAdapter itemShopCartAdapter;
    ArrayList<shopCartModel> shopCartModelArrayList = new ArrayList<>();
    SettingsMain settingsMain;
    private Context context;

    public Shop_CartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        shorCartRecylerView = view.findViewById(R.id.shopCartRecylerView);
        Log.d("info relova","asdsad");
        context=getContext();
        settingsMain=new SettingsMain(context);
        downtown_intiShopCart();
        setHasOptionsMenu(true);

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.shop, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem action_cart = menu.findItem(R.id.action_cart);
        action_cart.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SettingsMain.reload(context,"Shop_CartFragment");
//                replaceFragment(new Shop_CartFragment(), "Shop_CartFragment");
                return true;
            }
        });


    }
    private void downtown_intiShopCart() {
        shorCartRecylerView.setHasFixedSize(true);
        shorCartRecylerView.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(shorCartRecylerView, false);
        GridLayoutManager MyLayoutManager = new GridLayoutManager(getActivity(), 1);
        MyLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        shorCartRecylerView.setLayoutManager(MyLayoutManager);

        shopCartModelArrayList.clear();
        for (int i = 0; i < 10; i++) {
            shopCartModel profileListingModel = new shopCartModel();
            profileListingModel.setCartId("id"+i);
            shopCartModelArrayList.add(profileListingModel);

        }
        itemShopCartAdapter = new ItemShopCartAdapter(getActivity(), shopCartModelArrayList);
        shorCartRecylerView.setAdapter(itemShopCartAdapter);
        itemShopCartAdapter.setOnItemClickListener(new ItemShopCartAdapter.ShopCartClickListener() {
            @Override
            public void onDeleteClick(shopCartModel item) {
                Toast.makeText(getContext(),item.getCartId(), Toast.LENGTH_SHORT).show();
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
}
