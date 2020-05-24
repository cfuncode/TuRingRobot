package com.cjz.turingrobot.util;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MyOk {

    private static final OkHttpClient client = new OkHttpClient().newBuilder().build();
    private static final MediaType mediaType = MediaType.parse("application/json");

    public static void post(String uri, String args, Callback callback) {
        RequestBody body = RequestBody.create(mediaType, args);
        Request request = new Request.Builder()
                .url(uri)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(callback);
    }
}
