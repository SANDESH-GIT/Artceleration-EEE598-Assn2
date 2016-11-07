package edu.asu.msrs.artcelerationlibrary;

import android.app.Service;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArtTransformService extends Service {
    private TransformHandler transformHandler;



    public ArtTransformService() {
    }

    class ArtTransformHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            Messenger m = msg.replyTo;
            Bundle data = new Bundle();
            data = msg.getData();
            ParcelFileDescriptor fd = (ParcelFileDescriptor)data.get("PFD");

            int type = msg.what;
            //transformHandler =  myTransformHandler.getTransformHandler();
            int size = msg.arg1;
            int requestNo = msg.arg2;
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
            Log.d("fd", "bytes:"+b.length);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Bitmap bitmap = Bitmap.createBitmap(BitmapFactory.decodeByteArray(b, 0, b.length, options));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] oparray = outputStream.toByteArray();
            ParcelFileDescriptor pfd = null;
            try {
                MemoryFile serviceMemFile = new MemoryFile("MyMemFile", bitmap.getByteCount());
                pfd = MemoryFileUtil.getParcelFileDescriptor(serviceMemFile);
                serviceMemFile.getOutputStream().write(oparray);

                Bundle bundle = new Bundle();
                Message message = Message.obtain(null, 10, oparray.length, requestNo);
                bundle.putParcelable("ServicePFD", pfd);
                message.setData(bundle);
                m.send(message);
                serviceMemFile.allowPurging(true);
                serviceMemFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (type){
                case 0:
                    Thread t = new Thread(new GaussianBlur(m, bitmap,requestNo, pfd));
                    t.start();
                    break;
                case 1:
                    Thread t1 = new Thread(new NeonEdges(m, bitmap,requestNo, pfd));
                    t1.start();
                    break;
                case 2:
                    Thread t2 = new Thread(new ColorFilter(m, bitmap,requestNo, pfd));
                    t2.start();
                    break;
                case 3:
                    Thread t3 = new Thread(new SobelEdgeFilter(m, bitmap,requestNo, pfd));
                    t3.start();
                    break;
                case 4:
                    Thread t4 = new Thread(new MotionBlur(m, bitmap,requestNo, pfd));
                    t4.start();
                    break;
                default:
                    break;
            }
            Log.d("fd", "size:"+oparray.length);
        }
    }

    final Messenger m = new Messenger(new ArtTransformHandler());

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return m.getBinder();
    }
}
