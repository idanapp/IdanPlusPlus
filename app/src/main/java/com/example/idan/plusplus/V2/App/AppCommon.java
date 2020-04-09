package com.example.idan.plusplus.V2.App;

import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.idan.plusplus.Utils;

public class AppCommon {

    public static Boolean CheckNullOrEmpty(String val) {
        if (null == val || val.isEmpty()) return true;
        return false;
    }


    public static void showCenterToast(String msg, int duration) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast toast = Toast.makeText(Utils.getAppContext(),msg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(25);
            toast.show();
        });

    }

}
