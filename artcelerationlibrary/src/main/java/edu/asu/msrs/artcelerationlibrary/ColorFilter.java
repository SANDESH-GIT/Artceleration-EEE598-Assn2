/**
 * Thread which handles all requests for ColorFilter transform.
 * Parameters: Messenger messenger, Bitmap input, int requestNo, MemoryFile memoryFile
 * Sends a message to the handler in Library after processing the image using its Messenger object to
 * let the library know that the processed Bitmap output image is ready.
 */

package edu.asu.msrs.artcelerationlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.MemoryFile;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.ByteArrayOutputStream;


public class ColorFilter implements Runnable {
    private Bitmap input;
    private Messenger messenger;
    private int requestNo;
    private MemoryFile memoryFile;


    ColorFilter(Messenger messenger, Bitmap input, int requestNo, MemoryFile memoryFile) {
        this.messenger = messenger;
        this.input = input;
        this.requestNo = requestNo;
        this.memoryFile = memoryFile;
    }


    /**
     * Run method includes all the transform logic for processing the input Bitmap image.
     * Writes the output image to the Memory File and shares the file descriptor of this file
     * with the library.
     */
    @Override
    public void run() {
        // TODO transform Logic
        Log.d("fd", "Color Filter!");

        // Image size, w-> width & h->height
        int w = input.getWidth();
        int h = input.getHeight();

        // Creating bitmap to be returned as a modified (mutable output bitmap)
        Bitmap output = Bitmap.createBitmap(w,h,input.getConfig());

        // Image represented by 4-bytes (4 channels as A,R, G, B)
        int a, r, g, b;
        int pix;

        // Iterate over all pixels
        for(int i=0; i< w; i++){
            for(int j=0; j<h; j++){
                pix=input.getPixel(i,j); // getPixel returns an integer value of the color of pixel

                // Filtering for every channel a,r,g,b
                a= Color.alpha(pix);

                // TODO: red, green, blue parameter needed from scroll down menu (either float array or integer array)
                r=(int)(Color.red(pix)* 0.5);
                g=(int)(Color.green(pix)* 0.6);
                b=(int)(Color.blue(pix)* 0.3);

                output.setPixel(i,j,Color.argb(a,r,g,b));
            }
        }

        input = output; //Writing back modified image to input image

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            input.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] oparray = outputStream.toByteArray();
            memoryFile.getOutputStream().write(oparray);

            Bundle bundle = new Bundle();
            Message message = Message.obtain(null, 10, oparray.length, requestNo);
            ParcelFileDescriptor pfd = MemoryFileUtil.getParcelFileDescriptor(memoryFile);
            bundle.putParcelable("ClassPFD", pfd);
            message.setData(bundle);

            messenger.send(message);
            //Thread.sleep(1000,0);
            memoryFile.allowPurging(true);
            memoryFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
