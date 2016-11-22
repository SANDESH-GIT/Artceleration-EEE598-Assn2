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
        int r, g, b;
        int pix;
        int[] arr= {20,40,150,200,30,50,100,220,20,40,150,200,30,50,100,220,20,40,150,200,30,50,100,220};

        // TODO: conditions on p1 as arrangement in increasing order otherwise return null

        // Slopes for red; when p0!=0 and p3!=255
        double m1= ((double)arr[4]/arr[0]);
        double c2= arr[4]-((((double)arr[5]-arr[4])/(arr[1]-arr[0]))*arr[0]);
        double m2 = ((double)arr[5]-arr[4])/(arr[1]-arr[0]);
        double c3 = arr[5]-((((double)arr[6]-arr[5])/(arr[2]-arr[1]))*arr[1]);
        double m3= ((double)arr[6]-arr[5])/(arr[2]-arr[1]);
        double c4= arr[6]-((((double)arr[7]-arr[6])/(arr[3]-arr[2]))*arr[2]);
        double m4= ((double)arr[7]-arr[6])/(arr[3]-arr[2]);
        double c5= arr[7]-((((double)255-arr[7])/(255-arr[3]))*arr[3]);
        double m5= (((double)255-arr[7])/(255-arr[3]));


        // Iterate over all pixels
        for(int i=0; i< w; i++){
            for(int j=0; j<h; j++){
                pix=input.getPixel(i,j); // getPixel returns an integer value of the color of pixel

                r=Color.red(pix);
                g=Color.green(pix);
                b=Color.blue(pix);

                // Mapping for red channel
                if (arr[0]!=0 && arr[3]!=255){
                    if (r<=arr[0]){
                        r = (int) (m1*r);
                    }else if(r>=arr[0] && r<=arr[1]) {
                        r= (int) ((m2*r)+c2);
                    }else if(r>=arr[1] && r<=arr[2]){
                        r=(int) ((m3*r)+c3);
                    }else if (r>=arr[2] && r<=arr[3]){
                        r= (int) ((m4*r)+c4);
                    }else{
                        r= (int) ((m5*r)+c5);
                    }
                }else if (arr[0]==0 && arr[3]!=255){

                }else if (arr[0]==0 && arr[3]==255){

                }else if (arr[0]!=0 && arr[3]==255){

                }

                // Mapping for green channel
                if (arr[8]!=0 && arr[11]!=255){
                    if (g<=arr[0]){
                        g = (int) (m1*g);
                    }else if(g>=arr[8] && g<=arr[9]) {
                        g= (int) ((m2*g)+c2);
                    }else if(g>=arr[9] && g<=arr[10]){
                        g=(int) ((m3*g)+c3);
                    }else if (g>=arr[10] && g<=arr[11]){
                        g= (int) ((m4*g)+c4);
                    }else{
                        g= (int) ((m5*g)+c5);
                    }
                }else if (arr[8]==0 && arr[11]!=255){

                }else if (arr[8]==0 && arr[11]==255){

                }else if (arr[8]!=0 && arr[11]==255){

                }

                // Mapping for blue channel
                if (arr[16]!=0 && arr[19]!=255){
                    if (b<=arr[16]){
                        b = (int) (m1*b);
                    }else if(b>=arr[16] && b<=arr[17]) {
                        b= (int) ((m2*b)+c2);
                    }else if(b>=arr[17] && b<=arr[18]){
                        b=(int) ((m3*b)+c3);
                    }else if (b>=arr[18] && b<=arr[19]){
                        b= (int) ((m4*b)+c4);
                    }else{
                        b= (int) ((m5*b)+c5);
                    }
                }else if (arr[16]==0 && arr[19]!=255){

                }else if (arr[16]==0 && arr[19]==255){

                }else if (arr[16]!=0 && arr[19]==255){

                }

                output.setPixel(i,j,Color.argb(255,r,g,b));
            }
        }

        //TODO: Should return null if arguments passed are not proper

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
