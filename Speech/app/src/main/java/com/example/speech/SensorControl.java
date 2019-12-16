package com.example.speech;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

public class SensorControl extends AppCompatActivity {
    public static ImageView img;
    private static Bitmap bitmap;

    private Bluetooth bluetooth;
    private float delta=0;
    public SensorManager mSensorManager;
    public Sensor mSensor;
    private int lock=0;
    char controlSig='S';
    int set=0;

    private Receive rev;
    private bitmapHandler handler;

    Bitmap srcBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_control);
        mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor=mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        img = (ImageView) findViewById(R.id.camera);
        handler = new bitmapHandler();
        rev = new Receive(handler);
        new Thread(rev).start();

        Button takePhoto = (Button) findViewById(R.id.photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { savePhoto(SensorControl.this, bitmap, "Pic"); }
        });

        Button forwardButton = (Button) findViewById(R.id.forward);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    forward(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button backwardButton = (Button) findViewById(R.id.backward);
        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    backward(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button setButton = (Button) findViewById(R.id.set);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    set(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
        bluetooth = ((MyApp) getApplication()).getBluetooth();
        if(bluetooth != null) {
            bluetooth.setOut(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == 0x0) {
                        Intent intent = new Intent(SensorControl.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("Info", msg.obj.toString());
                        startActivity(intent);
                    }
                }
            });
        }
    }

    public void sendOperation(String op) {
        if(bluetooth.isAlive()) {
            Message msg = new Message();
            msg.what = 0x1;
            msg.obj = op;
            bluetooth.getIn().sendMessage(msg);
        }
        else {
            Intent intent = new Intent(SensorControl.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("Info", "Bluetooth Connection Suspended");
            startActivity(intent);
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
                sendOperation(String.valueOf(controlSig));
            }
            else if(degree>20&&degree<30){
                //lock=1;
                if(controlSig=='W') controlSig='E';
                else if(controlSig=='X') controlSig='C';
                text.setText(String.valueOf(controlSig));
                sendOperation(String.valueOf(controlSig));
            }
            else if(degree>=30&&degree<180){
                //lock=1;
                if(controlSig=='E') controlSig='D';
                else if(controlSig=='X') controlSig='C';
                text.setText(String.valueOf(controlSig));
                sendOperation(String.valueOf(controlSig));
            }
            else if(degree<340&&degree>=330){
                //lock=1;
                if(controlSig=='W') controlSig='Q';
                else if(controlSig=='X') controlSig='Z';
                text.setText(String.valueOf(controlSig));
                sendOperation(String.valueOf(controlSig));
            }
            else{
                if(controlSig=='A'||controlSig=='D'||controlSig=='Q'||controlSig=='E') controlSig='W';
                if(controlSig=='Z'||controlSig=='C') controlSig='X';
                text.setText(String.valueOf(controlSig));
                sendOperation(String.valueOf(controlSig));
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
            sendOperation(String.valueOf(controlSig));
        }
    }

    public void backward(View v) throws IOException {
        if(lock==0) {
            controlSig = 'X';
            TextView text = (TextView) findViewById(R.id.text);
            text.setText(String.valueOf(controlSig));
            sendOperation(String.valueOf(controlSig));
        }
    }

    public void set(View v) throws IOException {
        set=1;
        controlSig='S';
        TextView text = (TextView) findViewById(R.id.text);
        text.setText(String.valueOf(controlSig));
        sendOperation(String.valueOf(controlSig));
    }

    static class bitmapHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x1) {
                bitmap = (Bitmap) msg.obj;
                bitmap = rotate(90.0f, bitmap);
                img.setImageBitmap(bitmap);
                super.handleMessage(msg);
            }
        }
    }

    public static Bitmap rotate(float angle, Bitmap bitmap) {
        Matrix mat = new Matrix();
        mat.postRotate(angle);

        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);

        return newBitmap;
    }

    public static void savePhoto(Context context, Bitmap bitmap, String name) {
        String fileName = null;
        MainActivity.picNum++;
        name = name + String.valueOf(MainActivity.picNum);

        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;

        File file = null;
        FileOutputStream out = null;

        try {
            file = new File(galleryPath, name + ".jpg");
            fileName = file.toString();
            out = new FileOutputStream(fileName);
            if(out != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            }
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, fileName, null);
        Uri uri = Uri.fromFile(file);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        Toast.makeText(context, "照片已储存", Toast.LENGTH_SHORT);
    }
}