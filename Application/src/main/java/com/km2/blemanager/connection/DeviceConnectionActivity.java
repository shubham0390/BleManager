
package com.km2.blemanager.connection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.km2.blemanager.BleCharacteristic;
import com.km2.blemanager.BleService;
import com.km2.blemanager.R;
import com.km2.blemanager.widgets.CommentAnimator;

import java.util.List;

public class DeviceConnectionActivity extends AppCompatActivity implements BleConnectionManager.Callback, ServicesAdapter.Callback {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceAddress;
    private RecyclerView mGattServicesList;
    private BleConnectionManager mBleConnectionManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        final Intent intent = getIntent();

        String deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mBleConnectionManager = new BleConnectionManager();
        mBleConnectionManager.init(this, mDeviceAddress);
        mBleConnectionManager.setCallback(this);

        mGattServicesList = (RecyclerView) findViewById(R.id.gatt_services_list);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mBleConnectionManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBleConnectionManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBleConnectionManager.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mBleConnectionManager.isConnected()) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBleConnectionManager.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBleConnectionManager.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }


    @Override
    public void onConnectionFailed() {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onConnected() {
        updateConnectionState(R.string.connected);
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onDisconnected() {
        updateConnectionState(R.string.disconnected);
        supportInvalidateOptionsMenu();
        clearUI();
    }

    private void clearUI() {
        mGattServicesList.setAdapter(null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onServiceDiscovered(List<BleService> bleServices) {
        CommentAnimator commentAnimator = new CommentAnimator();
        mGattServicesList.setItemAnimator(commentAnimator);
        mGattServicesList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ServicesAdapter servicesAdapter = new ServicesAdapter(this, 120, bleServices);
        servicesAdapter.setRecyclerView(mGattServicesList);
        servicesAdapter.setCallback(this);
        servicesAdapter.setAnimator(commentAnimator);
        mGattServicesList.setAdapter(servicesAdapter);
        servicesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataReceived(String stringExtra) {
        displayData(stringExtra);
    }

    @Override
    public void readCharacteristic(BleCharacteristic bleCharacteristic) {
        mBleConnectionManager.readCharacteristic(bleCharacteristic);
    }

    @Override
    public void writeCharacteristic(BleCharacteristic bleCharacteristic, String inputValue) {
        mBleConnectionManager.writeCharacteristic(bleCharacteristic, inputValue);
    }

    @Override
    public void startNotifications(BleCharacteristic bleCharacteristic) {
        if (bleCharacteristic.isNotificationStarted()) {
            Toast.makeText(this, "Notifications already started", Toast.LENGTH_SHORT).show();
            return;
        }
        mBleConnectionManager.startNotification(bleCharacteristic);
    }
}
