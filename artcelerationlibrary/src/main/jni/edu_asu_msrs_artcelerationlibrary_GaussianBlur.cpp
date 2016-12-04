/**
 * This is native(C++) code for Gaussian Blur image transform.
 * Each output pixel should be a Gaussian‐weighted combination of nearby input pixel values.
 * This transform requires radius and standard deviation for processing.
 */
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

/**
 * This function performs the Gaussian Blur operation.
 * @param info
 */


/**
 * This function performs the Gaussian Blur operation.
 * @param info : info is a pointer to AndroidBitmapInfo which provides height and width of the bitmap.
 * @param input : Pointer to input Bitmap.
 * @param output : Pointer to resultant output transformed bitmap.
 * @param a0 : first value of intArray arguments sent to requestTransform (radius).
 * @param b0 : first value of floatArray arguments sent to requestTransform (Standard Deviation).
 */
static void process(AndroidBitmapInfo* info, void* input, void* output, int a0, float b0){
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

        // Red, Blue, Green values of Output Pixel
    	int Pr[info->width][info->height];
        int Pg[info->width][info->height];
        int Pb[info->width][info->height];

        float G[size];

        // Used for intermediate processing as per 2nd method mentioned to do Gaussian Blur
        float** qr = new float*[info->width];
        float** qg = new float*[info->width];
        float** qb = new float*[info->width];

        // Allocation on heap memory
        for (i=0;i<info->width;i++){
            qr[i]=new float [info->height];
            qg[i]=new float [info->height];
            qb[i]=new float [info->height];
        }

        // Generate Gaussian Weight Vector G(k)
        for(i=0; i<size; i++){
            G[i]= (float) (exp(-((i-a0)*(i-a0))/(2*b0*b0))/sqrt(2*(M_PI)*b0*b0));
        }

        // Calculating values for qr, qg, qb
        for (j = 0; j < h; j++) {
            line1 = (uint32_t *)px;   // Traversing input pixels
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
            px = (char *) px + info->stride; // Moving to next row of input bitmap after processing the current row.
        }

        // Calculating values for Pr, Pg, Pb
        for (j = 0; j < h; j++) {
            line = (uint32_t *)pxo; // Traversing output pixels
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
            pxo = (char *) pxo + info->stride;  // Moving to next row of output bitmap after processing the current row.
        }

        // Releasing the memory allocated on heap
        for (i=0;i<info->width;i++){
            delete [] qr[i];
            delete [] qg[i];
            delete [] qb[i];
        }
        delete [] qr;
        delete [] qg;
        delete [] qb;
}



/*
 * JNI call for Gaussian Blur.
 * @param a0 : first value of intArray arguments sent to requestTransform (radius).
 * @param b0 : first value of floatArray arguments sent to requestTransform (Standard Deviation).
 * @param input: native equivalent of input bitmap.
 * @param output: native equivalent of output bitmap.
 */
JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_GaussianBlur_getGaussianBlur
  (JNIEnv * env, jclass jc, jint a0, jfloat b0, jobject input, jobject output){
              AndroidBitmapInfo  info_input;
              int ret;
              void* pixels_input;
              void* pixels_output;


              // Get info for input bitmap.
              if ((ret = AndroidBitmap_getInfo(env, input, &info_input)) < 0) {
                      LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
                      return;
                  }

              // Acquire a lock for input pixels.
              if ((ret = AndroidBitmap_lockPixels(env, input, &pixels_input)) < 0) {
                  LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
              }

                // Acquire a lock for output pixels.
                if ((ret = AndroidBitmap_lockPixels(env, output, &pixels_output)) < 0) {
                    LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
                }

              // Call process function for transform.
              process(&info_input, pixels_input, pixels_output, a0, b0);

              // Unlock pixels.
              AndroidBitmap_unlockPixels(env, input);
              AndroidBitmap_unlockPixels(env, output);
  }


