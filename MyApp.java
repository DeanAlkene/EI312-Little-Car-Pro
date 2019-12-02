package com.example.myapplication;

import android.app.Application;
public class MyApp extends Application {

    private Bluetooth bluetooth;



    public void setBluetooth(Bluetooth bluetooth) {

        this.bluetooth = bluetooth;

    }



    public Bluetooth getBluetooth() {

        return bluetooth;

    }

}
