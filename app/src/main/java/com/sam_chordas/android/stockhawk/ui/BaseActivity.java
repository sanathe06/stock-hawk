package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.StockApplication;
import com.sam_chordas.android.stockhawk.common.ConnectivityReceiver;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by sanathnandasiri on 8/13/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private Unbinder mUnBinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mUnBinder = ButterKnife.bind(this);
    }

    public abstract int getLayoutId();

    @Override
    protected void onStart() {
        super.onStart();
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
    protected void onStop() {
        super.onStop();
        mUnBinder.unbind();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }
}
