/**
 * This library will provide the transform features specified by the service.
 * The application developer can get the list of the transform with an abstraction.
 * Main purpose of the library is that it will maintain the thread-safe queue with unique request number for every request.
 * Library will act as a client and service as a server.
 *
 * This library receives transform request from an application for an image transform having Bitmap as input.
 * Once the transform is requested library will write this data into the Memory File using byte array and then File Descriptor is passed.
 * The file descriptor is passed to service for image processing so that service can access the data over shared memory.
 *
 * Library also consists of a handler to handle the meassages sent by the service once the processing is done.
 * Then only library can send the results back to the application if the request made is at the front of the queue to ensure FIFO.
 */
package edu.asu.msrs.artcelerationlibrary;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by rlikamwa on 10/2/2016.
 */

public class ArtLib {
    private TransformHandler artlistener;
    private Activity activity;
    private Messenger messenger;
    private boolean isBound = false;
    private static int requestNo = 0;

    // Thread safe FIFO queue
    private static ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();

    // Interacting with the service
    ServiceConnection serviceConnection = new ServiceConnection() {
        // Once the service connection is successful, communicating with the service using messenger
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messenger = new Messenger(iBinder);

            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    public ArtLib(Activity activity){
        this.activity = activity;
        Intent i = new Intent(activity, ArtTransformService.class);
        activity.bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // List of transform options
    public String[] getTransformsArray(){
        String[] transforms = {"Gaussian Blur", "Unsharp Mask", "Color Filter", "Sobel Edge Filter", "Motion Blur"};
        return transforms;
    }

    public TransformTest[] getTestsArray(){
        TransformTest[] transforms = new TransformTest[5];
        transforms[0]=new TransformTest(0, new int[]{5,2,3}, new float[]{2.0f, 0.2f, 0.3f});
        transforms[1]=new TransformTest(1, new int[]{12,22,33}, new float[]{2.0f, 0.1f, 0.3f});
        transforms[2]=new TransformTest(2, new int[]{20,30,40,50,150,100,200,220,20,30,40,50,150,100,200,220,20,30,40,50,150,100,200,220}, new float[]{});//0.5f, 0.6f, 0.3f});
        transforms[3]=new TransformTest(3, new int[]{2,42,33}, new float[]{0.5f, 0.6f, 0.3f});
        transforms[4]=new TransformTest(4, new int[]{1,8,33}, new float[]{0.5f, 0.6f, 0.3f});

        return transforms;
    }

    public void registerHandler(TransformHandler artlistener){
        this.artlistener=artlistener;

    }

    // requestTransform method is invoked by the application having input parameter as Bitmap input image to be processed.
    public boolean requestTransform(Bitmap img, int index, int[] intArgs, float[] floatArgs){
        MemoryFile memoryFile = null;
        if(intArgs==null) return false;
        int intlen = intArgs.length;
        int floatlen;
        try {
            switch (index){
                case 0:
                    if (floatArgs==null) return false;
                    floatlen=floatArgs.length;
                    if (intlen<1 || floatlen<1) return false;
                    if (intArgs[0]<1 || floatArgs[0]<=0) return false;
                    break;
                case 1:
                    if (floatArgs==null) return false;
                    floatlen=floatArgs.length;
                    if (intlen<1 || floatlen<2) return false;
                    if (intArgs[0]<1 || floatArgs[0]<=0 || floatArgs[1]<=0) return false;
                    break;
                case 2:
                    if (intlen<24) return false;
                    int i, temp=intArgs[0];
                    for(i=0; i<24; i++){
                        if(intArgs[i]<0 || intArgs[i]>255) return false;
                        if(i%8==0) temp=intArgs[i];
                        if(i%2==0 && i%8!=0){
                            if(temp>=intArgs[i]) return false;
                            temp=intArgs[i];
                        }
                    }
                    break;
                case 3:
                    if (intlen<1) return false;
                    if (intArgs[0]>2 || intArgs[0]<0) return false;
                    break;
                case 4:
                    if (intlen<2) return false;
                    if (intArgs[0]>1 || intArgs[0]<0 || intArgs[1]<1) return false;
                    break;
                default: return false;

            }
            queue.add(requestNo);
            int size = 0;

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.JPEG, 100, b);
            Log.d("fd", "size:"+b.size());
            size = b.size();
            memoryFile = new MemoryFile("MyMemFile", size);
            ParcelFileDescriptor fd = MemoryFileUtil.getParcelFileDescriptor(memoryFile);
            OutputStream memOutputStream = memoryFile.getOutputStream();

            // Writing byte array of input image into the MemoryFile
            memOutputStream.write(b.toByteArray());
            Bundle data = new Bundle();

            // Sending Parcelable file descriptor to the service in a bundle
            data.putParcelable("PFD", fd);
            data.putIntArray("intArgs", intArgs);
            data.putFloatArray("floatArgs", floatArgs);

            /**
             * message to be sent to the service with parameter as index of transform, size of bitmap for memoryfile creation on server
             * side and unique requestNo associated with every transform request.
             * */
            Message m = Message.obtain(null, index, size, requestNo++);
            m.replyTo = client;
            m.setData(data);
            try {
                messenger.send(m);
            }catch(RemoteException e){
                e.printStackTrace();
            }
            memoryFile.allowPurging(true);
            memoryFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    final Messenger client = new Messenger(new ArtLibHandler());

    // Handler for handling messages by multiple threads of transforms.
    class ArtLibHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Log.d("clientSide", "what="+what);
            Bundle data = new Bundle();
            data = msg.getData();

            // Get the file descriptor passed from threads
            ParcelFileDescriptor fd = (ParcelFileDescriptor)data.get("ClassPFD");

            int size = msg.arg1;
            Log.d("fd", "size:"+size);
            ParcelFileDescriptor.AutoCloseInputStream isr = new ParcelFileDescriptor.AutoCloseInputStream(fd);
            final byte[] b = new byte[size];
            try {
                isr.read(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;

            // read the unique request number.
            final int requestNo = msg.arg2;
            Log.d("fd", "outside requestNo:"+requestNo);

            // Multiple threads can check on their position at the head of the queue
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    Bitmap bitmap = Bitmap.createBitmap(BitmapFactory.decodeByteArray(b, 0, b.length, options));

                    int requestNumber = requestNo;
                    Log.d("fd", "requestNo:"+requestNumber);

                    // Busy waiting for every thread unitil the head of the queue matches with the head of the queue
                    // If matched the remove from the queue and call onTransformProcessed passing processed Bitmap image.
                    while(true){
                        int rqNo = (int)queue.peek();
                        if (rqNo == requestNumber) {
                            artlistener.onTransformProcessed(bitmap);
                            queue.poll();
                            break;
                        }
                        try {
                            Thread.sleep(1000, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.start();
        }
    }

}
