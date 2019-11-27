package com.example.speech;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends Thread{
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> deviceSet;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private BufferedOutputStream outStream;
    private Handler in;
    private Handler out;
    private Message outMsg;

    public Bluetooth(Handler out) {
        this.out = out;
        BA = BluetoothAdapter.getDefaultAdapter();
}

    public void setOut(Handler out) {
        this.out = out;
    }

    public Handler getIn() {
        return in;
    }

    @Override
    public void run() {
        super.run();
        if(!BA.isEnabled()) {
            BA.enable();
        }
        deviceSet = BA.getBondedDevices();
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        if(deviceSet.size() != 0) {
            device = deviceSet.iterator().next();
            try {
                socket = device.createRfcommSocketToServiceRecord(uuid);
                socket.connect();
                outStream = new BufferedOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
                outMsg = new Message();
                outMsg.what = 0x0;
                outMsg.obj = "Bluetooth Connection Failed";
                out.sendMessage(outMsg);
                return;
            }
            outMsg = new Message();
            outMsg.what = 0x0;
            outMsg.obj = "Bluetooth Connected Successfully";
            out.sendMessage(outMsg);

            Looper.prepare();
            in = new Handler() {
                @Override
                public void handleMessage(Message inMsg) {
                    try {
                        if(inMsg.what == 0x1) {
                            String operation = inMsg.obj.toString();
                            for(int i = 0; i < operation.length(); ++i) {
                                outStream.write(operation.charAt(i));
                            }
                            outStream.flush();
                        }
                        else if(inMsg.what == 0x2) {
                            getLooper().quit();
                        }
                    } catch (Exception e) {
                        getLooper().quit();
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outMsg = new Message();
            outMsg.what = 0x0;
            outMsg.obj = "Bluetooth Connection Suspended";
            out.sendMessage(outMsg);
        } else {
            Message msg = new Message();
            msg.what = 0x0;
            msg.obj = "Bluetooth Connection Failed";
            out.sendMessage(msg);
        }
    }
}
