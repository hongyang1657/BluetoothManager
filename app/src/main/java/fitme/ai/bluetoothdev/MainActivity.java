package fitme.ai.bluetoothdev;

import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fitme.ai.bluetoothdev.utils.BlueToothManager;
import fitme.ai.bluetoothdev.utils.L;
import fitme.ai.bluetoothdev.utils.PacketUtils;

public class MainActivity extends Activity {

    private static BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> devices = new ArrayList<>();
    @SuppressLint("HandlerLeak")
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
                case 2:
                    BluetoothDevice bluetoothDevice1 = (BluetoothDevice) msg.obj;
                    L.i("--------主动连上的设备address--------:"+bluetoothDevice1.getAddress());
                    L.i("--------主动连上的设备getName--------:"+bluetoothDevice1.getName());
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
        BluetoothManager bluetoothManager=(BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
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
                checkBleDevice(this);
                break;
            case R.id.bt_close:
                unregisterReceiver(this);
                closeBlueTooth();
                break;
            case R.id.bt_search_on:

                L.i("开始搜索："+startSearthBltDevice(this));
                break;
            case R.id.bt_search_off:
                stopSearthBltDevice();
                break;
            case R.id.bt_open_service:
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        runOpenService(handler);
                    }
                }.start();

                break;
            case R.id.bt_connect:
                /*for (int i=0;i<newDevices.size();i++){
                    if ("OnePlus 3T".equals(newDevices.get(i).getName())){
                        L.i("发现QCY，开始连接");
                        final int finalI = i;
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                connect(newDevices.get(finalI));
                            }
                        }.start();

                    }
                }*/
                getBltList();
                break;
            default:
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

    public void checkBleDevice(Context context) {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(enableBtIntent);
            }
        } else {
            L.i("blueTooth"+"该手机不支持蓝牙");
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



    @SuppressLint("MissingPermission")
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


    /**
     * 尝试连接一个设备，子线程中完成，因为会线程阻塞
     *
     * @param btDev 蓝牙设备对象
     * @return
     */
    //private BluetoothSocket mBluetoothSocket;
    private void connect(BluetoothDevice btDev,Handler handler) {
        try {
            //通过和服务器协商的uuid来进行连接
            socket = btDev.createRfcommSocketToServiceRecord(getMyUUID());
            //通过反射得到bltSocket对象，与uuid进行连接得到的结果一样，但这里不提倡用反射的方法
            socket = (BluetoothSocket) btDev.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(btDev, 1);
            //在建立之前调用
            if (bluetoothAdapter.isDiscovering()){
                //停止搜索
                bluetoothAdapter.cancelDiscovery();
            }
            //如果当前socket处于非连接状态则调用连接
            if (!socket.isConnected()) {
                //你应当确保在调用connect()时设备没有执行搜索设备的操作。
                // 如果搜索设备也在同时进行，那么将会显著地降低连接速率，并很大程度上会连接失败。
                L.i("mBluetoothSocket开始连接");
                socket.connect();
            }
            L.i("blueTooth"+ "已经链接");

        } catch (Exception e) {
            L.i("blueTooth"+"...链接失败"+e.toString());
            Method m = null;
            try {
                m = btDev.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                socket = (BluetoothSocket) m.invoke(btDev, 1);
                socket.connect();
            } catch (Exception e1) {
                e1.printStackTrace();
                try {
                    L.i("blueTooth"+"...链接又失败了"+e.toString());
                    socket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            e.printStackTrace();
        }

        if (handler == null) return;
        //结果回调
        Message message = new Message();
        message.what = 2;
        message.obj = btDev;
        handler.sendMessage(message);

        //recSocketMsg();


    }

    //接收socket数据
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private byte[] sizeByte = new byte[64];
    private void recSocketMsg(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                while (!socket.isConnected() ) {
                    try {
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                        byte[] msgBytes = null;
                        int totalLength = 0;
                        L.i("getReadByte:"+getReadByte());
                        while (getReadByte()!=-1){
                            // TODO 隔1s 判断一次
                            // 每当读取到来自服务器的数据之后，发送的消息通知程序
                            msgBytes = PacketUtils.getInstance().appendBytes(msgBytes, sizeByte);
                            totalLength = PacketUtils.getInstance().getSocketBodyLength(msgBytes);
                            L.i("ssss"+ "=====content=====:" + (new String(msgBytes, "UTF-8")));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    private int getReadByte() {
        int readlenlength = -1;
        try {
            readlenlength = inputStream.read(sizeByte);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readlenlength;
    }

    /**
     * 尝试配对和连接
     *
     * @param btDev
     */
    public void createBond(BluetoothDevice btDev, Handler handler) {
        if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
            //如果这个设备取消了配对，则尝试配对
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                btDev.createBond();
            }
        } else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
            //如果这个设备已经配对完成，则尝试连接
            connect(btDev, handler);
        }
    }

    /**
     * 获得系统保存的配对成功过的设备，并尝试连接
     */
    public void getBltList() {
        if (bluetoothAdapter== null) return;
        //获得已配对的远程蓝牙设备的集合
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext(); ) {
                BluetoothDevice device = it.next();
                //自动连接已有蓝牙设备
                createBond(device, handler);
            }
        }
    }

    /**
     * 输入mac地址进行自动配对
     * 前提是系统保存了该地址的对象
     *
     * @param address
     */
    public void autoConnect(String address, Handler handler) {
        if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
        BluetoothDevice btDev = bluetoothAdapter.getRemoteDevice(address);
        connect(btDev, handler);
    }


    public void runOpenService(Handler handler){
        /**
         * 这个操作应该放在子线程中，因为存在线程阻塞的问题
         */
        //服务器端的bltsocket需要传入uuid和一个独立存在的字符串，以便验证，通常使用包名的形式
        BluetoothServerSocket bluetoothServerSocket = null;
        try {
            bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("fitme.ai.bluetoothdev", getMyUUID());
        } catch (IOException e) {
            e.printStackTrace();
            L.i("验证uuid"+e.toString());
        }
        while (true) {
            try {
                //注意，当accept()返回BluetoothSocket时，socket已经连接了，因此不应该调用connect方法。
                //这里会线程阻塞，直到有蓝牙设备链接进来才会往下走
                L.i("等待连接蓝牙");
                socket = bluetoothServerSocket.accept();
                L.i("同意连接设备");
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
}
