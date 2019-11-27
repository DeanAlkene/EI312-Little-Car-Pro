package com.EI312.carapp;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

public class IPAddr {
    public static String IP;
    public static int PORT;

    public static String getIP() { return IP; }
    public static int getPORT() { return PORT; }
    public static void getLocalIPAddr(ServerSocket ss) {
        try {
            for(Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces(); i.hasMoreElements();) {
                NetworkInterface intFace = i.nextElement();
                for(Enumeration<InetAddress> ipAddr = intFace.getInetAddresses(); ipAddr.hasMoreElements();) {
                    InetAddress inetAddress = ipAddr.nextElement();
                    String ip = inetAddress.getHostAddress().substring(0, 3);
                    if(ip.equals("192")) {
                        IP = inetAddress.getHostAddress();
                        PORT = ss.getLocalPort();
                        Log.e("IP", ""+IP);
                        Log.e("PORT",""+PORT);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
