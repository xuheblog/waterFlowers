package com.xuhe.waterflower;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /*需要更新的UI*/
    private TextView Temperature,Temperature_tip,
                    Humidity_one,Humidity_one_tip,
                    Humidity_two,Humidity_two_tip;

    private SharedHelper sh;
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton fab;
    private TextView set_humidity_one;
    private TextView set_humidity_one_high;
    private TextView set_humidity_two;
    private TextView set_humidity_two_high;
    private BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private InputStream is;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String data = (String) msg.obj;
            MainActivity.this.disPlayResult(data);
        }
    };
    private int ACTION_REQUEST_BT = 2;

    //更新UI，展示温度、湿度、提示信息
    private void disPlayResult(String data) {
        //测试接收的数据
        float num = toFloat(data);
        if (num > 200f){
            //显示湿度2
            Humidity_two.setText(data.substring(data.length() - 2,data.length()) + "%");
            //湿度2小于设定值
            if (num - 200 < Float.parseFloat(set_humidity_two.getText().toString().substring(0,2))){
                Humidity_two_tip.setText("好干啊，浇点水吧");
            }else if (num - 200 > Float.parseFloat(set_humidity_two_high.getText().toString().substring(0,2)) ){
                Humidity_two_tip.setText("好潮啊，好潮啊");
            }else{
                Humidity_two_tip.setText("刚刚好");
            }
        }else if (num > 100f){
            //显示湿度1
            Humidity_one.setText(data.substring(data.length() - 2,data.length()) + "%");
            //湿度1小于设定值
            if (num - 100 < Float.parseFloat(set_humidity_one.getText().toString().substring(0,2))) {
                Humidity_one_tip.setText("好干啊，浇点水吧");
            }else if (num - 100 > Float.parseFloat(set_humidity_one_high.getText().toString().substring(0,2)) ){
                Humidity_one_tip.setText("好潮啊，好潮啊");
            }else{
                Humidity_one_tip.setText("刚刚好");
            }
        }else {
            //显示温度
            Temperature.setText(data + "℃");
            //温度的提示
            if (num > 25f){
                Temperature_tip.setText("温度高了");
            }else if(num < 10f){
                Temperature_tip.setText("温度低了");
            }else{
                Temperature_tip.setText("真舒服");
            }
        }
    }

    public static float toFloat(String str ,float defaultValue){
        if(str==null){
            return defaultValue;
        }
        try{
            return Float.parseFloat(str);
        }catch(Exception e){
        }
        return defaultValue;
    }

    public static float toFloat(String str ){
        return toFloat(str,0.0F);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取各个控件

        /*需更新的UI*/

        Temperature = (TextView) findViewById(R.id.temperature);
        Temperature_tip = (TextView) findViewById(R.id.temperature_tip);
        Humidity_one = (TextView) findViewById(R.id.humidity_one);
        Humidity_two = (TextView) findViewById(R.id.humidity_two);
        Humidity_one_tip = (TextView) findViewById(R.id.humidity_one_tip);
        Humidity_two_tip = (TextView) findViewById(R.id.humidity_two_tip);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        set_humidity_one = (TextView) findViewById(R.id.set_humidity_one);
        set_humidity_one_high = (TextView) findViewById(R.id.set_humidity_one_high);
        set_humidity_two = (TextView) findViewById(R.id.set_humidity_two);
        set_humidity_two_high = (TextView) findViewById(R.id.set_humidity_two_high);
        Context mContext = getApplicationContext();
        sh = new SharedHelper(mContext);

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                final EditText editText = new EditText(MainActivity.this);
                switch (item.getItemId()){
                    case R.id.nav_setting_one:
                        new AlertDialog.Builder(MainActivity.this).setTitle("植物1湿度下限设定").setIcon(
                                android.R.drawable.ic_menu_edit).setView(
                                editText).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                set_humidity_one.setText(editText.getText().toString() + "%");
                                String humidity_one = editText.getText().toString();
                                sh.save(humidity_one, null,null,null);
                                mDrawerLayout.closeDrawers();
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
                        break;
                    case R.id.nav_setting_two:
                        new AlertDialog.Builder(MainActivity.this).setTitle("植物2湿度下限设定").setIcon(
                                android.R.drawable.ic_menu_edit).setView(
                                editText).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                set_humidity_two.setText(editText.getText().toString() + "%");
                                String humidity_two = editText.getText().toString();
                                sh.save(null,humidity_two,null,null);
                                mDrawerLayout.closeDrawers();
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
                        break;
                    case R.id.nav_setting_one_high:
                        new AlertDialog.Builder(MainActivity.this).setTitle("植物1湿度上限设定").setIcon(
                                android.R.drawable.ic_menu_edit).setView(
                                editText).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                set_humidity_one_high.setText(editText.getText().toString() + "%");
                                String humidity_one_high = editText.getText().toString();
                                sh.save(null,null,humidity_one_high, null);
                                mDrawerLayout.closeDrawers();
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
                        break;
                    case R.id.nav_setting_two_high:
                        new AlertDialog.Builder(MainActivity.this).setTitle("植物2湿度上限设定").setIcon(
                                android.R.drawable.ic_menu_edit).setView(
                                editText).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                set_humidity_two_high.setText(editText.getText().toString() + "%");
                                String humidity_two_high = editText.getText().toString();
                                sh.save(null,null,null,humidity_two_high);
                                mDrawerLayout.closeDrawers();
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
                        break;
                    case R.id.share:
                        AlertDialog alertDialog_about = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog_about.show();
                        Window window_about = alertDialog_about.getWindow();
                        window_about.setContentView(R.layout.about);
                        break;
                    case R.id.about:
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.show();
                        Window window = alertDialog.getWindow();
                        window.setContentView(R.layout.function);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

    }

    //连接蓝牙设备
    private void connect() {
        if (!bluetooth.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,ACTION_REQUEST_BT);
            return;
        }
        if (this.socket == null) {
            startActivityForResult(new Intent(MainActivity.this, DevicesListActivity.class), 1);
            return;
        }
        disconnect();
    }

    private void disconnect() {
        try {
            socket.close();
            socket = null;
            fab.setImageResource(R.drawable.disconnect);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Map<String, String> data = sh.read();
        if (data.get("humidity_one") != null)
            set_humidity_one.setText(data.get("humidity_one").trim() + "%");
        if (data.get("humidity_two") != null)
            set_humidity_two.setText(data.get("humidity_two").trim() + "%");
        if (data.get("humidity_one_high") != null)
            set_humidity_one_high.setText(data.get("humidity_one_high").trim() + "%");
        if (data.get("humidity_two_high") != null)
            set_humidity_two_high.setText(data.get("humidity_two_high").trim() + "%");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case RESULT_OK:
                startActivityForResult(new Intent(MainActivity.this, DevicesListActivity.class), 1);
                break;
            case RESULT_CANCELED:
                Toast.makeText(MainActivity.this,"开启蓝牙失败",Toast.LENGTH_SHORT).show();
                break;
            case 2 :
                String device_address = data.getExtras().getString(DevicesListActivity.EXTRA_DEVICE_ADDRESS);
                this.device = bluetooth.getRemoteDevice(device_address);
                /*if (device.getBondState() != BluetoothDevice.BOND_BONDED){
                    Toast.makeText(MainActivity.this,"未配对",Toast.LENGTH_SHORT).show();
                    break;
                }*/
                try {
                    socket = this.device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this,"创建socket失败",Toast.LENGTH_SHORT).show();
                }
                try {
                    socket.connect();
                    Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
                    fab.setImageResource(R.drawable.bluetooth);
                    is = socket.getInputStream();
                    //打开接收数据子线程
                    ReadThread readThread = new ReadThread(is);
                    readThread.start();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
                    try {
                        socket.close();
                    } catch (IOException e1) { }
                    socket = null;
                    device = null;
                }
                break;
            case 1:
                Toast.makeText(MainActivity.this,"没有选择要连接的蓝牙设备",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private class ReadThread extends Thread{
        private InputStream mmInStream;

        @Override
        public void run() {
            //接收数据子线程
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    if (is.available() != 0) {
                        //完整接收
                        if (is.available() > 3){
                       bytes = mmInStream.read(buffer);
                            String s = new String(buffer,0,bytes);
                            Message msg = new Message();
                            msg.obj = s.substring(0,bytes).trim();
                            handler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public ReadThread(InputStream is) {
            mmInStream = is;
        }
    }
}
