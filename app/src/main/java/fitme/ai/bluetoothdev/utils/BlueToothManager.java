package fitme.ai.bluetoothdev.utils;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by hongy on 2018/3/19.
 */

public class BlueToothManager {

    private static BluetoothAdapter bluetoothAdapter;
    private static BlueToothManager blueToothManager = null;
    public static BlueToothManager getInstance(){
        if (blueToothManager==null){
            blueToothManager = new BlueToothManager();
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.setName("AYAH_HOME");  //蓝牙改名
        }
        return blueToothManager;
    }

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

}
