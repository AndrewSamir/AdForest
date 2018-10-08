package com.InternetSolutions.shopping.messages;


import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.InternetSolutions.shopping.home.HomeActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import com.InternetSolutions.shopping.Notification.Config;
import com.InternetSolutions.shopping.R;
import com.InternetSolutions.shopping.messages.adapter.ChatAdapter;
import com.InternetSolutions.shopping.modelsList.ChatMessage;
import com.InternetSolutions.shopping.modelsList.ChatTyping;
import com.InternetSolutions.shopping.utills.AnalyticsTrackers;
import com.InternetSolutions.shopping.utills.Network.RestService;
import com.InternetSolutions.shopping.utills.SettingsMain;
import com.InternetSolutions.shopping.utills.UrlController;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment implements View.OnClickListener
{

    private EditText msg_edittext;
    ArrayList<ChatMessage> chatlist;

    ChatAdapter chatAdapter;
    ListView msgListView;
    int nextPage = 1;
    boolean hasNextPage = false;
    String adId, senderId, recieverId, type;
    SettingsMain settingsMain;

    TextView adName, adPrice, adDate, tv_typing;
    SwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    RestService restService;
    ChatTyping chatTypingModel;
    String userId;
    boolean typingStarted;
    int totalCount;
    FirebaseDatabase database, database2;
    DatabaseReference myRef, myRef2;
    long delay = 10000; // 1 seconds after user stops typing
    long last_text_edit = 0;
    Handler handler = new Handler();
    String userName;
    String typingRecieverId;
    String typingText;
    TextView tv_chatTtile, tv_online;

    public ChatFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_chat_layout, container, false);
        settingsMain = new SettingsMain(getActivity());
//        database = FirebaseDatabase.getInstance();
//        myRef = database.getReference("chatTyping");

        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            adId = bundle.getString("adId", "0");
            senderId = bundle.getString("senderId", "0");
            recieverId = bundle.getString("recieverId", "0");
            type = bundle.getString("type", "0");
        }

        adDate = view.findViewById(R.id.verified);
        adName = view.findViewById(R.id.loginTime);
        adPrice = view.findViewById(R.id.text_viewName);
        tv_typing = view.findViewById(R.id.tv_typing);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        msg_edittext = view.findViewById(R.id.messageEditText);
        msgListView = view.findViewById(R.id.msgListView);
        tv_chatTtile = getActivity().findViewById(R.id.tv_chatTtile);
        tv_online = getActivity().findViewById(R.id.tv_online);
        ImageButton sendButton = view.findViewById(R.id.sendMessageButton);
        sendButton.setOnClickListener(this);

        ImageView img_attach_message = view.findViewById(R.id.img_attach_message);
        img_attach_message.setOnClickListener(this);

        sendButton.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
        // ----Set autoscroll of listview when a new message arrives----//
        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);
        restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), getActivity());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (hasNextPage)
                {
                    swipeRefreshLayout.setRefreshing(true);
                    msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
                    msgListView.setStackFromBottom(false);
                    adforest_loadMore(nextPage);
                } else
                {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        chatlist = new ArrayList<>();
        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE))
                {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    Toast.makeText(getActivity(), "there", Toast.LENGTH_SHORT).show();
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);


                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION))
                {

                    String date = intent.getStringExtra("date");
                    String img = intent.getStringExtra("img");
                    String text = intent.getStringExtra("text");
                    String type = intent.getStringExtra("type");

                    String adIdCheck = intent.getStringExtra("adIdCheck");
                    String recieverIdCheck = intent.getStringExtra("recieverIdCheck");
                    String senderIdCheck = intent.getStringExtra("senderIdCheck");

                    if (adId.equals(adIdCheck) && recieverId.equals(recieverIdCheck)
                            && senderId.equals(senderIdCheck))
                    {
                        Log.d("Instant Message", "true");
                        ChatMessage item = new ChatMessage();
                        item.setImage(img);
                        item.setBody(text);
                        item.setDate(date);
                        item.setMine(type.equals("message"));
                        chatlist.add(item);
                        msgListView.setAdapter(chatAdapter);
                        chatAdapter.notifyDataSetChanged();
                    } else
                    {
                        Log.d("Instant Message", adIdCheck + recieverIdCheck + senderIdCheck);
                        Log.d("Instant Message", adId + senderId + recieverId);

                    }
                }
            }
        };
        adforest_getChat();
//        adforest_typingIndicatoor();
//        adforest_checkLogin();
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                tv_online.setVisibility(View.GONE);
//                adforest_checkLogin();
//            }
//        }, 10000);
        return view;
    }
//
//    private void adforest_checkLogin() {
//        database2 = FirebaseDatabase.getInstance();
//        myRef2 = database2.getReference("UserLogin");
//        String otherUserId;
//        if (type.equals("receive"))
//            otherUserId = senderId;
//        else
//            otherUserId = recieverId;
//
//        Log.d("info",otherUserId);
//
//        myRef2.child(otherUserId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                ChatUserModel chatTypingModel = dataSnapshot.getValue(ChatUserModel.class);
//                // Check for null
//                if (chatTypingModel == null) {
//                    Log.e("info", "ChatTyping data is null!");
//                    tv_online.setVisibility(View.GONE);
//                } else {
//                    if (chatTypingModel.isOnline()) {
//                        tv_online.setVisibility(View.VISIBLE);
//                        tv_online.setText("online");
//                    } else
//                        tv_online.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w("info ", "Failed to read value.", error.toException());
//            }
//        });
//    }
//
//    private void adforest_typingIndicatoor() {
//        try {
//            if (type.equals("receive")) {
//                userId = recieverId;
//                typingRecieverId = senderId;
//            } else {
//                userId = senderId;
//                typingRecieverId = recieverId;
//            }
//            final Runnable input_finish_checker = new Runnable() {
//                public void run() {
//                    if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
//                        // TODO: do what you need here
//                        // ............
//                        // ............
////                        DoStaff();
//                        chatTypingModel = new ChatTyping(adId, senderId, recieverId, type, "", false);
//                        myRef.child(adId).child(userId).setValue(chatTypingModel);
//                        Log.d("info stoped ", "dsadsad");
//                    }
//                }
//            };
//            msg_edittext.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length() == 1) {
//
//                        typingStarted = true;
//
//                    } else if (s.toString().trim().length() == 0 && typingStarted) {
//                        typingStarted = false;
//                    }
//                    chatTypingModel = new ChatTyping(adId, senderId, recieverId, type, s.toString(), typingStarted);
//                    myRef.child(adId).child(userId).setValue(chatTypingModel);
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    if (s.length() > 0) {
//                        last_text_edit = System.currentTimeMillis();
//                        handler.postDelayed(input_finish_checker, delay);
//                    } else {
//
//                    }
//                }
//            });
//
//            myRef.child(adId).child(typingRecieverId).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    ChatTyping chatTypingModel = dataSnapshot.getValue(ChatTyping.class);
//                    // Check for null
//                    if (chatTypingModel == null) {
//                        Log.e("info", "ChatTyping data is null!");
//                        return;
//                    } else {
//                        if (userName != null) {
//                            String s = userName + " " + typingText + " ....";
//                            if (chatTypingModel.text.length() > 0 && chatTypingModel.type.equals("sent") &&
//                                    settingsMain.getUserLogin().equals(chatTypingModel.recieverId)) {
//                                tv_typing.setVisibility(View.VISIBLE);
//                                tv_typing.setText(s);
//                            }
//                            if (chatTypingModel.text.length() > 0 && chatTypingModel.type.equals("receive") &&
//                                    settingsMain.getUserLogin().equals(chatTypingModel.senderId)) {
//                                tv_typing.setVisibility(View.VISIBLE);
//                                tv_typing.setText(s);
//                            }
//                        }
//                        if (!chatTypingModel.typing && chatTypingModel.text.isEmpty()) {
//                            tv_typing.setVisibility(View.GONE);
//                        }
//                        Log.d("info ", chatTypingModel.type + "" + chatTypingModel.senderId + "" + typingRecieverId);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError error) {
//                    // Failed to read value
//                    Log.w("info ", "Failed to read value.", error.toException());
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void adforest_getChat()
    {

        if (SettingsMain.isConnectingToInternet(getActivity()))
        {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("ad_id", adId);
            params.addProperty("sender_id", senderId);
            params.addProperty("receiver_id", recieverId);
            params.addProperty("type", type);

            Log.d("info sendChat object", "" + params.toString());

            Call<ResponseBody> myCall = restService.postGetChatORLoadMore(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>()
            {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj)
                {
                    try
                    {
                        if (responseObj.isSuccessful())
                        {
                            Log.d("info Chat Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success"))
                            {
                                Log.d("info Chat object", "" + response.getJSONObject("data"));

                                getActivity().setTitle("");
                                tv_chatTtile.setText(response.getJSONObject("data").getString("page_title"));
                                userName = response.getJSONObject("data").getString("page_title");

                                JSONObject jsonObjectPagination = response.getJSONObject("data").getJSONObject("pagination");

                                adName.setText(response.getJSONObject("data").getString("ad_title"));
                                adPrice.setText(response.getJSONObject("data").getJSONObject("ad_price").getString("price"));
                                adDate.setText(response.getJSONObject("data").getString("ad_date"));
                                typingText = response.getJSONObject("data").getString("is_typing");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                adforest_intList(response.getJSONObject("data").getJSONArray("chat"));

                                chatAdapter = new ChatAdapter(getActivity(), chatlist);
                                msgListView.setAdapter(chatAdapter);

                            } else
                            {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e)
                    {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t)
                {
                    if (t instanceof TimeoutException)
                    {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException)
                    {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException)
                    {
                        Log.d("info Chat Exception ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else
                    {
                        SettingsMain.hideDilog();
                        Log.d("info Chat error", String.valueOf(t));
                        Log.d("info Chat error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else
        {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_loadMore(int nextPag)
    {

        if (SettingsMain.isConnectingToInternet(getActivity()))
        {

            JsonObject params = new JsonObject();
            params.addProperty("ad_id", adId);
            params.addProperty("sender_id", senderId);
            params.addProperty("receiver_id", recieverId);
            params.addProperty("type", type);

            params.addProperty("page_number", nextPag);

            Log.d("info LoadMore Chat", "" + params.toString());

            Call<ResponseBody> myCall = restService.postGetChatORLoadMore(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>()
            {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj)
                {
                    try
                    {
                        if (responseObj.isSuccessful())
                        {
                            Log.d("info LoadChat Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success"))
                            {
                                Log.d("info LoadChat object", "" + response.getJSONObject("data"));

                                JSONObject jsonObjectPagination = response.getJSONObject("data").getJSONObject("pagination");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                JSONArray jsonArray = (response.getJSONObject("data").getJSONArray("chat"));

                                Collections.reverse(chatlist);

                                try
                                {
                                    for (int i = 0; i < jsonArray.length(); i++)
                                    {
                                        ChatMessage item = new ChatMessage();
                                        item.setImage(jsonArray.getJSONObject(i).getString("img"));
                                        item.setBody(jsonArray.getJSONObject(i).getString("text"));
                                        item.setDate(jsonArray.getJSONObject(i).getString("date"));
                                        item.setMine(jsonArray.getJSONObject(i).getString("type").equals("message"));

                                        chatlist.add(item);
                                    }

                                } catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                                Collections.reverse(chatlist);
                                msgListView.setAdapter(chatAdapter);
                                chatAdapter.notifyDataSetChanged();

                            } else
                            {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    } catch (JSONException e)
                    {
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    SettingsMain.hideDilog();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t)
                {
                    if (t instanceof TimeoutException)
                    {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException)
                    {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException)
                    {
                        Log.d("info LoadChat Excptn ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    } else
                    {
                        SettingsMain.hideDilog();
                        Log.d("info LoadChat error", String.valueOf(t));
                        Log.d("info LoadChat error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        } else
        {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }


    }

    private void adforest_intList(JSONArray jsonArray)
    {
        chatlist.clear();
        try
        {
            for (int i = 0; i < jsonArray.length(); i++)
            {
                ChatMessage item = new ChatMessage();
                item.setBody(jsonArray.getJSONObject(i).getString("text"));
                item.setImage(jsonArray.getJSONObject(i).getString("img"));
                item.setDate(jsonArray.getJSONObject(i).getString("date"));
                item.setMine(jsonArray.getJSONObject(i).getString("type").equals("message"));

                chatlist.add(item);
            }
            Collections.reverse(chatlist);


        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
    }

    public void adforest_sendTextMessage()
    {

        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);

        String message = msg_edittext.getEditableText().toString();
        msg_edittext.setText("");
        if (!message.equalsIgnoreCase(""))
        {

            if (SettingsMain.isConnectingToInternet(getActivity()))
            {

                SettingsMain.showDilog(getActivity());

                JsonObject params = new JsonObject();
                params.addProperty("ad_id", adId);
                params.addProperty("sender_id", senderId);
                params.addProperty("receiver_id", recieverId);
                params.addProperty("type", type);
                params.addProperty("message", message);


                Log.d("info sendMessage Object", "" + params.toString());

                Call<ResponseBody> myCall = restService.postSendMessage(params, UrlController.AddHeaders(getActivity()));
                myCall.enqueue(new Callback<ResponseBody>()
                {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj)
                    {
                        try
                        {
                            if (responseObj.isSuccessful())
                            {
                                Log.d("info sendMessage Resp", "" + responseObj.toString());

                                JSONObject response = new JSONObject(responseObj.body().string());
                                if (response.getBoolean("success"))
                                {
                                    Log.d("info sendMessage object", "" + response.getJSONObject("data"));

                                    adforest_intList(response.getJSONObject("data").getJSONArray("chat"));

                                    chatAdapter = new ChatAdapter(getActivity(), chatlist);
                                    msgListView.setAdapter(chatAdapter);

                                    msg_edittext.setText("");
                                } else
                                {
                                    Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            SettingsMain.hideDilog();
                        } catch (JSONException e)
                        {
                            SettingsMain.hideDilog();
                            e.printStackTrace();
                        } catch (IOException e)
                        {
                            SettingsMain.hideDilog();
                            e.printStackTrace();
                        }
                        SettingsMain.hideDilog();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t)
                    {
                        if (t instanceof TimeoutException)
                        {
                            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                            SettingsMain.hideDilog();
                        }
                        if (t instanceof SocketTimeoutException || t instanceof NullPointerException)
                        {

                            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                            SettingsMain.hideDilog();
                        }
                        if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException)
                        {
                            Log.d("info sendMessage", "NullPointert Exception" + t.getLocalizedMessage());
                            SettingsMain.hideDilog();
                        } else
                        {
                            SettingsMain.hideDilog();
                            Log.d("info sendMessage error", String.valueOf(t));
                            Log.d("info sendMessage error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                        }
                    }
                });
            } else
            {
                SettingsMain.hideDilog();
                Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
            }


        }
    }

    public void adforest_sendAttachedMessage(String path)
    {

        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);


        if (SettingsMain.isConnectingToInternet(getActivity()))
        {

            SettingsMain.showDilog(getActivity());

//                   Log.d("info sendMessage Object", "" + params.toString());

            Uri uri = Uri.parse(path);

            Call<ResponseBody> myCall = restService.attachFile(prepareFilePart("chatf", uri), UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>()
            {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj)
                {
                    try
                    {
                        if (responseObj.isSuccessful())
                        {
                            Log.d("info sendMessage Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success"))
                            {
                                Log.d("info sendMessage object", "" + response.getJSONObject("data"));

                                adforest_intList(response.getJSONObject("data").getJSONArray("chat"));

                                chatAdapter = new ChatAdapter(getActivity(), chatlist);
                                msgListView.setAdapter(chatAdapter);

                                msg_edittext.setText("");
                            } else
                            {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e)
                    { 
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t)
                {
                    if (t instanceof TimeoutException)
                    {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException)
                    {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException)
                    {
                        Log.d("info sendMessage", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else
                    {
                        SettingsMain.hideDilog();
                        Log.d("info sendMessage error", String.valueOf(t));
                        Log.d("info sendMessage error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else
        {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }


    }


    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri)
    {

        File file = new File(getRealPathFromUri(fileUri));
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private String getRealPathFromUri(final Uri uri)
    {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(getActivity(), uri))
        {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type))
                {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri))
            {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(getActivity(), contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type))
                {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type))
                {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type))
                {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(getActivity(), contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme()))
        {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(getActivity(), uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme()))
        {
            return uri.getPath();
        }

        return null;
    }

    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs)
    {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try
        {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst())
            {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally
        {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    private boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isGooglePhotosUri(Uri uri)
    {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.sendMessageButton:

                adforest_sendTextMessage();
                break;

            case R.id.img_attach_message:
                attachFile();
                break;
        }
    }


    @Override
    public void onResume()
    {
        try
        {
            if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals(""))
                AnalyticsTrackers.getInstance().trackScreenView("Chat Box");
            super.onResume();
        } catch (IllegalStateException e)
        {
            e.printStackTrace();
        }

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

    }

    @Override
    public void onPause()
    {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }

    private void attachFile()
    {

        Intent intent = new Intent(getActivity(), FilePickerActivity.class);
        intent.putExtra(com.jaiselrahman.filepicker.activity.FilePickerActivity.CONFIGS, new Configurations.Builder()
                .setCheckPermission(true)
                .setShowImages(true)
                .enableImageCapture(true)
                .setShowFiles(true)
                .setMaxSelection(1)
                .setSkipZeroSizeFiles(true)
                .build());
        startActivityForResult(intent, 20);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {

            case 20:
                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
                adforest_sendAttachedMessage(files.get(0).getPath());
                break;
        }
    }
}
