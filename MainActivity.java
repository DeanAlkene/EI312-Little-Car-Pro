package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends AppCompatActivity {
    private Bluetooth bluetooth;
    private float delta=0;
    public SensorManager mSensorManager;
    public Sensor mSensor;
    private int lock=0;
    char controlSig='S';
    int set=0;
    BluetoothDevice device;
    BluetoothSocket socket;
    OutputStream os;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor=mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener,mSensor,SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onStart() {
        super.onStart();


        BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> deviceSet = BA.getBondedDevices();
        if(deviceSet.size() == 0) {
            TextView text=(TextView) findViewById(R.id.text);
            text.setText(String.valueOf('N'));
        }
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        device = deviceSet.iterator().next();
        try {

            socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);

            //socket = device.createRfcommSocketToServiceRecord(uuid);

            BA.cancelDiscovery();

            socket.connect();
            os=socket.getOutputStream();
            //os.write(String.valueOf('W').getBytes());
            //os.flush();

        } catch (Exception e) {

            TextView text=(TextView) findViewById(R.id.text);
            text.setText(String.valueOf('N'));

        }

        /**bluetooth = ((MyApp) getApplication()).getBluetooth();
        if(bluetooth != null) {
            bluetooth.setOut(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == 0x0) {
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("Info", msg.obj.toString());
                        startActivity(intent);
                    }
                }
            });
        }**/
    }

    /**public void sendOperation(String op) {
        if(bluetooth.isAlive()) {
            Message msg = new Message();
            msg.what = 0x1;
            msg.obj = op;
            bluetooth.getIn().sendMessage(msg);
        }
        else {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("Info", "Bluetooth Connection Suspended");
        startActivity(intent);
        }
    }**/

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendsig(char sig){
        try {
            os.write(String.valueOf(controlSig).getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SensorEventListener mSensorEventListener=new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            TextView text=(TextView) findViewById(R.id.text);
            if(set==1){
                delta=event.values[0];
                set=0;
            }
            float degree = (event.values[0]-delta+360)%360;
            // 创建旋转动画（反向转过degree度）
            if(degree<330&&degree>180){
                //lock=1;
                if(controlSig=='Q') controlSig='A';
                else if(controlSig=='X') controlSig='Z';
                text.setText(String.valueOf(controlSig));
                sendsig(controlSig);
                //sendOperation(String.valueOf(controlSig));
            }
            else if(degree>20&&degree<30){
                //lock=1;
                if(controlSig=='W') controlSig='E';
                else if(controlSig=='X') controlSig='C';
                text.setText(String.valueOf(controlSig));
                sendsig(controlSig);
                //sendOperation(String.valueOf(controlSig));
            }
            else if(degree>=30&&degree<180){
                //lock=1;
                if(controlSig=='E') controlSig='D';
                else if(controlSig=='X') controlSig='C';
                text.setText(String.valueOf(controlSig));
                sendsig(controlSig);
                //sendOperation(String.valueOf(controlSig));
            }
            else if(degree<340&&degree>=330){
                //lock=1;
                if(controlSig=='W') controlSig='Q';
                else if(controlSig=='X') controlSig='Z';
                text.setText(String.valueOf(controlSig));
                sendsig(controlSig);
                //sendOperation(String.valueOf(controlSig));
            }
            else{
                if(controlSig=='A'||controlSig=='D'||controlSig=='Q'||controlSig=='E') controlSig='W';
                if(controlSig=='Z'||controlSig=='C') controlSig='X';
                text.setText(String.valueOf(controlSig));
                sendsig(controlSig);
                //sendOperation(String.valueOf(controlSig));
                //lock=0;

            }
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };





    public void forward(View v) throws IOException {
        if(lock==0) {
            TextView text = (TextView) findViewById(R.id.text);
            controlSig='W';
            text.setText(String.valueOf(controlSig));
            sendsig(controlSig);
            //sendOperation(String.valueOf(controlSig));
        }
    }

    public void backward(View v) throws IOException {
        if(lock==0) {
            controlSig='X';
            TextView text = (TextView) findViewById(R.id.text);
            text.setText(String.valueOf(controlSig));
            sendsig(controlSig);
            //sendOperation(String.valueOf(controlSig));
        }
    }

    public void set(View v) throws IOException {
        set=1;
        controlSig='S';
        TextView text = (TextView) findViewById(R.id.text);
        text.setText(String.valueOf(controlSig));
        sendsig(controlSig);
    }
}
