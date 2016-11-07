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

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //Toast.makeText(CameraActivity.this, "Service Connected!", Toast.LENGTH_LONG).show();
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

    public String[] getTransformsArray(){
        String[] transforms = {"Gaussian Blur", "Neon edges", "Color Filter", "Sobel Edge Filter", "Motion Blur"};
        return transforms;
    }

    public TransformTest[] getTestsArray(){
        TransformTest[] transforms = new TransformTest[5];
        transforms[0]=new TransformTest(0, new int[]{1,2,3}, new float[]{0.1f, 0.2f, 0.3f});
        transforms[1]=new TransformTest(1, new int[]{11,22,33}, new float[]{0.3f, 0.2f, 0.3f});
        transforms[2]=new TransformTest(2, new int[]{51,42,33}, new float[]{0.5f, 0.6f, 0.3f});
        transforms[3]=new TransformTest(3, new int[]{51,42,33}, new float[]{0.5f, 0.6f, 0.3f});
        transforms[4]=new TransformTest(4, new int[]{51,42,33}, new float[]{0.5f, 0.6f, 0.3f});
        return transforms;
    }

    public void registerHandler(TransformHandler artlistener){
        this.artlistener=artlistener;
        //listener = new MyTransformHandler(artlistener);
    }

    public boolean requestTransform(Bitmap img, int index, int[] intArgs, float[] floatArgs){
        MemoryFile memoryFile = null;
        try {
            queue.add(requestNo);
            int size = 0;
            //listener = new MyTransformHandler((MyTransformHandler)artlistener, index, intArgs, floatArgs, requestNo++);
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.JPEG, 100, b);
            Log.d("fd", "size:"+b.size());
            size = b.size();
            memoryFile = new MemoryFile("MyMemFile", size);
            ParcelFileDescriptor fd = MemoryFileUtil.getParcelFileDescriptor(memoryFile);
            OutputStream memOutputStream = memoryFile.getOutputStream();

            memOutputStream.write(b.toByteArray());
            Bundle data = new Bundle();
            data.putParcelable("PFD", fd);

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

    class ArtLibHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Log.d("clientSide", "what="+what);
            Bundle data = new Bundle();
            data = msg.getData();
            ParcelFileDescriptor fd = (ParcelFileDescriptor)data.get("ServicePFD");

            int size = msg.arg1;
            Log.d("fd", "size:"+size);
            ParcelFileDescriptor.AutoCloseInputStream isr = new ParcelFileDescriptor.AutoCloseInputStream(fd);
            byte[] b = new byte[size];
            try {
                isr.read(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final Bitmap bitmap = Bitmap.createBitmap(BitmapFactory.decodeByteArray(b, 0, b.length, options));

            final int requestNo = msg.arg2;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    int rqNo = (int)queue.peek();
                    Log.d("fd", "rqNo:"+rqNo);
                    Log.d("fd", "requestNo:"+requestNo);
                    while(true){
                        if (rqNo == requestNo) {
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
            //artlistener.onTransformProcessed(bitmap);
        }
    }

}
