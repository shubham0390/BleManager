package com.km2.blemanager;

import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;

public class BleService {

    private String mName;
    private String mUUID;
    private BluetoothGattService mBluetoothGattService;
    private List<BleCharacteristic> mBleCharacteristics = new ArrayList<>();

    public static BleService toBleService(BluetoothGattService gattService) {
        BleService bleService = new BleService();
        String uuid = gattService.getUuid().toString();
        bleService.setName(SampleGattAttributes.lookup(uuid, "Unknown"));
        bleService.setUUID(uuid);
        bleService.mBluetoothGattService = gattService;
        return bleService;
    }

    public BleService() {
    }

    public BleService(String name, String UUID) {
        mName = name;
        mUUID = UUID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String UUID) {
        mUUID = UUID;
    }

    public void addCharacteristic(BleCharacteristic bleCharacteristic) {
        mBleCharacteristics.add(bleCharacteristic);
    }

    public List<BleCharacteristic> getBleCharacteristics() {
        return mBleCharacteristics;
    }
}
