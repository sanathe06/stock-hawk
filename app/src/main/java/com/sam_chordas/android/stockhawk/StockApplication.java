package com.sam_chordas.android.stockhawk;

import android.app.Application;

import com.sam_chordas.android.stockhawk.common.ConnectivityReceiver;

/**
 * Created by sanathnandasiri on 8/13/16.
 */
public class StockApplication extends Application {

    private static StockApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static StockApplication getInstance() {
        return instance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
