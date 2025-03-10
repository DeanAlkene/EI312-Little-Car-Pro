package com.example.speech;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import android.content.Intent;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SpeechControl extends AppCompatActivity{
    public static ImageView img;
    private static Bitmap bitmap;

    private Bluetooth bluetooth;
    private Receive rev;
    private bitmapHandler handler;
    static int num = 0;

    Bitmap srcBitmap;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_control);
        img = (ImageView) findViewById(R.id.camera);
        handler = new bitmapHandler();
        rev = new Receive(handler);
        new Thread(rev).start();

        Button takePhoto = (Button) findViewById(R.id.photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { savePhoto(SpeechControl.this, bitmap, "Pic"); }
        });

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5dd7f6d0");
        Button startButton = (Button) findViewById(R.id.recog);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { init(SpeechControl.this); }
        });
    }

    public void init(final Context context) {
        RecognizerDialog mDialog = new RecognizerDialog(context, null);

        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        mDialog.setListener((new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                if(!b) {
                    Boolean isLegal = false;
                    String result = parse(recognizerResult.getResultString());

                    if(result.equals("前进")) {
                        sendOperation("W");
                        isLegal = true;
                    }
                    else if(result.equals("左转")) {
                        sendOperation("A");
                        isLegal = true;
                    }
                    else if(result.equals("右转")) {
                        sendOperation("D");
                        isLegal = true;
                    }
                    else if(result.equals("后退")) {
                        sendOperation("X");
                        isLegal = true;
                    }
                    else if(result.equals("停车")) {
                        sendOperation("S");
                        isLegal = true;
                    }
                    else if(result.equals("左前方")) {
                        sendOperation("Q");
                        isLegal = true;
                    }
                    else if(result.equals("右前方")) {
                        sendOperation("E");
                        isLegal = true;
                    }
                    else if(result.equals("左后方")) {
                        sendOperation("Z");
                        isLegal = true;
                    }
                    else if(result.equals("右后方")) {
                        sendOperation("C");
                        isLegal = true;
                    }
                    TextView txv = (TextView) findViewById(R.id.Result);
                    txv.setText(result);
                    if(!isLegal) {
                        Toast.makeText(SpeechControl.this, "无法识别指令，请重新识别", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        }));

        mDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth = ((MyApp) getApplication()).getBluetooth();
        if(bluetooth != null) {
            bluetooth.setOut(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == 0x0) {
                        Intent intent = new Intent(SpeechControl.this, MainActivity.class);
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
            Intent intent = new Intent(SpeechControl.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("Info", "Bluetooth Connection Suspended");
            startActivity(intent);
        }
    }

    public String parse(String result) {
        Gson gson = new Gson();
        Words bean = gson.fromJson(result, Words.class);
        StringBuffer buf = new StringBuffer();
        ArrayList<Words.WSBean> ws = bean.ws;
        for(Words.WSBean wsBean : ws) {
            String word = wsBean.cw.get(0).w;
            buf.append(word);
        }
        return buf.toString();
    }

    public class Words {
        public ArrayList<WSBean> ws;
        public class WSBean {
            public ArrayList<CWBean> cw;
        }

        public class CWBean {
            public String w;
        }
    }

    static class bitmapHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x1) {
                bitmap = (Bitmap) msg.obj;
//                bitmap = rotate(90.0f, bitmap);
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
        num++;
        name = name + String.valueOf(num);

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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
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
