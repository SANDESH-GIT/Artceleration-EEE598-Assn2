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

using namespace std;

static void process(AndroidBitmapInfo* info, void* input, void* output,int a0, int a1){
	int i, j, k;
	uint32_t* line;
    uint32_t* line1;
    int size=2*a1+1;
	void* px = input;
    void* pxo = output;
	int xval;
    int yval;

    int r, g, b;

	int Pr[info->width][info->height];
    int Pg[info->width][info->height];
    int Pb[info->width][info->height];

    if (a0==0) {
        for (j = 0; j < info->height; j++) {
            line = (uint32_t *) pxo;
            line1 = (uint32_t *)px;

            for (i = 0; i < info->width; i++) {
                Pr[i][j] = 0;
                Pg[i][j] = 0;
                Pb[i][j] = 0;

                for (k = 0; k < size; k++) {
                    xval = i - a1 + k;

                    if (!(xval < 0 || xval >= info->width)) {
                        r = ((line1[xval] & 0x00FF0000) >> 16);
                        g = ((line1[xval] & 0x0000FF00) >> 8);
                        b = (line1[xval] & 0x00000FF);

                        Pr[i][j] += r;
                        Pg[i][j] += g;
                        Pb[i][j] += b;
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
                         (0xFF000000));
            }
            px = (char *) px + info->stride;
            pxo = (char *) pxo + info->stride;
        }
    }else if(a0==1){
        for (j = 0; j < info->height; j++) {
            line = (uint32_t *)(pxo)+(uint32_t)(info->width*j);

            for (i = 0; i < info->width; i++) {
                Pr[i][j] = 0;
                Pg[i][j] = 0;
                Pb[i][j] = 0;

                for (k = 0; k < size; k++) {
                    yval = j - a1 + k;

                    if (!(yval < 0 || yval >= info->height)) {
                        line1 = (uint32_t *)(px)+(uint32_t)(info->width*yval);

                        r = ((line1[i] & 0x00FF0000) >> 16);
                        g = ((line1[i] & 0x0000FF00) >> 8);
                        b = (line1[i] & 0x00000FF);

                        Pr[i][j] += r;
                        Pg[i][j] += g;
                        Pb[i][j] += b;
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
                         (0xFF000000));
            }
        }
    }
}

JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_MotionBlur_getMotionBlur
  (JNIEnv * env, jclass  jc, jint a0, jint a1, jobject input, jobject output)
  {

      AndroidBitmapInfo  info_input;
      int ret;
      void* pixels_input;
      void* pixels_output;


      // LOGD("a0=%d, a1=%d, size =%d\n", a0, a1, size);
      if ((ret = AndroidBitmap_getInfo(env, input, &info_input)) < 0) {
              LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
              return;
          }

      if ((ret = AndroidBitmap_lockPixels(env, input, &pixels_input)) < 0) {
          LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
      }

        if ((ret = AndroidBitmap_lockPixels(env, output, &pixels_output)) < 0) {
            LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        }

      process(&info_input, pixels_input, pixels_output, a0, a1);

      AndroidBitmap_unlockPixels(env, input);
      AndroidBitmap_unlockPixels(env, output);
  }



