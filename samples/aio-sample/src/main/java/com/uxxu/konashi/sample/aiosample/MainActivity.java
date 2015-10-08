package com.uxxu.konashi.sample.aiosample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.uxxu.konashi.lib.Konashi;
import com.uxxu.konashi.lib.KonashiListener;
import com.uxxu.konashi.lib.KonashiManager;
import com.uxxu.konashi.lib.KonashiUtils;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.w3c.dom.Text;

import info.izumin.android.bletia.BletiaException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final MainActivity self = this;

    private TextView mValueText;

    private KonashiManager mKonashiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mValueText = (TextView) findViewById(R.id.text_value);
        findViewById(R.id.btn_read).setOnClickListener(this);
        findViewById(R.id.btn_find).setOnClickListener(this);

        mKonashiManager = new KonashiManager(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mKonashiManager.addListener(mKonashiListener);
        refreshViews();
    }

    @Override
    protected void onPause() {
        mKonashiManager.removeListener(mKonashiListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mKonashiManager.reset()
                        .then(new DoneCallback<BluetoothGattCharacteristic>() {
                            @Override
                            public void onDone(BluetoothGattCharacteristic result) {
                                mKonashiManager.disconnect();
                            }
                        });
            }
        }).start();
        super.onDestroy();
    }

    private void refreshViews() {
        boolean isReady = mKonashiManager.isReady();
        findViewById(R.id.btn_find).setVisibility(!isReady ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_read).setVisibility(isReady ? View.VISIBLE : View.GONE);
        mValueText.setVisibility(isReady ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_find:
                mKonashiManager.find(this);
                break;
            case R.id.btn_read:
                mKonashiManager.analogRead(Konashi.AIO0)
                .then(new DoneCallback<Integer>() {
                    @Override
                    public void onDone(Integer result) {
                        mValueText.setText(String.valueOf(result) + "mV");
                    }
                });
        }
    }

    private final KonashiListener mKonashiListener = new KonashiListener() {
        @Override
        public void onConnect(KonashiManager manager) {
            refreshViews();
        }

        @Override
        public void onDisconnect(KonashiManager manager) {
            refreshViews();
        }

        @Override
        public void onError(KonashiManager manager, BletiaException e) {

        }

        @Override
        public void onUpdatePioOutput(KonashiManager manager, int value) {

        }

        @Override
        public void onUpdateUartRx(KonashiManager manager, byte[] value) {

        }

        @Override
        public void onUpdateBatteryLevel(KonashiManager manager, int level) {

        }
    };
}
