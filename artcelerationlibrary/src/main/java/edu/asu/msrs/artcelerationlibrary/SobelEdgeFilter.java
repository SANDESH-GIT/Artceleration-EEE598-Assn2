/**
 * Thread which handles all requests for SobelEdgeFilter transform.
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


public class SobelEdgeFilter implements Runnable {
    private Bitmap input;
    private Messenger messenger;
    private int requestNo;
    private MemoryFile memoryFile;


    SobelEdgeFilter(Messenger messenger, Bitmap input, int requestNo, MemoryFile memoryFile) {
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
        Log.d("fd", "SobelEdge Filter!");

        int a0=0; // Grx
        //a0 = 1; // Gry
        a0 = 2; // Overall gradient

        //vertical edge filter
        int[][] sx=new int[][]{{-1,0,1}, {-2,0,2}, {-1,0,1}} ;

        // horizontal edge filter
        int[][] sy=new int[][]{{-1,-2,-1}, {0,0,0}, {1,2,1}} ;

        // Image size, w-> width & h->height
        int w = input.getWidth();
        int h = input.getHeight();

        // Creating bitmap to be returned as a modified (mutable output bitmap)
        Bitmap output = Bitmap.createBitmap(w,h,input.getConfig());
        Bitmap grayScale = Bitmap.createBitmap(w,h,input.getConfig());

        // Image represented by 4-bytes (4 channels as A,R, G, B)
        int r, g, b;
        int pix;

        int[][] gg = new int[w][h]; // Green

        for(int i=0; i< w; i++){
            for(int j=0; j<h; j++){
                pix=input.getPixel(i,j); // getPixel returns an integer value of the color of pixel

                // Filtering for every channel a,r,g,b
                r=(int)(Color.red(pix)* 0.2989);
                g=(int)(Color.green(pix)* 0.5870);
                b=(int)(Color.blue(pix)* 0.1140);

                grayScale.setPixel(i,j,Color.argb(255,r,g,b));
            }
        }

        for (int i=0;i<w;i++) {
            for (int j = 0; j < h; j++) {
                pix = grayScale.getPixel(i,j);
                gg[i][j] = Color.green(pix);
            }
        }


        if (a0==0){
            int[][]Grx=new int[w][h];
            for (int i=1;i<w-1;i++) {
                for (int j = 1; j < h-1; j++) {

                    Grx[i][j] = sx[0][0]*gg[i-1][j-1] + sx[1][0]*gg[i][j-1]+ sx[2][0]*gg[i+1][j-1]
                            + sx[0][1]*gg[i-1][j]+ sx[1][1]*gg[i][j]+ sx[2][1]*gg[i+1][j]
                            + sx[0][2]*gg[i-1][j+1] + sx[1][2]*gg[i][j+1]+ sx[2][2]*gg[i+1][j+1];

                    output.setPixel(i, j , Color.argb(255, Grx[i][j], Grx[i][j], Grx[i][j]));
                }
            }
        }else if(a0==1){
            int[][]Gry=new int[w][h];
            for (int i=1;i<w-1;i++) {
                for (int j = 1; j < h-1; j++) {

                    Gry[i][j] =sy[0][0]*gg[i-1][j-1] + sy[1][0]*gg[i][j-1]+ sy[2][0]*gg[i+1][j-1]
                            + sy[0][1]*gg[i-1][j]+ sy[1][1]*gg[i][j]+ sy[2][1]*gg[i+1][j]
                            + sy[0][2]*gg[i-1][j+1] + sy[1][2]*gg[i][j+1]+ sy[2][2]*gg[i+1][j+1];

                    output.setPixel(i, j , Color.argb(255, Gry[i][j], Gry[i][j], Gry[i][j]));
                }
            }
        }else if (a0 == 2){
            int[][]Grx=new int[w][h];
            int[][]Gry=new int[w][h];
            for (int i=1;i<w-1;i++) {
                for (int j = 1; j < h-1; j++) {

                    Grx[i][j] = sx[0][0]*gg[i-1][j-1] + sx[1][0]*gg[i][j-1]+ sx[2][0]*gg[i+1][j-1]
                            + sx[0][1]*gg[i-1][j]+ sx[1][1]*gg[i][j]+ sx[2][1]*gg[i+1][j]
                            + sx[0][2]*gg[i-1][j+1] + sx[1][2]*gg[i][j+1]+ sx[2][2]*gg[i+1][j+1];

                    Gry[i][j] =sy[0][0]*gg[i-1][j-1] + sy[1][0]*gg[i][j-1]+ sy[2][0]*gg[i+1][j-1]
                            + sy[0][1]*gg[i-1][j]+ sy[1][1]*gg[i][j]+ sy[2][1]*gg[i+1][j]
                            + sy[0][2]*gg[i-1][j+1] + sy[1][2]*gg[i][j+1]+ sy[2][2]*gg[i+1][j+1];

                    int Gr = (int) Math.sqrt(Grx[i][j]*Grx[i][j] + Gry[i][j]*Gry[i][j]);

                    output.setPixel(i, j , Color.argb(255, Gr, Gr, Gr));
                }
            }
        }else{
            output=input; //TODO: Should return null as per assignment change
        }

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
