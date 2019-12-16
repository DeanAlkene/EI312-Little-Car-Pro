package com.example.speech;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.LocaleData;
import android.os.Message;
import android.util.Log;
import android.os.Handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ServiceConfigurationError;

public class Receive implements Runnable {
    public Socket s;
    public ServerSocket ss;

    private Handler handler;

    private Bitmap bitmap;
    private static final int COMPLETED = 0x111;

    public Receive(Handler handler) { this.handler = handler; }

    public void run()
    {
        byte [] buf = new byte[1024];
        int len = 0;

        try{
            ss = new ServerSocket(8000);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        InputStream in = null;
        while (true) {
            try {
                s = ss.accept();

                Log.e("start", "Start socket");
                in = s.getInputStream();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                in.close();
                byte data[] = out.toByteArray();
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                Message msg = handler.obtainMessage();
                msg.what = 0x1;
                msg.obj = bitmap;
                handler.sendMessage(msg);

                out.flush();
                out.close();
                if(!s.isClosed()) {
                    s.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
}
