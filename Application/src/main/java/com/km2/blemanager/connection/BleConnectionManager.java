package com.km2.blemanager.connection;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.km2.blemanager.BleCharacteristic;
import com.km2.blemanager.BleService;
import com.km2.blemanager.BluetoothLeService;

import java.util.ArrayList;
import java.util.List;

public class BleConnectionManager {

    private final static String TAG = BleConnectionManager.class.getSimpleName();

    private Context mContext;

    private String mDeviceAddress;

    private RecyclerView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private Callback mCallback;

    public void connect(String deviceAddress) {
        mDeviceAddress = deviceAddress;
        mBluetoothLeService.connect(mDeviceAddress);
    }


    public interface Callback {

        void onConnectionFailed();

        void onConnected();

        void onDisconnected();

        void onServiceDiscovered(List<BleService> gattServices);

        void onDataReceived(String stringExtra);
    }

    public BleConnectionManager() {

    }

    public void init(Context context, String deviceAddress) {
        mContext = context;
        mDeviceAddress = deviceAddress;
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private Callback getCallback() {
        if (mCallback == null) {
            throw new IllegalStateException("BleConnection Listener is null");
        }
        return mCallback;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                getCallback().onConnectionFailed();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                getCallback().onConnected();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                getCallback().onDisconnected();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                getCallback().onServiceDiscovered(getGattServices(mBluetoothLeService.getSupportedGattServices()));
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                getCallback().onDataReceived(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private List<BleService> getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return null;
        List<BleService> bleServices = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            BleService bleService = BleService.toBleService(gattService);
            bleServices.add(bleService);

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                BleCharacteristic bleCharacteristic = BleCharacteristic.toBleCharacteristic(bleService.getUUID(), gattCharacteristic);
                bleService.addCharacteristic(bleCharacteristic);
            }
        }
        return bleServices;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void onResume() {
        mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    public void onPause() {
        mContext.unregisterReceiver(mGattUpdateReceiver);
    }

    public void onDestroy() {
        mContext.unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public void disconnect() {
        mBluetoothLeService.disconnect();
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void readCharacteristic(BleCharacteristic bleCharacteristic) {
        mBluetoothLeService.readCharacteristic(bleCharacteristic);
    }

    public void writeCharacteristic(BleCharacteristic bleCharacteristic, String inputValue) {
        mBluetoothLeService.writeCharacteristic(bleCharacteristic, inputValue);
    }

    public void startNotification(BleCharacteristic bleCharacteristic) {
        mBluetoothLeService.setCharacteristicNotification(bleCharacteristic);
    }


}
