package fitme.ai.bluetoothdev.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;

/**
 * Created by hongy on 2018/3/19.
 */

public class BlueToothManager {

    private static Context mContext;
    private static final int INTERVAL_TIME = 10000;
    private Handler mHandler = new Handler();
    private static BluetoothAdapter bluetoothAdapter;
    private static BlueToothManager blueToothManager = null;

    private static class BlueToothHolder{
        private static BlueToothManager blueToothManager = new BlueToothManager();
    }

    public static BlueToothManager getInstance(Context context){
        mContext = context.getApplicationContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.setName("AYAH_HOME");  //蓝牙改名
        return BlueToothHolder.blueToothManager;
    }

    public BlueToothManager(){}

    /**
       打开蓝牙
     */
    public boolean openBlueTooth(){
        if (!bluetoothAdapter.isEnabled()){
            return bluetoothAdapter.enable();
        }else {
            return false;
        }
    }

    /**
     * 关闭蓝牙
     */
    public boolean closeBlueTooth(){
        if (bluetoothAdapter.isEnabled()){
            return bluetoothAdapter.disable();
        }else {
            return false;
        }
    }

    /***********
     * 扫描设备
     ********/
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                    });
                }
            };
        } else {
            L.i("设备蓝牙版本过低");
            return;
        }
    }
}
