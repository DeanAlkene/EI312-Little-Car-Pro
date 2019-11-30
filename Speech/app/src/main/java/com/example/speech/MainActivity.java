package com.example.speech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    private Bluetooth bluetooth;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button speechButton = (Button) findViewById(R.id.speechButton);
        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentSpeech = new Intent(MainActivity.this, SpeechControl.class);
                startActivity(intentSpeech);
            }
        });

        Button testButton = (Button) findViewById(R.id.speechButton2);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentTest = new Intent(MainActivity.this, Test.class);
                startActivity(intentTest);
            }
        });

        preferences = getSharedPreferences("Speech", MODE_PRIVATE);
        editor = preferences.edit();
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
                        ((TextView) findViewById(R.id.bluetoothStatus)).setText(msg.obj.toString());
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        if(msg.obj.toString().equals("Bluetooth Connected Successfully")) {
                            ((ToggleButton) findViewById(R.id.BluetoothToggle)).setChecked(true);
                        } else {
                            ((ToggleButton) findViewById(R.id.BluetoothToggle)).setChecked(false);
                        }
                    }
                }
            });
        }
        ((TextView) findViewById(R.id.bluetoothStatus)).setText("");
        try {
            String Info = getIntent().getStringExtra("Info");
            getIntent().removeExtra("Info");
            ((TextView) findViewById(R.id.bluetoothStatus)).setText(Info);
            if(Info.equals("Bluetooth Connection Suspended")) {
                ((ToggleButton) findViewById(R.id.BluetoothToggle)).setChecked(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if(bluetooth != null) {
            if(bluetooth.isAlive()) {
                if(bluetooth.getIn() != null) {
                    Message msg = new Message();
                    msg.what = 0x2;
                    bluetooth.getIn().sendMessage(msg);
                }
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(bluetooth != null) {
                if(bluetooth.isAlive()) {
                    if(bluetooth.getIn() != null) {
                        Message msg = new Message();
                        msg.what = 0x2;
                        bluetooth.getIn().sendMessage(msg);
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void onBluetoothToggleButtonClick(View source) {
        ToggleButton toggleButton = (ToggleButton) source;
        if(toggleButton.isChecked()) {
            toggleButton.setChecked(false);
            if(bluetooth != null) {
                if(bluetooth.isAlive()) {
                    return;
                }
            }
            bluetooth = new Bluetooth(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == 0x0) {
                        ((TextView) findViewById(R.id.bluetoothStatus)).setText(msg.obj.toString());
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        if(msg.obj.toString().equals("Bluetooth Connected Successfully")) {
                            ((ToggleButton) findViewById(R.id.BluetoothToggle)).setChecked(true);
                        } else {
                            ((ToggleButton) findViewById(R.id.BluetoothToggle)).setChecked(false);
                        }
                    }
                }
            });
            ((MyApp) getApplication()).setBluetooth(bluetooth);
            ((TextView) findViewById(R.id.bluetoothStatus)).setText("Bluetooth is connecting...");
            ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
            bluetooth.start();
        } else {
            toggleButton.setChecked(true);
            if(bluetooth != null) {
                if(bluetooth.isAlive()) {
                    if(bluetooth.getIn() != null) {
                        Message msg = new Message();
                        msg.what = 0x2;
                        bluetooth.getIn().sendMessage(msg);
                    }
                }
            }
        }
    }
}
