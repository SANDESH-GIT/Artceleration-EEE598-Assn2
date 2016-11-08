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
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ArtTransformService extends Service {

    public ArtTransformService() {
    }

    class ArtTransformHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Messenger m = msg.replyTo;
            Bundle data = new Bundle();
            data = msg.getData();
            ParcelFileDescriptor fd = (ParcelFileDescriptor)data.get("PFD");

            int type = msg.what;
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

            MemoryFile serviceMemFile = null;
            try {
                serviceMemFile = new MemoryFile("MyMemFile", bitmap.getByteCount());
            } catch (IOException e) {
                e.printStackTrace();
            }

            switch (type){
                case 0:
                    Thread t = new Thread(new GaussianBlur(m, bitmap,requestNo, serviceMemFile));
                    t.start();
                    break;
                case 1:
                    Thread t1 = new Thread(new NeonEdges(m, bitmap,requestNo, serviceMemFile));
                    t1.start();
                    break;
                case 2:
                    Thread t2 = new Thread(new ColorFilter(m, bitmap,requestNo, serviceMemFile));
                    t2.start();
                    break;
                case 3:
                    Thread t3 = new Thread(new SobelEdgeFilter(m, bitmap,requestNo, serviceMemFile));
                    t3.start();
                    break;
                case 4:
                    Thread t4 = new Thread(new MotionBlur(m, bitmap,requestNo, serviceMemFile));
                    t4.start();
                    break;
                default:
                    break;
            }
        }
    }

    final Messenger m = new Messenger(new ArtTransformHandler());

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return m.getBinder();
    }
}
