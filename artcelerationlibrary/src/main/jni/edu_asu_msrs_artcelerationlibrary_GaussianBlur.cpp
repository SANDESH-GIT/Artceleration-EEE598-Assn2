#include <edu_asu_msrs_artcelerationlibrary_GaussianBlur.h>
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

static void process(AndroidBitmapInfo* info, void* input, void* output,int a0, float b0){
        int i, j, k;
        uint32_t* line;
        uint32_t* line1;
        int size=2*a0+1;
    	void* px = input;
        void* pxo = output;
    	int xval;
        int yval;
        int w = info->width;
        int h = info->height;
        int r, g, b;

    	int Pr[info->width][info->height];
        int Pg[info->width][info->height];
        int Pb[info->width][info->height];

        float G[size];
        float** qr = new float*[info->width];
        float** qg = new float*[info->width];
        float** qb = new float*[info->width];

        for (i=0;i<info->width;i++){
            qr[i]=new float [info->height];
            qg[i]=new float [info->height];
            qb[i]=new float [info->height];
        }

        for(i=0; i<size; i++){
            G[i]= (float) (exp(-((i-a0)*(i-a0))/(2*b0*b0))/sqrt(2*(M_PI)*b0*b0));
            //Log.d("Gaussian Blur: ", "Calculating kernel"+G[i]);
        }

        for (j = 0; j < h; j++) {
            line1 = (uint32_t *)px;
            for (i=0;i<w;i++) {
                qr[i][j] = 0;
                qg[i][j] = 0;
                qb[i][j] = 0;
                for (k=0;k<size;k++) {
                    xval = i-a0+k;
                    if(!(xval<0 || xval>=w)){
                        r = ((line1[xval] & 0x00FF0000) >> 16);
                        g = ((line1[xval] & 0x0000FF00) >> 8);
                        b = (line1[xval] & 0x00000FF);
                        //float temp = G[k];
                        qr[i][j] += G[k] * r;
                        qg[i][j] += G[k] * g;
                        qb[i][j] += G[k] * b;
                    }
                }
            }
            px = (char *) px + info->stride;
        }


        for (j = 0; j < h; j++) {
            line = (uint32_t *)pxo;
            for (i=0;i<w;i++) {
                Pr[i][j] = 0;
                Pg[i][j] = 0;
                Pb[i][j] = 0;
                for (k=0;k<size;k++) {
                    yval = j-a0+k;
                    if(!(yval<0 || yval>=h)){
                        Pr[i][j] += G[k] * qr[i][yval];
                        Pg[i][j] += G[k] * qg[i][yval];
                        Pb[i][j] += G[k] * qb[i][yval];
                    }
                }
                // set the new pixel back in
                line[i] =
                (((Pr[i][j] << 16) & 0x00FF0000) |
                ((Pg[i][j] << 8) & 0x0000FF00) |
                (Pb[i][j] & 0x000000FF) |
                (0xFF000000));
            }
            pxo = (char *) pxo + info->stride;
        }

        for (i=0;i<info->width;i++){
            delete [] qr[i];
            delete [] qg[i];
            delete [] qb[i];
        }
        delete [] qr;
        delete [] qg;
        delete [] qb;
}


JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_GaussianBlur_getGaussianBlur
  (JNIEnv * env, jclass jc, jint a0, jfloat b0, jobject input, jobject output){
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

              process(&info_input, pixels_input, pixels_output, a0, b0);

              AndroidBitmap_unlockPixels(env, input);
              AndroidBitmap_unlockPixels(env, output);
  }


