package com.km2.blemanager;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

/**
 * Created by subhamtyagi on 09/01/17.
 */

public class BleCharacteristic {

    private String mName;
    private String mUUID;
    private String mServiceUUID;
    private boolean isWritable;
    private boolean isReadable;
    private boolean isNotifiable;
    private boolean isNotificationStarted;
    private boolean isWriteWithoutResponse;

    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;

    public static BleCharacteristic toBleCharacteristic(String serviceUUID, BluetoothGattCharacteristic gattCharacteristic) {
        BleCharacteristic bleCharacteristic = new BleCharacteristic();
        bleCharacteristic.mServiceUUID = serviceUUID;
        String uuid = gattCharacteristic.getUuid().toString();
        bleCharacteristic.setName(SampleGattAttributes.lookup(uuid, "Unknown"));
        bleCharacteristic.setUUID(uuid);
        bleCharacteristic.mBluetoothGattCharacteristic = gattCharacteristic;
        int property = gattCharacteristic.getProperties();
        int writeType = gattCharacteristic.getWriteType();

        if ((property & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            bleCharacteristic.isReadable = true;
        }
        if ((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            bleCharacteristic.isNotifiable = true;
        }

        if ((property & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
            bleCharacteristic.isWritable = true;
        }
        return bleCharacteristic;
    }

    public BleCharacteristic() {
    }

    public BleCharacteristic(String name, String UUID) {
        mName = name;
        mUUID = UUID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public UUID getUUID() {
        return UUID.fromString(mUUID);
    }

    public void setUUID(String UUID) {
        mUUID = UUID;
    }

    public boolean isWritable() {
        return isWritable;
    }

    public void setWritable(boolean writable) {
        isWritable = writable;
    }

    public boolean isReadable() {
        return isReadable;
    }

    public void setReadable(boolean readable) {
        isReadable = readable;
    }

    public boolean isNotifiable() {
        return isNotifiable;
    }

    public void setNotifiable(boolean notifiable) {
        isNotifiable = notifiable;
    }

    public boolean isNotificationStarted() {
        return isNotificationStarted;
    }

    public void setNotificationStarted(boolean notificationStarted) {
        isNotificationStarted = notificationStarted;
    }

    public boolean isWriteWithoutResponse() {
        return isWriteWithoutResponse;
    }

    public void setWriteWithoutResponse(boolean writeWithoutResponse) {
        isWriteWithoutResponse = writeWithoutResponse;
    }

    public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        return mBluetoothGattCharacteristic;
    }

    public void setBluetoothGattCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        mBluetoothGattCharacteristic = bluetoothGattCharacteristic;
    }

    public UUID getServiceUUID() {
        return UUID.fromString(mServiceUUID);
    }

    public void setServiceUUID(String serviceUUID) {
        mServiceUUID = serviceUUID;
    }
}
