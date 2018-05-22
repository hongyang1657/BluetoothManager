package fitme.ai.bluetoothdev;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;

import fitme.ai.bluetoothdev.utils.BlueToothManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void click(View view){
        switch (view.getId()){
            case R.id.bt_open:
                BlueToothManager.getInstance(this).openBlueTooth();
                break;
            case R.id.bt_close:
                BlueToothManager.getInstance(this).closeBlueTooth();
                break;
            case R.id.bt_search:
                break;
        }
    }
}
