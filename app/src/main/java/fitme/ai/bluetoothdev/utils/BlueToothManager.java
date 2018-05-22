package fitme.ai.bluetoothdev.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hongy on 2018/3/19.
 */

public class BlueToothManager {

    /*private static Context mContext;
    private static final int INTERVAL_TIME = 10000;
    private Handler mHandler = new Handler();
    private static BluetoothAdapter bluetoothAdapter;
    private static BlueToothManager blueToothManager = null;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private List<BluetoothDevice> devices;

    private static class BlueToothHolder{
        private static BlueToothManager blueToothManager = new BlueToothManager();
    }

    public static BlueToothManager getInstance(Context context){
        mContext = context.getApplicationContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.setName("AYAH_HOME");  //蓝牙改名
        return BlueToothHolder.blueToothManager;
    }

    public BlueToothManager(){
        devices = new LinkedList<>();
    }

    *//**
       打开蓝牙
     *//*
    public boolean openBlueTooth(){
        if (!bluetoothAdapter.isEnabled()){
            return bluetoothAdapter.enable();
        }else {
            return false;
        }
    }

    *//**
     * 关闭蓝牙
     *//*
    public boolean closeBlueTooth(){
        if (bluetoothAdapter.isEnabled()){
            return bluetoothAdapter.disable();
        }else {
            return false;
        }
    }

    *//***********
     * 扫描设备
     ********//*
    private void scanLeDevice(final boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (enable) {
                devices.clear();//清空集合
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            bluetoothAdapter.stopLeScan(mLeScanCallback);
                        }
                    }
                }, INTERVAL_TIME);
                bluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                try {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                } catch (Exception e) {
                }
            }
        }
    }

    private void initCallBack(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    if (device != null) {
                        if (!TextUtils.isEmpty(device.getName())) {
                            // devices.add(device);
                            String name = device.getName();
                            if (name.contains(BluetoothDeviceAttr.OYGEN_DEVICE_NAME)) {
                                if (!devices.contains(device)) {
                                    devices.add(device);
                                }
                            }
                        }
                    }
                }
            };
        } else {
            L.i("设备蓝牙版本过低");
            return;
        }
    }

    private boolean connectDevice(){
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            L.i("Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, GattCallback);
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback) {
        return (connectGatt(context, autoConnect,callback, TRANSPORT_AUTO));
    }


    private BluetoothGattCallback GattCallback = new BluetoothGattCallback() {
        // 这里有9个要实现的方法，看情况要实现那些，用到那些就实现那些
        //当连接状态发生改变的时候
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){

        }
        //回调响应特征写操作的结果。
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){

        }
        //回调响应特征读操作的结果。
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }
        //当服务被发现的时候回调的结果
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        }
        //当连接能被被读的操作
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            super.onDescriptorRead(gatt, descriptor, status);
        }
    };*/
}
