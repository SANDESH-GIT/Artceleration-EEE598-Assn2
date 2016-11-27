#include <edu_asu_msrs_artcelerationlibrary_MotionBlur.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "libimageprocessing"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)


static void process(AndroidBitmapInfo* info, void* pixels, int a0, int a1, int size){
	int i, j, k;
	uint32_t* line;
	void* px;
	int xval;

	static int r[info->width][info->height];
	static int g[info->width][info->height];
	static int b[info->width][info->height];

	int Pr[info->width][info->height];
    int Pg[info->width][info->height];
    int Pb[info->width][info->height];


    px = pixels;
    //uint8_t* line = (uint8_t*)pixels;
    int alpha;
    //line = (uint32_t*)pixels;

	for(j = 0; j < info->height; j++){
            line = (uint32_t*)pixels;
            //LOGD("Pixel value: %d!!\n", line[3]);
            //line1 = (uint32_t*)pixels;
            //line = (uint32_t*)((char*)pixels + j*info->stride);
			for(i =0; i < info->width; i++){
			   // pos = j*info->width+i;
			    //extract the RGB values from the pixel
			    //uint8_t value = *(line + i + j*info->stride);
				r[i][j] = (int)((line[i] & 0x00FF0000) >> 16);
				g[i][j] = (int)((line[i] & 0x0000FF00) >> 8);
				b[i][j] = (int)(line[i] & 0x00000FF );
				alpha = (int)((line[i] & 0xFF000000) >> 24);
				line[i] = (((r[i][j] << 16) & 0x00FF0000) |
                          ((g[i][j] << 8) & 0x0000FF00) |
                          (b[i][j] & 0x000000FF)|
                          ((alpha << 24) & 0xFF000000));

			}
			pixels = ((char*)pixels + info->stride);
			//line = (uint32_t*)((char*)line + info->stride);
	}


	for(j = 0; j < info->height; j++){

	            line = (uint32_t*)px;
	            //line1 = (uint32_t*)pixels;
	            //LOGD("line1: %u\n", line1[3]);
                //LOGD("I am here3!!\n");
    			for(i =0; i < info->width; i++){
    			    //pos = j*info->width+i;
                    Pr[i][j] =0;
                    Pg[i][j]=0;
                    Pb[i][j]=0;

                    for (k = 0; k < size; k++) {
                        xval = i - a1 + k;

                        if (!(xval < 0 || xval >= info->width)) {
                            //LOGD("I am here4!!\n");

                            //r[xval][j] = ((line[xval] & 0x00FF0000) >> 16);
                            //g[xval][j] = ((line[xval] & 0x0000FF00) >> 8);
                            //b[xval][j] = (line[xval] & 0x00000FF);

                            Pr[i][j] += r[xval][j];
                            Pg[i][j] += g[xval][j];
                            Pb[i][j] += b[xval][j];

                        }

                    }
                        Pr[i][j] /= size;
                        Pg[i][j] /= size;
                        Pb[i][j] /= size;

                        // set the new pixel back in
                        line[i] =
                        (((Pr[i][j] << 16) & 0x00FF0000) |
                        ((Pg[i][j] << 8) & 0x0000FF00) |
                        (Pb[i][j] & 0x000000FF) |
                        0xFF000000);

                    }

    			px = (char*)px + info->stride;

    	}
}



JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_MotionBlur_getMotionBlur
  (JNIEnv * env, jclass  jc, jint a0, jint a1, jobject bitmap, jint size)
  {

      AndroidBitmapInfo  info;
      int ret;
      void* pixels;

      LOGD("a0=%d, a1=%d, size =%d\n", a0, a1, size);
      if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
              LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
              return;
          }
      /*
      if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
          LOGE("Bitmap format is not RGBA_8888 !");
          return;
      }*/

      if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
          LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
      }

      process(&info,pixels, a0, a1, size);

      AndroidBitmap_unlockPixels(env, bitmap);
  }



