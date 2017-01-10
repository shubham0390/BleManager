package com.km2.blemanager.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.km2.blemanager.R;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BleScanManager {

    private static final long SCAN_PERIOD = 10000;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private boolean mScanning;
    private ScanListener mScanListener;

    public interface ScanListener {
        void onScanStart();

        void onScanStop();

        void onDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord);
    }


    public BleScanManager(Context context) {
        mContext = context;
        init();
    }

    private void init() {

        mHandler = new Handler();
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
        }
    }

    public void setScanListener(ScanListener scanListener) {
        mScanListener = scanListener;
    }

    public ScanListener getScanListener() {
        if (mScanListener == null) {
            throw new IllegalStateException("Scan listener is null");
        }
        return mScanListener;
    }

    public void toggleScan(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    bluetoothLeScanner.stopScan(mLeScanCallback);
                    getScanListener().onScanStop();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            ParcelUuid parcelUuid = new ParcelUuid(UUID.fromString("daebb240-b041-11e4-9e45-0002a5d5c51b"));
            ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(parcelUuid).build();
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
            bluetoothLeScanner.startScan(Collections.singletonList(scanFilter), scanSettings, mLeScanCallback);
            getScanListener().onScanStart();
        } else {
            mScanning = false;
            getScanListener().onScanStop();
        }
    }

    public boolean isScanning() {
        return mScanning;
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            getScanListener().onDeviceFound(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                getScanListener().onDeviceFound(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("Subham", errorCode + "");
        }
    };

}
