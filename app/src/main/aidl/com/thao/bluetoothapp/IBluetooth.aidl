// IBluetooth.aidl
package com.thao.bluetoothapp;

// Declare any non-default types here with import statements

interface IBluetooth {
    /**
     * System private API for Bluetooth service
     */
    String getRemoteAlias(in String address);
      boolean setRemoteAlias(in String address, in String name);
}
