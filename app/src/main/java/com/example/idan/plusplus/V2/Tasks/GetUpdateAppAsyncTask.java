package com.example.idan.plusplus.V2.Tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Base64;

import androidx.fragment.app.FragmentActivity;

import com.example.idan.plusplus.Classes.MD5;
import com.example.idan.plusplus.R;
import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.App.WebapiSingleton;
import com.example.idan.plusplus.ui.SpinnerFragment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class GetUpdateAppAsyncTask extends AsyncTask<Void,Integer,List<Object>> {
    private final int fragmentId;
    private OnCheckForUpdateTaskCompleted listener;

    ProgressDialog mProgressDialog;

    //FragmentActivity fragmentActivity;
   // int fragmentId;
    private SpinnerFragment mSpinnerFragment;
    private final FragmentActivity activity;
    private PowerManager.WakeLock mWakeLock;


    public GetUpdateAppAsyncTask(FragmentActivity activity, int fragmentId, OnCheckForUpdateTaskCompleted listener)
    {
        this.listener = listener;
        this.activity = activity;
        this.fragmentId = fragmentId;
    }


    public static String getApkName(Context context) {
        String packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            String apk = ai.publicSourceDir;
            return apk;
        } catch (Throwable x) {
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        try {
            File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(outputDir,"idanplusplus.apk");
            if (file.exists())
                file.delete();
//            mSpinnerFragment = new SpinnerFragment();
//            activity.getSupportFragmentManager().beginTransaction().add(fragmentId, mSpinnerFragment).commit();
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage(activity.getString(R.string.CHECK_UPDATE_MSG));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                    activity.finish();
                }
            });
            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();

        } catch (Exception e) {
            activity.finish();
        }

    }

    @Override
    protected List<Object> doInBackground(Void... voids) {
        List<Object> returnList = new ArrayList<>();
        try {
            Integer serverVersion;
            Integer localVersion;
            String baseUrl = Utils.getBaseUrlEmpty();
            String versionUrl = Utils.getUpdateVersionUrl();
            String serverVersionEncoded = Utils.getUpdateSoftwareService(null).UpdateSoftware(versionUrl).blockingFirst();
            String versionStr = new String(Base64.decode(serverVersionEncoded,Base64.NO_WRAP));
            String[] versions = versionStr.split("\\@");
            String apk = getApkName(activity.getApplicationContext());
            if (MD5.checkMD5(versions[0],new File(apk))) {
                return returnList;
            } else {
                serverVersion = Integer.parseInt(versions[1]);
                localVersion = Utils.getVersionCode(activity);
                String getAppUrl = Utils.getAppUrl();

                URL url = new URL(getAppUrl);
                URLConnection conection = url.openConnection();
                conection.connect();


//                Response<ResponseBody> appUrlResponse = updateSoftwareService.getHtml(getAppUrl).execute();
                File file = null;
                if (WebapiSingleton.isTv) {
                    File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    try {
                        file = new File(outputDir, "idanplusplus.apk");
                    } catch (Exception e) {
                        e.printStackTrace();
                        file = null;
                    }
                } else {
                    File outputDir =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    try {
                        file = new File(outputDir, "idanplusplus.apk");
                    } catch (Exception e) {
                        e.printStackTrace();
                        file = null;
                    }
                }
                //InputStream input = appUrlResponse.body().byteStream();
                InputStream input = new BufferedInputStream(url.openStream(),8192);
                try {
                   // long fileLength = appUrlResponse.body().contentLength();
                    int fileLength = conection.getContentLength();
                    OutputStream output = new FileOutputStream(file);
                    try {
                        byte[] buffer = new byte[4096]; // or other buffer size
                        long total = 0;
                        int read;
                        while ((read = input.read(buffer)) != -1) {
                            total += read;
                            if (isCancelled()) {
                                input.close();
                                returnList.add(-2);
                                returnList.add(-2);
                                returnList.add(null);
                                return returnList;
                            }
                            // publishing the progress....
                            if (fileLength > 0) // only if total length is known
                                publishProgress((int) (total * 100 / fileLength));
                            output.write(buffer, 0, read);
                        }
                        output.flush();
                    } finally {
                        output.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    file = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    file = null;
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        file = null;
                    }
                }
                returnList.add(localVersion);
                returnList.add(serverVersion);
                returnList.add(file);
            }
        } catch (Exception e) {
            returnList.add(-2);
            returnList.add(-2);
            returnList.add(null);
        }
        return  returnList;
    }


    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
        mProgressDialog.setMessage(activity.getString(R.string.UPDATE_PROGRESS_MESSAGE));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }


    @Override
    protected void onPostExecute(List<Object> list) {
        //activity.getSupportFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
        mWakeLock.release();
        mProgressDialog.dismiss();
        if (list.size() <= 0)
            this.listener.onCheckForUpdateTaskCompleted(-1,-1,null);
        else {
            int localVer = -2;
            int serverVer = -2;
            File downloadFile = null;
            if (list.get(0) != null && list.get(0) instanceof Integer)
                localVer = (int)list.get(0);
            if (list.get(1) != null && list.get(1) instanceof  Integer)
                serverVer = (int)list.get(1);
            if (list.get(2) != null && list.get(2) instanceof  File)
                downloadFile = (File)list.get(2);

            this.listener.onCheckForUpdateTaskCompleted(localVer,serverVer,downloadFile);
        }
        super.onPostExecute(list);
    }
}
