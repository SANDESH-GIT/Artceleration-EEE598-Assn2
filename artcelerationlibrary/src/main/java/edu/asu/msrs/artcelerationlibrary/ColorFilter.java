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

        // Red channel slopes and constant
        float Rm1 = 0;
        float Rc2 = 0;
        float Rm2 = 0;
        float Rc3 = 0;
        float Rm3 = 0;
        float Rc4 = 0;
        float Rm4 = 0;
        float Rc5 = 0;
        float Rm5 = 0;

        // Green channel slopes and constant
        float Gm1 = 0;
        float Gc2 = 0;
        float Gm2 = 0;
        float Gc3 = 0;
        float Gm3 = 0;
        float Gc4 = 0;
        float Gm4 = 0;
        float Gc5 = 0;
        float Gm5 = 0;

        // Blue channel slopes and constant
        float Bm1 = 0;
        float Bc2 = 0;
        float Bm2 = 0;
        float Bc3 = 0;
        float Bm3 = 0;
        float Bc4 = 0;
        float Bm4 = 0;
        float Bc5 = 0;
        float Bm5 = 0;

        int[] arr={20,30,40,50,150,100,200,220,20,30,40,50,150,100,200,220,20,30,40,50,150,100,200,220};

        // TODO: conditions on p1 as arrangement in increasing order otherwise return null
        /*
        // Slopes for red; when p0!=0 and p6!=255
        if (arr[0]!=0 && arr[6]!=255) {
            Rm1 = ((float) arr[1] / arr[0]);
            Rm2 = ((float) arr[3] - arr[1]) / (arr[2] - arr[0]);
            Rc2 = arr[1] - (Rm2 * arr[0]);
            Rm3 = ((float) arr[5] - arr[3]) / (arr[4] - arr[2]);
            Rc3 = arr[3] - (Rm3 * arr[2]);
            Rm4 = ((float) arr[7] - arr[5]) / (arr[6] - arr[4]);
            Rc4 = arr[5] - (Rm4 * arr[4]);
            Rm5 = (((float) 255 - arr[7]) / (255 - arr[6]));
            Rc5 = arr[7] - (Rm5 * arr[6]);
        }else if (arr[0]==0 && arr[6]!=255){
            Rm2 = ((float) arr[3] - arr[1]) / (arr[2] - arr[0]);
            Rc2 = arr[1];
            Rm3 = ((float) arr[5] - arr[3]) / (arr[4] - arr[2]);
            Rc3 = arr[3] - (Rm3 * arr[2]);
            Rm4 = ((float) arr[7] - arr[5]) / (arr[6] - arr[4]);
            Rc4 = arr[5] - (Rm4 * arr[4]);
            Rm5 = (((float) 255 - arr[7]) / (255 - arr[6]));
            Rc5 = arr[7] - (Rm5 * arr[6]);
        }else if (arr[0]==0 && arr[6]==255){
            Rm2 = ((float) arr[3] - arr[1]) / (arr[2] - arr[0]);
            Rc2 = arr[1];
            Rm3 = ((float) arr[5] - arr[3]) / (arr[4] - arr[2]);
            Rc3 = arr[3] - (Rm3 * arr[2]);
            Rm4 = ((float) arr[7] - arr[5]) / (arr[6] - arr[4]);
            Rc4 = arr[5] - (Rm4 * arr[4]);
        }else if (arr[0]!=0 && arr[6]==255){
            Rm1 = ((float) arr[1] / arr[0]);
            Rm2 = ((float) arr[3] - arr[1]) / (arr[2] - arr[0]);
            Rc2 = arr[1] - (Rm2 * arr[0]);
            Rm3 = ((float) arr[5] - arr[3]) / (arr[4] - arr[2]);
            Rc3 = arr[3] - (Rm3 * arr[2]);
            Rm4 = ((float) arr[7] - arr[5]) / (arr[6] - arr[4]);
            Rc4 = arr[5] - (Rm4 * arr[4]);
        }

        // Slopes for green
        if (arr[8]!=0 && arr[14]!=255){
            Gm1 = ((float) arr[9] / arr[8]);
            Gm2 = ((float) arr[11] - arr[9]) / (arr[10] - arr[8]);
            Gc2 = arr[9] - (Gm2 * arr[8]);
            Gm3 = ((float) arr[13] - arr[11]) / (arr[12] - arr[10]);
            Gc3 = arr[11] - (Gm3 * arr[10]);
            Gm4 = ((float) arr[15] - arr[13]) / (arr[14] - arr[12]);
            Gc4 = arr[13] - (Gm4 * arr[12]);
            Gm5 = (((float) 255 - arr[15]) / (255 - arr[14]));
            Gc5 = arr[15] - (Gm5 * arr[14]);
        }else if (arr[8]==0 && arr[14]!=255){
            Gm2 = ((float) arr[11] - arr[9]) / (arr[10] - arr[8]);
            Gc2 = arr[9];
            Gm3 = ((float) arr[13] - arr[11]) / (arr[12] - arr[10]);
            Gc3 = arr[11] - (Gm3 * arr[10]);
            Gm4 = ((float) arr[15] - arr[13]) / (arr[14] - arr[12]);
            Gc4 = arr[13] - (Gm4 * arr[12]);
            Gm5 = (((float) 255 - arr[15]) / (255 - arr[14]));
            Gc5 = arr[15] - (Gm5 * arr[14]);
        }else if (arr[8]==0 && arr[14]==255){
            Gm2 = ((float) arr[11] - arr[9]) / (arr[10] - arr[8]);
            Gc2 = arr[9];
            Gm3 = ((float) arr[13] - arr[11]) / (arr[12] - arr[10]);
            Gc3 = arr[11] - (Gm3 * arr[10]);
            Gm4 = ((float) arr[15] - arr[13]) / (arr[14] - arr[12]);
            Gc4 = arr[13] - (Gm4 * arr[12]);
        }else if (arr[8]!=0 && arr[14]==255){
            Gm1 = ((float) arr[9] / arr[8]);
            Gm2 = ((float) arr[11] - arr[9]) / (arr[10] - arr[8]);
            Gc2 = arr[9] - (Gm2 * arr[8]);
            Gm3 = ((float) arr[13] - arr[11]) / (arr[12] - arr[10]);
            Gc3 = arr[11] - (Gm3 * arr[10]);
            Gm4 = ((float) arr[15] - arr[13]) / (arr[14] - arr[12]);
            Gc4 = arr[13] - (Gm4 * arr[12]);
        }

        // Slopes for blue
        if (arr[16]!=0 && arr[22]!=255){
            Bm1 = ((float) arr[17] / arr[16]);
            Bm2 = ((float) arr[19] - arr[17]) / (arr[18] - arr[16]);
            Bc2 = arr[17] - (Bm2 * arr[16]);
            Bm3 = ((float) arr[21] - arr[19]) / (arr[20] - arr[18]);
            Bc3 = arr[19] - (Bm3 * arr[18]);
            Bm4 = ((float) arr[23] - arr[21]) / (arr[22] - arr[20]);
            Bc4 = arr[21] - (Bm4 * arr[20]);
            Bm5 = (((float) 255 - arr[23]) / (255 - arr[22]));
            Bc5 = arr[23] - ((((float) 255 - arr[23]) / (255 - arr[22])) * arr[22]);
        }else if (arr[16]==0 && arr[22]!=255){
            Bm2 = ((float) arr[19] - arr[17]) / (arr[18] - arr[16]);
            Bc2 = arr[17];
            Bm3 = ((float) arr[21] - arr[19]) / (arr[20] - arr[18]);
            Bc3 = arr[19] - (Bm3 * arr[18]);
            Bm4 = ((float) arr[23] - arr[21]) / (arr[22] - arr[20]);
            Bc4 = arr[21] - (Bm4 * arr[20]);
            Bm5 = (((float) 255 - arr[23]) / (255 - arr[22]));
            Bc5 = arr[23] - ((((float) 255 - arr[23]) / (255 - arr[22])) * arr[22]);
        }else if (arr[16]==0 && arr[22]==255){
            Bm2 = ((float) arr[19] - arr[17]) / (arr[18] - arr[16]);
            Bc2 = arr[17];
            Bm3 = ((float) arr[21] - arr[19]) / (arr[20] - arr[18]);
            Bc3 = arr[19] - (Bm3 * arr[18]);
            Bm4 = ((float) arr[23] - arr[21]) / (arr[22] - arr[20]);
            Bc4 = arr[21] - (Bm4 * arr[20]);
        }else if (arr[16]!=0 && arr[22]==255){
            Bm1 = ((float) arr[17] / arr[16]);
            Bm2 = ((float) arr[19] - arr[17]) / (arr[18] - arr[16]);
            Bc2 = arr[17] - (Bm2 * arr[16]);
            Bm3 = ((float) arr[21] - arr[19]) / (arr[20] - arr[18]);
            Bc3 = arr[19] - (Bm3 * arr[18]);
            Bm4 = ((float) arr[23] - arr[21]) / (arr[22] - arr[20]);
            Bc4 = arr[21] - (Bm4 * arr[20]);
        }

        // Iterate over all pixels
        for(int i=0; i< w; i++){
            for(int j=0; j<h; j++){
                pix=input.getPixel(i,j); // getPixel returns an integer value of the color of pixel

                r=Color.red(pix);
                g=Color.green(pix);
                b=Color.blue(pix);

                // Mapping for red channel
                if (arr[0]!=0 && arr[6]!=255){
                    if (r<=arr[0]){
                        r = (int) (Rm1*r);
                    }else if(r>arr[0] && r<=arr[2]) {
                        r= (int) ((Rm2*r)+Rc2);
                    }else if(r>arr[2] && r<=arr[4]){
                        r=(int) ((Rm3*r)+Rc3);
                    }else if (r>arr[4] && r<=arr[6]){
                        r= (int) ((Rm4*r)+Rc4);
                    }else{
                        r= (int) ((Rm5*r)+Rc5);
                    }
                }else if (arr[0]==0 && arr[6]!=255){
                    if(r<=arr[2]) {
                        r= (int) ((Rm2*r)+Rc2);
                    }else if(r>arr[2] && r<=arr[4]){
                        r=(int) ((Rm3*r)+Rc3);
                    }else if (r>arr[4] && r<=arr[6]){
                        r= (int) ((Rm4*r)+Rc4);
                    }else{
                        r= (int) ((Rm5*r)+Rc5);
                    }
                }else if (arr[0]==0 && arr[6]==255){
                    if(r<=arr[2]) {
                        r= (int) ((Rm2*r)+Rc2);
                    }else if(r>arr[2] && r<=arr[4]){
                        r=(int) ((Rm3*r)+Rc3);
                    }else{
                        r= (int) ((Rm4*r)+Rc4);
                    }
                }else if (arr[0]!=0 && arr[6]==255){
                    if (r<=arr[0]){
                        r = (int) (Rm1*r);
                    }else if(r>arr[0] && r<=arr[2]) {
                        r= (int) ((Rm2*r)+Rc2);
                    }else if(r>arr[2] && r<=arr[4]){
                        r=(int) ((Rm3*r)+Rc3);
                    }else{
                        r= (int) ((Rm4*r)+Rc4);
                    }
                }

                // Mapping for green channel
                if (arr[8]!=0 && arr[14]!=255){
                    if (g<=arr[8]){
                        g = (int) (Gm1*g);
                    }else if(g>=arr[8] && g<=arr[10]) {
                        g= (int) ((Gm2*g)+Gc2);
                    }else if(g>=arr[10] && g<=arr[12]){
                        g=(int) ((Gm3*g)+Gc3);
                    }else if (g>=arr[12] && g<=arr[14]){
                        g= (int) ((Gm4*g)+Gc4);
                    }else{
                        g= (int) ((Gm5*g)+Gc5);
                    }
                }else if (arr[8]==0 && arr[14]!=255){
                    if(g<=arr[10]) {
                        g= (int) ((Gm2*g)+Gc2);
                    }else if(g>=arr[10] && g<=arr[12]){
                        g=(int) ((Gm3*g)+Gc3);
                    }else if (g>=arr[12] && g<=arr[14]){
                        g= (int) ((Gm4*g)+Gc4);
                    }else{
                        g= (int) ((Gm5*g)+Gc5);
                    }
                }else if (arr[8]==0 && arr[14]==255){
                    if(g<=arr[10]) {
                        g= (int) ((Gm2*g)+Gc2);
                    }else if(g>=arr[10] && g<=arr[12]){
                        g=(int) ((Gm3*g)+Gc3);
                    }else{
                        g= (int) ((Gm4*g)+Gc4);
                    }
                }else if (arr[8]!=0 && arr[14]==255){
                    if (g<=arr[8]){
                        g = (int) (Gm1*g);
                    }else if(g>=arr[8] && g<=arr[10]) {
                        g= (int) ((Gm2*g)+Gc2);
                    }else if(g>=arr[10] && g<=arr[12]){
                        g=(int) ((Gm3*g)+Gc3);
                    }else{
                        g= (int) ((Gm4*g)+Gc4);
                    }
                }

                // Mapping for blue channel
                if (arr[16]!=0 && arr[22]!=255){
                    if (b<=arr[16]){
                        b = (int) (Bm1*b);
                    }else if(b>=arr[16] && b<=arr[18]) {
                        b= (int) ((Bm2*b)+Bc2);
                    }else if(b>=arr[18] && b<=arr[20]){
                        b=(int) ((Bm3*b)+Bc3);
                    }else if (b>=arr[20] && b<=arr[22]){
                        b= (int) ((Bm4*b)+Bc4);
                    }else{
                        b= (int) ((Bm5*b)+Bc5);
                    }
                }else if (arr[16]==0 && arr[22]!=255){
                    if(b<=arr[18]) {
                        b= (int) ((Bm2*b)+Bc2);
                    }else if(b>=arr[18] && b<=arr[20]){
                        b=(int) ((Bm3*b)+Bc3);
                    }else if (b>=arr[20] && b<=arr[22]){
                        b= (int) ((Bm4*b)+Bc4);
                    }else{
                        b= (int) ((Bm5*b)+Bc5);
                    }
                }else if (arr[16]==0 && arr[22]==255){
                    if(b<=arr[18]) {
                        b= (int) ((Bm2*b)+Bc2);
                    }else if(b>=arr[18] && b<=arr[20]){
                        b=(int) ((Bm3*b)+Bc3);
                    }else{
                        b= (int) ((Bm4*b)+Bc4);
                    }
                }else if (arr[16]!=0 && arr[22]==255){
                    if (b<=arr[16]){
                        b = (int) (Bm1*b);
                    }else if(b>=arr[16] && b<=arr[18]) {
                        b= (int) ((Bm2*b)+Bc2);
                    }else if(b>=arr[18] && b<=arr[20]){
                        b=(int) ((Bm3*b)+Bc3);
                    }else{
                        b= (int) ((Bm4*b)+Bc4);
                    }
                }

                output.setPixel(i,j,Color.argb(255,r,g,b));
            }
        }*/

        getColorFilter(arr, input);
        //TODO: Should return null if arguments passed are not proper

        Log.d("Color filter","Done processing...!!!");

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
    public native static void getColorFilter(int a[], Bitmap img);
}
