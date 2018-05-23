package fitme.ai.bluetoothdev;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fitme.ai.bluetoothdev.utils.BlueToothManager;
import fitme.ai.bluetoothdev.utils.L;

public class MainActivity extends Activity {

    private static BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    BluetoothDevice bluetoothDevice = (BluetoothDevice) msg.obj;
                    L.i("--------address--------:"+bluetoothDevice.getAddress());
                    L.i("--------getName--------:"+bluetoothDevice.getName());
                    L.i("--------getBondState--------:"+bluetoothDevice.getBondState());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 用BroadcastReceiver来取得搜索结果
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化
        registerReceiver(searchDevices, intent);
    }

    public void click(View view){
        switch (view.getId()){
            case R.id.bt_open:
                openBlueTooth();
                break;
            case R.id.bt_close:
                unregisterReceiver(this);
                closeBlueTooth();
                break;
            case R.id.bt_search:
                startSearthBltDevice(this);
                break;
        }
    }

    //打开蓝牙
    public boolean openBlueTooth(){
        if (!bluetoothAdapter.isEnabled()){
            return bluetoothAdapter.enable();
        }else {
            return false;
        }
    }


    //关闭蓝牙
    public boolean closeBlueTooth(){
        if (bluetoothAdapter.isEnabled()){
            return bluetoothAdapter.disable();
        }else {
            return false;
        }
    }

    /**
     * 蓝牙接收广播
     */
    private BroadcastReceiver searchDevices = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            Object[] lstName = b.keySet().toArray();
            // 显示所有收到的消息及其细节
            for (int i = 0; i < lstName.length; i++) {
                String keyName = lstName[i].toString();
                L.i("bluetooth:"+keyName + ">>>" + String.valueOf(b.get(keyName)));
            }
            BluetoothDevice device;
            // 搜索发现设备时，取得设备的信息；这里有可能重复搜索同一设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                //onRegisterBltReceiver.onBluetoothDevice(device);
            }
            //状态改变时
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        L.i("BlueToothTestActivity" + "正在配对......");
                        //onRegisterBltReceiver.onBltIng(device);
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        L.i("BlueToothTestActivity" + "完成配对");
                        //onRegisterBltReceiver.onBltEnd(device);
                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        L.i("BlueToothTestActivity" + "取消配对");
                        //onRegisterBltReceiver.onBltNone(device);
                    default:
                        break;
                }
            }
        }
    };

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(searchDevices);
        if (bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();
    }

    /**
     * 开始搜索
     * @param context
     * @return
     */
    private BluetoothSocket socket;
    private boolean startSearthBltDevice(Context context) {
        //开始搜索设备，当搜索到一个设备的时候就应该将它添加到设备集合中，保存起来
        //checkBleDevice(context);
        //如果当前发现了新的设备，则停止继续扫描，当前扫描到的新设备会通过广播推向新的逻辑
        if (bluetoothAdapter.isDiscovering()){
            stopSearthBltDevice();

            /**
             * 这个操作应该放在子线程中，因为存在线程阻塞的问题
             */

            new Thread(){
                @Override
                public void run() {
                    super.run();
                    //服务器端的bltsocket需要传入uuid和一个独立存在的字符串，以便验证，通常使用包名的形式
                    BluetoothServerSocket bluetoothServerSocket = null;
                    try {
                        bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("com.bluetooth.demo", getMyUUID());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while (true) {
                        try {
                            //注意，当accept()返回BluetoothSocket时，socket已经连接了，因此不应该调用connect方法。
                            //这里会线程阻塞，直到有蓝牙设备链接进来才会往下走
                            socket = bluetoothServerSocket.accept();
                            if (socket != null) {
                                //BltAppliaction.bluetoothSocket = socket;
                                //回调结果通知
                                Message message = new Message();
                                message.what = 1;
                                message.obj = socket.getRemoteDevice();
                                handler.sendMessage(message);
                                //如果你的蓝牙设备只是一对一的连接，则执行以下代码
                                bluetoothServerSocket.close();

                            }
                        } catch (IOException e) {
                            try {
                                bluetoothServerSocket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }.start();
        }
        L.i("bluetooth"+"本机蓝牙地址：" + bluetoothAdapter.getAddress());
        //开始搜索
        bluetoothAdapter.startDiscovery();
        //这里的true并不是代表搜索到了设备，而是表示搜索成功开始。
        return true;
    }

    /**
     * 停止搜索蓝牙
     * @return
     */
    public boolean stopSearthBltDevice() {
        //暂停搜索设备
        if(bluetoothAdapter!=null){
            return bluetoothAdapter.cancelDiscovery();
        }else {
            return false;
        }
    }



    private UUID getMyUUID(){

        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, tmPhone, androidId;

        tmDevice = "" + tm.getDeviceId();

        tmSerial = "" + tm.getSimSerialNumber();

        androidId = "" +android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());

        String uniqueId = deviceUuid.toString();

        L.i("debug"+ "uuid="+uniqueId);

        return deviceUuid;

    }

}
