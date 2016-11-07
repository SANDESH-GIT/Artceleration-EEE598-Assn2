package edu.asu.msrs.artcelerationlibrary;

import android.graphics.Bitmap;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by pavilion on 06-11-2016.
 */
public class SobelEdgeFilter implements Runnable {
    private Bitmap output;
    private Bitmap input;
    private Messenger messenger;
    private int requestNo;
    private ParcelFileDescriptor fd;

    SobelEdgeFilter(Messenger messenger, Bitmap input, int requestNo, ParcelFileDescriptor fd) {
        this.messenger = messenger;
        this.input = input;
        this.requestNo = requestNo;
        this.fd = fd;
    }

    @Override
    public void run() {
        output = input;
        // TODO transform Logic

            /*
            MyTransformHandler m = (MyTransformHandler) (queue.peek());
            if (m.getRequestNo() == requestNo) {
                transformHandler.onTransformProcessed(output);
                queue.poll();
                break;
            }
            */



    }
}
