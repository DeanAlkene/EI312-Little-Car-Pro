package com.EI312.carapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.WildcardType;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    SurfaceView sView;
    SurfaceHolder sHolder;
    int WIDTH, HEIGHT;
    Camera camera;
    boolean isPreview = false;

    private static OutputStream os = null;
    private static Socket socket = null;
    private static String IP = "192.168.43.1"; //need to change
    private static String data;
    private Bitmap bmp = null;
    private static boolean socketStatus = false;
    private Matrix mtx = new Matrix();
    private ImageView img;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //Change
        HEIGHT = 640;
        WIDTH = 480;
        sView = (SurfaceView) findViewById(R.id.surfaceView);
        sHolder = sView.getHolder();

        sHolder.addCallback(new Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                init();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(camera != null) {
                    if (isPreview) {
                        camera.stopPreview();
                    }
                    camera.release();
                    camera = null;
                }
                System.exit(0);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    public void onAutoFocus(boolean success, Camera camera) {
                        if(success) {
                            init();
                            camera.cancelAutoFocus();
                        }
                    }
                });
            }
        });
        sHolder.setType(sHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void init() {
        if (!isPreview) {
            camera = Camera.open();
        }
        if (camera != null && !isPreview) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(WIDTH, HEIGHT);
                parameters.setPreviewFpsRange(20, 30);
                parameters.setPictureFormat(ImageFormat.NV21);
                parameters.setPictureSize(WIDTH, HEIGHT);
                camera.setPreviewDisplay(sHolder);
                camera.setPreviewCallback(new imageStream(IP));
                camera.startPreview();
//                camera.setDisplayOrientation(90);
                camera.autoFocus(null);

            } catch (Exception e) {
                e.printStackTrace();
            }
            isPreview = true;
        }
    }

    class imageStream implements Camera.PreviewCallback {
        private String ip = "192.168.43.1"; //need to be changed

        public imageStream(String ip) { this.ip = ip; }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Size size = camera.getParameters().getPreviewSize();
            try {
                YuvImage img = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                if(img != null) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    img.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, outputStream);
                    Thread t = new sendThread(outputStream, ip);
                    t.start();
                }
            } catch (Exception e) {
                Log.e("Sys", "ERROR:" + e.getMessage());
            }
        }
    }

    class sendThread extends Thread {
        private byte buf[] = new byte[1024];
        private OutputStream outputStream;
        private ByteArrayOutputStream byteArrayOutputStream;
        private String ip = "192.168.43.1"; //need to be changed

        public sendThread(ByteArrayOutputStream outStream, String ip) {
            this.byteArrayOutputStream = outStream;
            this.ip = ip;

            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                Socket s = new Socket(ip, 8000);
                outputStream = s.getOutputStream();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                int num;
                while ((num = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, num);
                }
                outputStream.flush();
                outputStream.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

