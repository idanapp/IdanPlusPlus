package com.example.idan.plusplus.Dialogs;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.example.idan.plusplus.R;
import com.example.idan.plusplus.V2.App.WebapiSingleton;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AppUpdateDialog extends GuidedStepSupportFragment {
    private static final int ACTION_ID_POSITIVE = 1;
    private static final int ACTION_ID_NEGATIVE = ACTION_ID_POSITIVE + 1;
    private static File file;



    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        Bundle extras = getActivity().getIntent().getExtras();
        Integer oldVer = extras.getInt("OldVer",-1);
        Integer newVer = extras.getInt("NewVer",-1);
        file = (File)extras.get("urlToUpdateFile");
        GuidanceStylist.Guidance guidance = new GuidanceStylist.Guidance(getString(R.string.UPDATE_DIALOG_TITLE),
                getString(R.string.UPDATE_DIALOG_DESCRIPTION,String.valueOf(oldVer),String.valueOf(newVer)),
                "", null);
        return guidance;
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder(getContext())
                .id(ACTION_ID_POSITIVE)
                .title(R.string.UPDATE_DIALOG_BTN_UPDATE).build();
        actions.add(action);
        action = new GuidedAction.Builder(getContext())
                .id(ACTION_ID_NEGATIVE)
                .title(R.string.UPDATE_DIALOG_BTN_CANCEL).build();
        actions.add(action);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (ACTION_ID_POSITIVE == action.getId()) {
            if (WebapiSingleton.isTv) {
                final Uri uri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName()+".updateProvider", file);
                try {
                    Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Uri apkUri = uri;
                    install.setDataAndType(apkUri,"application/vnd.android.package-archive");
                    startActivityForResult(install,999);
                } catch (ActivityNotFoundException ex) {
                    Uri fileUri = Uri.fromFile(file); //for Build.VERSION.SDK_INT <= 24
                    if (Build.VERSION.SDK_INT >= 24) {
                        fileUri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName()+".updateProvider", file);
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                    intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //dont forget add this line
                    List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        getContext().grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    startActivityForResult(intent,999);
                }
            } else {
                Uri fileUri = Uri.fromFile(file); //for Build.VERSION.SDK_INT <= 24
                if (Build.VERSION.SDK_INT >= 24) {
                    fileUri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName()+".updateProvider", file);
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //dont forget add this line
                List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getContext().grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivityForResult(intent,999);
            }
        } else {

        }
        if (WebapiSingleton.isTv)
            System.exit(-1);
        else
            System.exit(0);
        //getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(outputDir,"idanplusplus.apk");
        if (file.exists())
            file.delete();
        if (requestCode == 999) {
            if (resultCode == RESULT_OK) {

            } else {

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }
}
