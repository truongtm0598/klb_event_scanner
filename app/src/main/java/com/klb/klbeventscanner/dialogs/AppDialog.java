package com.klb.klbeventscanner.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

public class AppDialog{
    public static void showDialog(final Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(message);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Sử dụng Handler để đóng dialog sau khoảng thời gian
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, 5000); // 5 giây
    }
}