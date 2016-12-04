/**
 * Thread which handles all requests for NeonEdges transform.
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


public class UnsharpMask implements Runnable {
    private Bitmap input;
    private Messenger messenger;
    private int requestNo;
    private MemoryFile memoryFile;
    private int intArgs[];
    private float floatArgs[];


    UnsharpMask(Messenger messenger, Bitmap input, int requestNo, MemoryFile memoryFile,int[] intArgs, float[] floatArgs) {
        this.messenger = messenger;
        this.input = input;
        this.requestNo = requestNo;
        this.memoryFile = memoryFile;
        this.intArgs =intArgs;
        this.floatArgs = floatArgs;
    }

    static {
        System.loadLibrary("UnsharpMaskLib");
    }

    /**
     * Run method includes all the transform logic for processing the input Bitmap image.
     * Writes the output image to the Memory File and shares the file descriptor of this file
     * with the library.
     */
    @Override
    public void run() {
        Log.d("fd", "Unsharp Mask!");
        int a0;
        //float b0 = 2f; // f0 or b0
        float b0 = floatArgs[0];
        a0 = (int)(6*b0);
        //float b1 = 0.1f;
        float b1 = floatArgs[1];
        // int size = 2*a0+1;

        // Image size, w-> width & h->height
        int w = input.getWidth();
        int h = input.getHeight();

        // Creating bitmap to be returned as a modified (mutable output bitmap)
        Bitmap output = Bitmap.createBitmap(w,h,input.getConfig());
        //Bitmap q = Bitmap.createBitmap(w,h,input.getConfig());
        /*
        float[] G = new float[size];
        float[][] qr = new float[w][h];
        float[][] qg = new float[w][h];
        float[][] qb = new float[w][h];
        int[][] Pr = new int[w][h];
        int[][] Pg = new int[w][h];
        int[][] Pb = new int[w][h];
        int[][] r = new int[w][h]; // Red
        int[][] g = new int[w][h]; // Green
        int[][] b = new int[w][h]; // Blue
        int pix;


        for(int i=0; i<size; i++){
            G[i]= (float) (Math.exp(-((i-a0)*(i-a0))/(2*b0*b0))/Math.sqrt(2*(Math.PI)*b0*b0));
            //Log.d("Unsharp,Gaussian Blur: ", "Calculating kernel"+G[i]);
        }

        for (int i=0;i<w;i++) {
            for (int j = 0; j < h; j++) {
                pix = input.getPixel(i,j);

                r[i][j] = Color.red(pix);
                g[i][j] = Color.green(pix);
                b[i][j] = Color.blue(pix);


            }
        }

        for (int i=0;i<w;i++) {
            for (int j = 0; j < h; j++) {
                for (int k=0;k<size;k++) {
                    int xval = i-a0+k;
                    if(!(xval<0 || xval>=w)){
                        qr[i][j] += G[k] * r[xval][j];
                        qg[i][j] += G[k] * g[xval][j];
                        qb[i][j] += G[k] * b[xval][j];
                    }
                }
            }
        }

        for (int i=0;i<w;i++) {
            for (int j = 0; j < h; j++) {
                for (int k=0;k<size;k++) {
                    int yval = j-a0+k;
                    if(!(yval<0 || yval>=h)){
                        Pr[i][j] += G[k] * qr[i][yval];
                        Pg[i][j] += G[k] * qg[i][yval];
                        Pb[i][j] += G[k] * qb[i][yval];
                        //Log.d("Gaussian Blur","One pixel modified");
                    }
                }
                Pr[i][j] = r[i][j] + (int)(b1*(r[i][j] -  Pr[i][j]));
                Pg[i][j] = g[i][j] + (int)(b1*(g[i][j] -  Pg[i][j]));
                Pb[i][j] = b[i][j] + (int)(b1*(b[i][j] -  Pb[i][j]));
                output.setPixel(i,j,Color.argb(255,Pr[i][j],Pg[i][j],Pb[i][j]));
            }
        }
        */

        getUnsharpMask(a0, b0, b1, input, output);
        Log.d("Unsharp mask","Done processing...!!!");

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

    public native static void getUnsharpMask(int a0, float b0, float b1, Bitmap input,Bitmap output);
}
