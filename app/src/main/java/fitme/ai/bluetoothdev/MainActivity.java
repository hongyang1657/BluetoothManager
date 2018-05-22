package fitme.ai.bluetoothdev;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.view.View;

import fitme.ai.bluetoothdev.utils.BlueToothManager;

public class MainActivity extends Activity {

    private static BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void click(View view){
        switch (view.getId()){
            case R.id.bt_open:
                break;
            case R.id.bt_close:
                break;
            case R.id.bt_search:
                break;
        }
    }
}
