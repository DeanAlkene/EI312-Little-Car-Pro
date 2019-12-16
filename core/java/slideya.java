package com.example.speech;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

public class slideya extends Activity implements
        GestureDetector.OnGestureListener {
    Bluetooth bluetooth;

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth = ((MyApp) getApplication()).getBluetooth();
        if (bluetooth != null) {
            bluetooth.setOut(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0x0) {
                        Intent intent = new Intent(slideya.this, MainActivity.class);
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
            Intent intent = new Intent(slideya.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("Info", "Bluetooth Connection Suspended");
            startActivity(intent);
        }
    }

    // 定义手势检测器实例
    GestureDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);

        // 创建手势检测器
        detector = new GestureDetector(this, this);
    }

    // 将该activity上的触碰事件交给GestureDetector处理
    public boolean onTouchEvent(MotionEvent me) {
        return detector.onTouchEvent(me);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    /**
     * 滑屏监测
     *
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        float minMove = 120; // 最小滑动距离
        float minVelocity = 0; // 最小滑动速度
        float beginX = e1.getX();
        float endX = e2.getX();
        float beginY = e1.getY();
        float endY = e2.getY();

        if (beginX - endX > minMove && Math.abs(velocityX) > minVelocity) { // 左滑
            Toast.makeText(this, velocityX + "左滑", Toast.LENGTH_SHORT).show();

            sendOperation("A");
        } else if (endX - beginX > minMove && Math.abs(velocityX) > minVelocity) { // 右滑
            Toast.makeText(this, velocityX + "右滑", Toast.LENGTH_SHORT).show();

            sendOperation("D");
        } else if (beginY - endY > minMove && Math.abs(velocityY) > minVelocity) { // 上滑
            Toast.makeText(this, velocityX + "上滑", Toast.LENGTH_SHORT).show();

            sendOperation("W");
        } else if (endY - beginY > minMove && Math.abs(velocityY) > minVelocity) { // 下滑
            Toast.makeText(this, velocityX + "下滑", Toast.LENGTH_SHORT).show();

            sendOperation("S");
        }

        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX,
                            float velocityY) {

        return false;
    }
}
