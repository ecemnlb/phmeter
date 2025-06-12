package com.example.phmeter.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;

public class EspClient {
    private static final OkHttpClient client = new OkHttpClient();

    public interface StringCallback {
        void onResult(String body);
    }

    public interface ImageCallback {
        void onResult(Bitmap bmp);
    }

    public static void get(String url, StringCallback cb) {
        Request req = new Request.Builder()
                .url(url)
                .header("Cache-Control", "no-cache")    // ← added
                .build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                cb.onResult(null);
            }
            @Override
            public void onResponse(Call call, Response resp) throws IOException {
                cb.onResult(resp.body() != null ? resp.body().string() : null);
            }
        });
    }

    public static void getImage(String url, ImageCallback cb) {
        Request req = new Request.Builder()
                .url(url)
                .header("Cache-Control", "no-cache")    // ← added
                .build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                cb.onResult(null);
            }
            @Override
            public void onResponse(Call call, Response resp) throws IOException {
                if (resp.body() == null) {
                    cb.onResult(null);
                    return;
                }
                InputStream is = resp.body().byteStream();
                Bitmap bmp = BitmapFactory.decodeStream(is);
                cb.onResult(bmp);
            }
        });
    }

}
