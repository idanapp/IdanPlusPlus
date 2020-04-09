package com.example.idan.plusplus.Dialogs;

import android.os.Bundle;

import com.example.idan.plusplus.R;
import com.example.idan.plusplus.ui.LeanbackActivity;

public class AppUpdateDialogActivity extends LeanbackActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_update_dialog_activity);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
