/**
 * Bounded Service to which the library binds.
 * Library uses this service for each transform request made by the application.
 * Acts as a multithreaded server by creating a new thread for each request.
 */

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

    // Handles messages sent by the library
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

            // To read the input bitmap image written to the Memory File in the library.
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

            // Creates a new Memory File for every request and is passed to Thread which write to it after processing is done.
            MemoryFile serviceMemFile = null;
            try {
                serviceMemFile = new MemoryFile("MyMemFile", bitmap.getByteCount());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Creates a new thread depending on the what parameter of the message received by library which specifies the Transform type
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

    // Messenger object pointing to ArtTransformHandler
    final Messenger m = new Messenger(new ArtTransformHandler());

    // Called when library invokes the bindService method which returns a binder.
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return m.getBinder();
    }
}
