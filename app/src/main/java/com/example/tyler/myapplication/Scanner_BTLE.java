package com.example.tyler.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import android.os.Handler;

/**
 * Created by Stacks on Stacks on 2018-03-23.
 * @version 1.0
 */


public class Scanner_BTLE {
    private Welcome_User ma;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private long scanPeriod;
    private int signalStrength;

    /**
     *
     * @param welcomeActivity reference to the activity using this function
     * @param scanPeriod the amount of time that the central searches for a peripheral
     * @param signalStrength the signal strength range
     */
    public Scanner_BTLE(Welcome_User welcomeActivity, long scanPeriod , int signalStrength){
        ma = welcomeActivity;
        mHandler = new Handler();
        this.scanPeriod = scanPeriod;
        this.signalStrength =signalStrength;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) ma.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

    }

    /**
     * @return boolean to notify whether device is scanning for bluetooth or not
     */
    public boolean isScanning(){return mScanning;}

    /**
     *  checks if user's bluetooth is switched on, if not prompts user to turn it on
     */
    public void start(){
        if(!Utils.checkBluetooth(mBluetoothAdapter)){
            Utils.requestUsersBluetooth(ma);
            ma.stopScan();
        }else{
            scanLeDevice(true);
        }
    }
    public void stop(){
        scanLeDevice(false);
    }

    /**
     * 
     * @param enable
     */
    private void scanLeDevice(final boolean enable){
        if(enable && !mScanning){ // CODE CHANGE HERE = true ie.!mScanning
            Utils.toast(ma.getApplication(),"Starting BLE Scan...");
            mHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    Utils.toast(ma.getApplicationContext(),"Stopping BLE Scan");
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    ma.stopScan();
                }

            },scanPeriod);
            mScanning=true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);

        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback=
            new BluetoothAdapter.LeScanCallback(){
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    final int new_rssi =rssi;
                    if(rssi > signalStrength){
                        mHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                ma.addDevice(device,new_rssi);
                            }
                        });
                    }
                }

            };
}
