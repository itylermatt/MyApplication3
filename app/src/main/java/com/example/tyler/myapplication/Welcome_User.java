package com.example.tyler.myapplication;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tyler.myapplication.Welcome_User;

import java.util.ArrayList;
import java.util.HashMap;

public class Welcome_User extends AppCompatActivity implements View.OnClickListener , AdapterView.OnItemClickListener {
    private final static String TAG = Welcome_User.class.getSimpleName();
    public static final int REQUEST_ENABLE_BT = 1;

    private HashMap<String,BTLE_Device> mBTDeviceHashMap;
    private ArrayList<BTLE_Device> mBTDeviceArrayList;
    private ListAdapter_BTLE_DEVICES adapter;
    private Button btn_scan;
    private BroadCastReceiver_BTState mBTStateUpdateReceiver;
    private Scanner_BTLE mBTLeScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome__user);
        TextView tv = (TextView) findViewById(R.id.textView);
        String t = getIntent().getExtras().getString("name");
        String a = getIntent().getExtras().getString("surname");
        tv.setText("Welcome " + t + " " + a + " to ARIVL APP SEAMLESS CONTROL!!!\n");

        //BLE Coding Starts here
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Utils.toast(getApplicationContext(),"BLE not supported");
            finish();
        }
        mBTStateUpdateReceiver = new BroadCastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new Scanner_BTLE(this,7500,-75);
        mBTDeviceHashMap = new HashMap<>();
        mBTDeviceArrayList = new ArrayList<>();
        adapter = new ListAdapter_BTLE_DEVICES(this,R.layout.activity_welcome__user,mBTDeviceArrayList);
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
      // ((ScrollView) findViewById(R.id.scrollview)).addView(listView);
        ScrollView sv = new ScrollView(this);
        sv.addView(listView);
       //((ScrollView) new ScrollView(this)).addView(listView);
     //  this.setContentView(sv);
       // LinearLayout ian = (LinearLayout) findViewById(R.id.linear);//linear
      //  TextView tyler = new TextView(this);
      //  tyler.setText("TestView");
      //  tyler.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
      //  ian.addView(tyler);
     //   tyler.setText("tyler");
        //new added function
      //  ((LinearLayout) findViewById(R.id.linear)).addView(listView);
        btn_scan = (Button) findViewById(R.id.button);
        findViewById(R.id.button).setOnClickListener(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check which request we're responding to
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode==RESULT_OK){
                Utils.toast(getApplicationContext(),"Thanks for turning on BT");
            }
            else if(resultCode==RESULT_CANCELED){
                Utils.toast(getApplicationContext(),"Please turn on bluetooth");
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        //used in future BLE tuts
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.button:
                Utils.toast(getApplicationContext(),"Scan Button Pressed");
                if(!mBTLeScanner.isScanning()){
                    startScan();
                }else{
                    stopScan();
                }
                break;
            default:
                break;
        }

    }
    public void addDevice(BluetoothDevice device, int new_rssi){
        String address = device.getAddress();
        if(!mBTDeviceHashMap.containsKey(address)){
            BTLE_Device btle_device = new BTLE_Device(device);
            btle_device.setRSSI(new_rssi);
            mBTDeviceHashMap.put(address,btle_device);
            mBTDeviceArrayList.add(btle_device);
        }else{
            mBTDeviceHashMap.get(address).setRSSI(new_rssi);
        }
        adapter.notifyDataSetChanged();
    }
    public void startScan(){
        btn_scan.setText("Scanning..");
        ((TextView) findViewById(R.id.btle_device_list_items)).setText("Devices: ");
        ((TextView) findViewById(R.id.tv_macaddr)).setText("Addresses: ");
        ((TextView) findViewById(R.id.tv_rssi)).setText("Rssis: ");
        mBTDeviceArrayList.clear();
        mBTDeviceHashMap.clear();
        adapter.notifyDataSetChanged();
        mBTLeScanner.start();
    }
    public void stopScan(){
        btn_scan.setText("Scan Again");
        TextView name =findViewById(R.id.btle_device_list_items);
        TextView address = findViewById(R.id.tv_macaddr);
        TextView rssi = findViewById(R.id.tv_rssi);
        for(int count =0 ; count< mBTDeviceArrayList.size();count++){
           if(mBTDeviceArrayList.get(count).getName() != null)
           { if(mBTDeviceArrayList.get(count).getName().length()>0){
                name.setText(name.getText() + mBTDeviceArrayList.get(count).getName()+ " ");
                address.setText(address.getText() + mBTDeviceArrayList.get(count).getAddress()+ " ");
                rssi.setText(rssi.getText() + Integer.toString(mBTDeviceArrayList.get(count).getRSSI())+ " ");
            }
           }
        }
        mBTLeScanner.stop();
    }
}