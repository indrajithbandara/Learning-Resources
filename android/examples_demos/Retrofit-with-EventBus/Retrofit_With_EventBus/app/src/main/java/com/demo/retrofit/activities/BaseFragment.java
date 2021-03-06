package com.demo.retrofit.activities;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.demo.retrofit.RetroFitApp;
import com.demo.retrofit.network.ApiClient;
import com.demo.retrofit.utils.PermissionResult;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This BaseFragment class is mainly used for avoiding most of the boiler-plate code.
 */
public class BaseFragment extends Fragment {

    private int KEY_PERMISSION = 0;
    private PermissionResult permissionResult;
    private String permissionsAsk[];

    private EventBus mEventBus;
    protected ApiClient mApiClient;

    ProgressDialog dialog;
    private Handler handler;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEventBus = EventBus.getDefault();
        mApiClient = getApp().getApiClient();

        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    public RetroFitApp getApp() {
        return (RetroFitApp) getActivity().getApplication();
    }

    /**
     * Shows toast message.
     *
     * @param msg
     */
    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Initialize Loading Dialog
     */
    protected void initDialog(Context context) {
        dialog = new ProgressDialog(context); // this or YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        handler = new Handler();
    }

    protected void dismissProgress() {
        if (handler != null && dialog != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            });
        }
    }

    protected void showProgress() {
        if (handler != null && dialog != null) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
//        hideKeyboard(edt);
                }
            });
        }
    }


    /**
     * Check if the Application required Permission is granted.
     *
     * @param context
     * @param permission
     * @return
     */
    @SuppressWarnings({"MissingPermission"})
    public boolean isPermissionGranted(Context context, String permission) {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) ||
               (ActivityCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Check if the Application required Permissions are granted.
     *
     * @param context
     * @param permissions
     * @return
     */
    public boolean isPermissionsGranted(Context context, String permissions[]) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        boolean granted = true;
        for (String permission : permissions) {
            if (!(ActivityCompat.checkSelfPermission(
                    context, permission) == PackageManager.PERMISSION_GRANTED))
                granted = false;
        }

        return granted;
    }

    /**
     * Ask Permission to be granted.
     *
     * @param permissionAsk
     */
    private void internalRequestPermission(String[] permissionAsk) {
        String arrayPermissionNotGranted[];
        ArrayList<String> permissionsNotGranted = new ArrayList<>();

        for (String aPermissionAsk : permissionAsk) {
            if (!isPermissionGranted(getActivity(), aPermissionAsk)) {
                permissionsNotGranted.add(aPermissionAsk);
            }
        }

        if (permissionsNotGranted.isEmpty()) {

            if (permissionResult != null)
                permissionResult.permissionGranted();

        } else {

            arrayPermissionNotGranted = new String[permissionsNotGranted.size()];
            arrayPermissionNotGranted = permissionsNotGranted.toArray(arrayPermissionNotGranted);
            ActivityCompat.requestPermissions(getActivity(),
                    arrayPermissionNotGranted, KEY_PERMISSION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode != KEY_PERMISSION) {
            return;
        }

        List<String> permissionDenied = new LinkedList<>();
        boolean granted = true;

        for (int i = 0; i < grantResults.length; i++) {

            if (!(grantResults[i] == PackageManager.PERMISSION_GRANTED)) {
                granted = false;
                permissionDenied.add(permissions[i]);
            }
        }

        if (permissionResult != null) {
            if (granted) {
                permissionResult.permissionGranted();
            } else {
                for (String s : permissionDenied) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), s)) {
                        permissionResult.permissionForeverDenied();
                        return;
                    }
                }
                permissionResult.permissionDenied();
            }
        }
    }

    public void askCompactPermission(String permission, PermissionResult permissionResult) {
        KEY_PERMISSION = 200;
        permissionsAsk = new String[]{permission};
        this.permissionResult = permissionResult;
        internalRequestPermission(permissionsAsk);
    }

    public void askCompactPermissions(String permissions[], PermissionResult permissionResult) {
        KEY_PERMISSION = 200;
        permissionsAsk = permissions;
        this.permissionResult = permissionResult;
        internalRequestPermission(permissionsAsk);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void onStop() {
        mEventBus.unregister(this);
        dismissProgress();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void onDestroy() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
        dismissProgress();
        super.onDestroy();
    }

}
