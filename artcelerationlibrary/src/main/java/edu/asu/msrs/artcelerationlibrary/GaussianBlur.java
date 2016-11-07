package edu.asu.msrs.artcelerationlibrary;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by pavilion on 06-11-2016.
 */
public class GaussianBlur implements Runnable {
    private Bitmap output;
    private Bitmap input;
    private Messenger messenger;
    private int requestNo;
    private ParcelFileDescriptor fd;

    GaussianBlur(Messenger messenger, Bitmap input, int requestNo, ParcelFileDescriptor fd) {
        this.messenger = messenger;
        this.input = input;
        this.requestNo = requestNo;
        this.fd = fd;
    }

    @Override
    public void run() {
        output = input;
        // TODO transform Logic
        Log.d("fd", "Gaussian Blur!");
        try {
            /*
            serviceMemFile.getOutputStream().write(oparray);

            Bundle bundle = new Bundle();
            Message message = Message.obtain(null, 10, oparray.length, requestNo);
            bundle.putParcelable("ServicePFD", fd);
            message.setData(bundle);
            messenger.send(message);
            serviceMemFile.allowPurging(true);
            serviceMemFile.close();
            */
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
