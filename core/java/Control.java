package com.example.speech;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import org.w3c.dom.Text;

import java.io.IOException;

public class Control extends AppCompatActivity {

    private Bluetooth bluetooth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);

    }
    public void forward(View v) throws IOException {
        TextView text=(TextView) findViewById(R.id.tv_key_state);
        text.setText(String.valueOf('W'));
        sendOperation(String.valueOf('W'));
    }
    public void back(View v) throws IOException {
        TextView text=(TextView) findViewById(R.id.tv_key_state);
        text.setText(String.valueOf('X'));
        sendOperation(String.valueOf('X'));
    }

    public void left(View v) throws IOException {
        TextView text=(TextView) findViewById(R.id.tv_key_state);
        text.setText(String.valueOf('A'));
        sendOperation(String.valueOf('A'));
    }

    public void right(View v) throws IOException {
        TextView text=(TextView) findViewById(R.id.tv_key_state);
        text.setText(String.valueOf('D'));
        sendOperation(String.valueOf('D'));
    }
    public void leftup(View v) throws IOException {
        TextView text=(TextView) findViewById(R.id.tv_key_state);
        text.setText(String.valueOf('Q'));
        sendOperation(String.valueOf('Q'));
    }
    public void rightup(View v) throws IOException {
        TextView text=(TextView) findViewById(R.id.tv_key_state);
        text.setText(String.valueOf('E'));
        sendOperation(String.valueOf('E'));
    }
    public void stop(View v) throws IOException {
        TextView text=(TextView) findViewById(R.id.tv_key_state);
        text.setText(String.valueOf('S'));
        sendOperation(String.valueOf('S'));
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
                        Intent intent = new Intent(Control.this, MainActivity.class);
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
            Intent intent = new Intent(Control.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("Info", "Bluetooth Connection Suspended");
            startActivity(intent);
        }
    }
}