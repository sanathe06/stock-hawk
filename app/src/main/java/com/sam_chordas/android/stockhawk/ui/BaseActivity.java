package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.StockApplication;
import com.sam_chordas.android.stockhawk.common.ConnectivityReceiver;

/**
 * Created by sanathnandasiri on 8/13/16.
 */
public class BaseActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StockApplication.getInstance().setConnectivityListener(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        StockApplication.getInstance().setConnectivityListener(null);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }
}
