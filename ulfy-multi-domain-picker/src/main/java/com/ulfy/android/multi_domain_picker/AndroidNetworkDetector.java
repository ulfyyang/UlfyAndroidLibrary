package com.ulfy.android.multi_domain_picker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

class AndroidNetworkDetector implements NetworkDetector {
    private Context context;

    public AndroidNetworkDetector(Context context) {
        this.context = context;
    }

    @Override public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
