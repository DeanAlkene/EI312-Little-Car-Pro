package com.example.speech;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class Mannual extends AppCompatActivity {

    public static ImageView img;
    private static Bitmap bitmap;

    private Bluetooth bluetooth;
    private Receive rev;
    private SpeechControl.bitmapHandler handler;
    static int num = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_layout);
    };
}