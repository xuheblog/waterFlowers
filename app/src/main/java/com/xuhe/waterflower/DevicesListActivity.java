package com.xuhe.waterflower;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class DevicesListActivity extends AppCompatActivity {

    private ListView paired_devices,scanned_devices;
    private Button scan_btn;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ArrayAdapter<String> mArrayAdapter,mNewArrayAdapter;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static String EXTRA_DEVICE_ADDRESS = "设备地址";
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private BroadcastReceiver mReceiver;
    private String TAG = "DevicesListActivity";
    private BluetoothDevice device;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);
        Log.d(TAG,"onCreate()");
        //获取控件
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
        scan_btn = (Button) findViewById(R.id.scan);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        paired_devices = (ListView) findViewById(R.id.paired_devices);
        scanned_devices = (ListView) findViewById(R.id.scan_devices);
        mArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.generate_text);
        mNewArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.generate_text);
        paired_devices.setAdapter(mArrayAdapter);
        scanned_devices.setAdapter(mNewArrayAdapter);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                 String action = intent.getAction();
                //当发现一个设备时
                if (BluetoothDevice.ACTION_FOUND.equals(action)){
                    //从intent获得一个Bluetoothdevice
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //在listview中显示设备名称和mac地址
                    mNewArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//搜索设备
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行为扫描模式改变
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态改变
        Log.d(TAG,"注册广播");
        registerReceiver(mReceiver,intentFilter);//不要忘了取消注册

        //获取手机已经配对好的蓝牙设备
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        bluetoothDevices = new ArrayList<BluetoothDevice>();
        if (pairedDevices.size() > 0){
            for (BluetoothDevice device : pairedDevices){
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                bluetoothDevices.add(device);
            }
        }
        paired_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DevicesListActivity.this.bluetoothAdapter.cancelDiscovery();
                scan_btn.setText(R.string.scan);
                String address = ((TextView) view).getText().toString();
                address = address.substring(address.length() - 17, address.length());
                DevicesListActivity.this.sendData(address);
            }
        });
        scanned_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DevicesListActivity.this.bluetoothAdapter.cancelDiscovery();
                scan_btn.setText(R.string.scan);
                String address = ((TextView) view).getText().toString();
                address = address.substring(address.length() - 17, address.length());
                device = adapter.getRemoteDevice(address);
                /*判断是否配对*/
                switch (device.getBondState()){
                    //未配对
                    case BluetoothDevice.BOND_NONE:
                        //配对
                        try {
                            Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                            try {
                                createBondMethod.invoke(device);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                        break;
                    //已配对
                    case BluetoothDevice.BOND_BONDED:
                        //连接
                        DevicesListActivity.this.sendData(address);
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Toast.makeText(DevicesListActivity.this,"正在配对",Toast.LENGTH_SHORT).show();
                        break;
                }

                /*DevicesListActivity.this.sendData(address);*/
            }
        });
    }

    private void sendData(String address) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS,address);
        setResult(2,intent);
        finish();
    }

    public void Scan(View view) {

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
            return;
        }
        doDiscovery();
    }

    private void doDiscovery() {
        this.scan_btn.setText("停止搜索");
        if (this.bluetoothAdapter.isDiscovering()){
            this.scan_btn.setText(R.string.scan);
            progressBar.setVisibility(View.INVISIBLE);
            this.bluetoothAdapter.cancelDiscovery();
        }else {
            progressBar.setVisibility(View.VISIBLE);
            this.bluetoothAdapter.startDiscovery();
        }
    }

    public void Cancel(View view) {
        setResult(1,null);
        DevicesListActivity.this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                setResult(1,null);
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    protected void onDestroy()
    {
        super.onDestroy();
        if (this.bluetoothAdapter != null) {
            progressBar.setVisibility(View.INVISIBLE);
            this.bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(this.mReceiver);
    }
}
