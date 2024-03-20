package com.klb.klbeventscanner.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.klb.klbeventscanner.dialogs.AppDialog;

public class NetworkUtils {
    private Context context;

    public NetworkUtils(Context context) {
        this.context = context.getApplicationContext();
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }
}
