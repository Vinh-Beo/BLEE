package com.vinhbeo.blelibrary;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.pm.PackageManager;

import android.location.Address;
import android.os.Build;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Handler;

import android.support.annotation.RequiresApi;
import android.util.Log;
import com.unity3d.player.UnityPlayer;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.widget.Toast;

import static android.content.Context.BIND_AUTO_CREATE;

public class BLEFramework {

    private Activity _unityActivity;
    /*
    Singleton instance.
    */
    private static volatile BLEFramework _instance;


    /*
      Static variables
   */
    private static final String TAG = BLEFramework.class.getSimpleName();
    private static final long SCAN_PERIOD = 1000;
    private static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_CODE = 30;


    /*
      List containing all the discovered bluetooth devices
    */
    private List<BluetoothDevice> listBluetoothDevice = new ArrayList<BluetoothDevice>();

    /*
    Bluetooth device address and name to which the app is currently connected
    */

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BroadcastReceiver mReceiver;
    private Context mBase;
    private Handler mHandler;



    /*
    Bluetooth device address and name to which the app is currently connected
    */
    private BluetoothDevice device;
    private String mDeviceAddress;
    private String mDeviceName;

    /*
    Boolean variables used to estabilish the status of the connection
    */
    private boolean mConnected = false;
    private boolean flag = true;
    private boolean mScanning = false;


    /*
     Bluetooth service
    */
    private BLEServices mBluetoothLeService;

    private Map<UUID, BluetoothGattCharacteristic> _map = new HashMap<UUID, BluetoothGattCharacteristic>();




    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BLEServices.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                //finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Address"+ mDeviceName);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: Bluetooth disconnected");
            mBluetoothLeService = null;
        }
    };


    /*
    Callback called when the scan of bluetooth devices is finished
    */





    /*
    Callback called when the bluetooth device receive relevant updates about connection, disconnection, service discovery, data available, rssi update
    */

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {// đk sự kiện
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();// lay action của sự kiện GATT

            if (BLEServices.ACTION_GATT_CONNECTED.equals(action)) {// connect gatt service
                mConnected = true;
                flag = true;
                Log.d(TAG, "Connection estabilished with: " + mDeviceAddress);
                //updateConnectionState("GATT_CONNECTED");


            } else if (BLEServices.ACTION_GATT_DISCONNECTED.equals(action)) {// disconnect gatt service
                mConnected = false;
                flag = false;
                Log.d(TAG, "Connection lost");
                //updateConnectionState("GATT_DISCONNECTED");


            } else if (BLEServices.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "Service discovered");

                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());


            } else if (BLEServices.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "New Data received by the server");
                //displayData(intent.getStringExtra(BLEServices.EXTRA_DATA));

            }

        }
    };




     /*
    METHODS DEFINITION
    */

    public static BLEFramework getInstance(Activity activity) {
        if (_instance == null) {
            synchronized (BLEFramework.class) {
                if (_instance == null) {
                    Log.d(TAG, "BleFramework: Creation of _instance");
                    _instance = new BLEFramework(activity);
                }
            }
        }

        return _instance;
    }



    public BLEFramework(Activity activity) {
        Log.d(TAG, "BleFramework: saving unityActivity in private var.");
        this._unityActivity = activity;
    }

    /*
    Method used to create a filter for the bluetooth actions that you like to receive
    */

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEServices.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEServices.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEServices.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEServices.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    /*
    Method used to initialize the characteristic for data transmission
    */

    // SUA
//    private void getGattService(BluetoothGattService gattService)
//    {
//
//        if (gattService == null)
//            return;
//
//        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(BLEServices.UUID_BLE_SHIELD_TX);
//        _map.put(characteristic.getUuid(), characteristic);
//
//        BluetoothGattCharacteristic characteristicRx = gattService.getCharacteristic(BLEServices.UUID_BLE_SHIELD_RX);
//        mBluetoothLeService.setCharacteristicNotification(characteristicRx,
//                true);
//        mBluetoothLeService.readCharacteristic(characteristicRx);
//    }


    // SUA
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanLeDevice() {


        if (!mScanning) {

            // Stops scanning after a pre-defined scan period.

            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mleScanCallback);
                    mScanning = false;

                }
            }, SCAN_PERIOD);
//            new Thread() {
//
//                    @Override
//                    public void run() {
//                        mBluetoothLeScanner.stopScan((ScanCallback) leScanCallback);
//                        mScanning = false;
//
//
//                    }
//                }.start();
//            mBluetoothAdapter.startDiscovery();
//            mReceiver = new BroadcastReceiver() {
//                public void onReceive(Context context, Intent intent) {
//                    String action = intent.getAction();
//
//                    //Finding devices
//                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                        // Get the BluetoothDevice object from the Intent
//                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                        // Add the name and address to an array adapter to show in a ListView
//                         listBluetoothDevice.add(device);
//                        //listViewLE.invalidateViews(); SUA
//
//                    }
//                }
//            };
//
//            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//            registerReceiver(mReceiver, filter);



            mBluetoothAdapter.startLeScan(mleScanCallback);
            mScanning = true;

        } else {
            mBluetoothAdapter.stopLeScan(mleScanCallback);
            mScanning = false;

        }
    }


//    public Intent registerReceiver(
//            BroadcastReceiver receiver, IntentFilter filter) {
//        return mBase.registerReceiver(receiver, filter);
//    }


    private BluetoothAdapter.LeScanCallback mleScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    _unityActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listBluetoothDevice.add(device);
                            Log.d(TAG, device.getAddress() + " " + device.getName() + "");
                        }
                    });
                }
            };

//    //SUA
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private ScanCallback scanCallback = new ScanCallback() {
//
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//
//            //addBluetoothDevice(result.getDevice());SUA
//
//
//            Log.d(TAG, "onLeScan: run()");
//            if (device == null) {
//                Log.d(TAG, "Device is null? stop?");
//            } else {
//                listBluetoothDevice.add(device);
//                Log.d(TAG, device.getAddress() + " " + device.getName() + "");
//            }
//        }
//
//
//    };

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private ScanCallback scanCallback = new ScanCallback() {
//
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//
//            addBluetoothDevice(result.getDevice());
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
//            for(ScanResult result : results){
//                addBluetoothDevice(result.getDevice());
//            }
//        }
//
//        @Override// hien thi thong bao khi scan fail
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//
//        }
//
//        private void addBluetoothDevice(BluetoothDevice device){
//            if(!listBluetoothDevice.contains(device)){
//                listBluetoothDevice.add(device);
//
//            }
//        }
//    };


    private void unregisterBleUpdatesReceiver() {
        Log.d(TAG, "unregisterBleUpdatesReceiver:");
        _unityActivity.unregisterReceiver(mGattUpdateReceiver);
    }

    private void registerBleUpdatesReceiver() {
        Log.d(TAG, "registerBleUpdatesReceiver:");
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "registerBleUpdatesReceiver: WARNING: _mBluetoothAdapter is not enabled!");

        }
        Log.d(TAG, "registerBleUpdatesReceiver: registerReceiver");
        _unityActivity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void _InitBLEFramework() {
        System.out.println("Android Executing: _InitBLEFramework");

        if (!_unityActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "onCreate: fail: missing FEATURE_BLUETOOTH_LE");
            //UnityPlayer.UnitySendMessage("BLEControllerEventHandler", BLEUnityMessageName_OnBleDidInitialize, "Fail: missing FEATURE_BLUETOOTH_LE");
            return;
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) _unityActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "onCreate: fail: _mBluetoothAdapter is null");
            //UnityPlayer.UnitySendMessage("BLEControllerEventHandler", BLEUnityMessageName_OnBleDidInitialize, "Fail: Context.BLUETOOTH_SERVICE");
            return;
        }
        mHandler = new Handler();
        registerBleUpdatesReceiver();
    }

    public void Scan() {
        Log.d(TAG, "ScanForPeripherals: Launching scanLeDevice");
        scanLeDevice();

    }

    public boolean IsDeviceConnected() {
        Log.d(TAG, "_IsDeviceConnected");
        return mConnected;
    }

    public boolean SearchDeviceDidFinish() {
        Log.d(TAG, "_SearchDeviceDidFinish");
        return mScanning;
    }

    public String _GetListOfDevices() {
        String jsonListString;

        if (listBluetoothDevice.size() > 0) {
            Log.d(TAG, "_GetListOfDevices");
            String[] uuidsArray = new String[listBluetoothDevice.size()];

            for (int i = 0; i < listBluetoothDevice.size(); i++) {

                BluetoothDevice bd = listBluetoothDevice.get(i);


                uuidsArray[i] = bd.getAddress();
            }
            Log.d(TAG, "_GetListOfDevices: Building JSONArray");
            JSONArray uuidsJSON = new JSONArray(Arrays.asList(uuidsArray));
            Log.d(TAG, "_GetListOfDevices: Building JSONObject");
            JSONObject dataUuidsJSON = new JSONObject();

            try {
                Log.d(TAG, "_GetListOfDevices: Try inserting uuuidsJSON array in the JSONObject");
                dataUuidsJSON.put("data", uuidsJSON);
            } catch (JSONException e) {
                Log.e(TAG, "_GetListOfDevices: JSONException");
                e.printStackTrace();
            }

            jsonListString = dataUuidsJSON.toString();

            Log.d(TAG, "_GetListOfDevices: sending found devices in JSON: " + jsonListString);

        } else {
            jsonListString = "NO DEVICE FOUND";
            Log.d(TAG, "_GetListOfDevices: no device was found");
        }

        return jsonListString;
    }

    public boolean _ConnectPeripheralAtIndex(int peripheralIndex) {
        Log.d(TAG, "_ConnectPeripheralAtIndex: " + peripheralIndex);
        BluetoothDevice device = listBluetoothDevice.get(peripheralIndex);

        mDeviceAddress = device.getAddress();
        mDeviceName = device.getName();


        return true;
    }

    public void _ConnectPeripheral() {
        Log.d(TAG, "_ConnectPeripheral:FETEL ");

        for (BluetoothDevice device : listBluetoothDevice) {
            if (device.getAddress() == "80:EA:CA:00:00:00") {

                Intent gattServiceIntent = new Intent(_unityActivity, BLEServices.class);
                _unityActivity.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                Log.d(TAG, "Connected ");


            }
            Log.d(TAG, "Disconnected ");
        }



    }

    public byte[] GetData(byte[] value) {
        if (BluetoothGattCharacteristic.PROPERTY_READ > 0) {
            BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString(BLEAttribute.BLE_SHIELD_SERVICE));
            BluetoothGattCharacteristic characteristicTX = Service.getCharacteristic(UUID.fromString(BLEAttribute.BLE_SHIELD_TX));

            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }

            mBluetoothLeService.readCharacteristic(characteristicTX);
        }

        Log.d(TAG, "Connected " + value);


        return value;
    }


    public void SendData(byte[] value) {
        Log.d(TAG, "SendData: ");

        if (BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
            BluetoothGattCharacteristic characteristic = _map.get(BLEServices.UUID_BLE_SHIELD_RX);
            Log.d(TAG, "Set data in the characteristicRx");

            Log.d(TAG, "Write _characteristic");
            if (!characteristic.setValue(value)) {
                Log.w(TAG, "Couldn't set characteristic's local value");
                //return;
            }

            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

            mBluetoothLeService.writeCharacteristic(value);

        }


    }
}

