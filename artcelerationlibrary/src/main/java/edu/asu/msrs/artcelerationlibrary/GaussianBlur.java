/**
 * Thread which handles all requests for Gaussian Blur transform.
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


public class GaussianBlur implements Runnable {
    private Bitmap input;
    private Messenger messenger;
    private int requestNo;
    private MemoryFile memoryFile;


    GaussianBlur(Messenger messenger, Bitmap input, int requestNo, MemoryFile memoryFile) {
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
        Log.d("fd", "Gaussian Blur!");

        int rad = 5;
        float sd = 2f;
        int size = 2*rad+1;

        // Image size, w-> width & h->height
        int w = input.getWidth();
        int h = input.getHeight();

        // Creating bitmap to be returned as a modified (mutable output bitmap)
        Bitmap output = Bitmap.createBitmap(w,h,input.getConfig());

        double[] G = new double[size];
        double[][] qr = new double[w][h];
        double[][] qg = new double[w][h];
        double[][] qb = new double[w][h];
        int[][] Pr = new int[w][h];
        int[][] Pg = new int[w][h];
        int[][] Pb = new int[w][h];

        for(int i=0; i<size; i++){
            G[i]= (Math.exp(-(Math.pow(i-rad,2))/(2*Math.pow(sd,2)))/Math.sqrt(2*(Math.PI)*sd*sd));
            Log.d("Gaussian Blur: ", "Calculating kernel"+G[i]);
        }

        for (int i=0;i<w;i++) {
            for (int j = 0; j < h; j++) {
                for (int k=0;k<size;k++) {
                    int xval = i-rad+k;
                    if(!(xval<0 || xval>=w)){
                        qr[i][j] += G[k] * Color.red(input.getPixel(xval,j));
                        qg[i][j] += G[k] * Color.green(input.getPixel(xval,j));
                        qb[i][j] += G[k] * Color.blue(input.getPixel(xval,j));
                    }
                }
            }
        }

        for (int i=0;i<w;i++) {
            for (int j = 0; j < h; j++) {
                for (int k=0;k<size;k++) {
                    int yval = j-rad+k;
                    if(!(yval<0 || yval>=h)){
                        Pr[i][j] += G[k] * qr[i][yval];
                        Pg[i][j] += G[k] * qg[i][yval];
                        Pb[i][j] += G[k] * qb[i][yval];
                        //Log.d("Gaussian Blur","One pixel modified");
                    }
                }
                output.setPixel(i,j,Color.argb(255,Pr[i][j],Pg[i][j],Pb[i][j]));
            }
        }

        Log.d("Gaussian Blur","Done processing...!!!");

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            output.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
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
