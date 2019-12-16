package com.example.speech;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector; //人脸识别的关键类
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.opencv.core.Point;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class faceDetect extends AppCompatActivity {
    public ImageView img;
    private Bitmap bitmap;

    private static final int MAX_FACE_NUM = 1;
    private int realFaceNum;
    private Paint paint;

    private Bluetooth bluetooth;
    private Receive2 rev;
    private bitmapHandler handler;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facedetect);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(10);  //线的宽度

        img = findViewById(R.id.face_camera);

        handler = new bitmapHandler();
        rev = new Receive2(handler);
        new Thread(rev).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth = ((MyApp) getApplication()).getBluetooth();
        if (bluetooth != null) {
            bluetooth.setOut(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0x0) {
                        Intent intent = new Intent(faceDetect.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("Info", msg.obj.toString());
                        startActivity(intent);
                    }
                }
            });
        }
    }

    public void sendOperation(String op) {
        if (bluetooth.isAlive()) {
            Message msg = new Message();
            msg.what = 0x1;
            msg.obj = op;
            bluetooth.getIn().sendMessage(msg);
        } else {
            Intent intent = new Intent(faceDetect.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("Info", "Bluetooth Connection Suspended");
            startActivity(intent);
        }
    }

    class bitmapHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                bitmap = (Bitmap) msg.obj;

                FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), MAX_FACE_NUM);
                FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACE_NUM];
                Bitmap mBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
                realFaceNum = faceDetector.findFaces(mBitmap, faces);

                drawFacesAreaAndSendOp(faces);

                super.handleMessage(msg);
            }
        }
    }
    private void drawFacesAreaAndSendOp(FaceDetector.Face[] faces) {
        Bitmap mbitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(mbitmap);

        int argMaxArea = -1;
        double maxEyesDis = 0;
        for (int i = 0; i < faces.length; i++) {
            FaceDetector.Face face = faces[i];
            if (face != null) {
                PointF pointF = new PointF();
                face.getMidPoint(pointF);
                float eyesDistance = face.eyesDistance();

                canvas.drawRect(pointF.x - eyesDistance, pointF.y - eyesDistance, pointF.x + eyesDistance, pointF.y + eyesDistance, paint);

                // find max face
                if (face.eyesDistance() > maxEyesDis) {
                    maxEyesDis = face.eyesDistance();
                    argMaxArea = i;
                }
            }

        }

        bitmap = mbitmap;
        img.setImageBitmap(bitmap);
        img.invalidate();

        // send op
        if (realFaceNum == 0) {
            sendOperation("S");
        } else {
            // tracking largest face
            PointF pointF = new PointF();
            faces[argMaxArea].getMidPoint(pointF);

            double locX = pointF.x;

            int w = bitmap.getWidth();

            if (locX / w < 0.2) {
                sendOperation("Q");
            } else if (locX / w < 0.8) {
                sendOperation("S");
            } else if (locX / w < 1.0) {
                sendOperation("E");
            }
        }
    }
}
