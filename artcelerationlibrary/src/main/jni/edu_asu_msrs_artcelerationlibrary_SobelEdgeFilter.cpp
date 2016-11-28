#include <edu_asu_msrs_artcelerationlibrary_SobelEdgeFilter.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <arm_neon.h>

#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "libimageprocessing"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

using namespace std;

static void process(AndroidBitmapInfo* info, void* input, void* gray, void* output,int a0){
            uint32_t* line;
            uint32_t* line1;

            void* pxi = input;
            void* pxg = gray;
            void* pxo = output;

            //vertical edge filter
            int sx[3][3] = {{-1,0,1}, {-2,0,2}, {-1,0,1}} ;

            // horizontal edge filter
            int sy[3][3] = {{-1,-2,-1}, {0,0,0}, {1,2,1}} ;

            // Image represented by 4-bytes (4 channels as A,R, G, B)
            int r, g, b;
            int pix;
            int i, j, k;
            int w = info->width;
            int h = info->height;
            int gg[w][h]; // Green

            uint8x8_t ascale = vdup_n_u8(1);
            uint8x8_t rscale = vdup_n_u8(77);
            uint8x8_t gscale = vdup_n_u8(151);
            uint8x8_t bscale = vdup_n_u8(28);
            /*
            h = h/8;
            for(i=0; i< w; i++){
                line = (uint8_t*)pxg;
                line1 = (uint8_t*)pxi;
                for(j=0; j< h; j++){
                    uint16x8_t temp;
                    uint8x8x4_t values = vld4_u8(line1);
                    uint8x8_t res;
                    temp = vmull_u8(values.val[0], ascale);
                    temp = vmlal_u8(temp, values.val[1], rscale);
                    temp = vmlal_u8(temp, values.val[2], gscale);
                    temp = vmlal_u8(temp, values.val[3], bscale);

                    res = vshrn_n_u16(temp, 8);
                    vst1_u8(line, res);
                    //pix=input.getPixel(i,j); // getPixel returns an integer value of the color of pixel

                    // Filtering for every channel a,r,g,b
                    //r=(int)(Color.red(pix)* 0.2989);
                    //g=(int)(Color.green(pix)* 0.5870);
                    //b=(int)(Color.blue(pix)* 0.1140);

                    //grayScale.setPixel(i,j,Color.argb(255,r,g,b));
                }
                pxg = (char*) pxg + info->stride;
                pxi = (char*) pxi + info->stride;
            }
            */
            for(int j=0; j<h; j++){
                line = (uint32_t*)pxg;
                line1 = (uint32_t*)pxi;
                        for(int i=0; i< w; i++){
                            //pix=input.getPixel(i,j); // getPixel returns an integer value of the color of pixel

                            // Filtering for every channel a,r,g,b
                            r = ((int)((line1[i] & 0x00FF0000)* 0.2989) >> 16);
                            g = ((int)((line1[i] & 0x0000FF00)* 0.5870) >> 8);
                            b = (int)((line1[i] & 0x00000FF)* 0.1140);

                            line[i] =
                            (((r << 16) & 0x00FF0000) |
                            ((g << 8) & 0x0000FF00) |
                            (b & 0x000000FF)|
                            0xFF000000);
                        }
                        pxg = (char*) pxg + info->stride;
                        pxi = (char*) pxi + info->stride;
                    }
            /*
            for (int i=0;i<w;i++) {
                for (int j = 0; j < h; j++) {
                    pix = grayScale.getPixel(i,j);
                    gg[i][j] = Color.green(pix);
                }
            }


            if (a0==0){
                int[][]Grx=new int[w][h];
                for (int i=1;i<w-1;i++) {
                    for (int j = 1; j < h-1; j++) {

                        Grx[i][j] = sx[0][0]*gg[i-1][j-1] + sx[1][0]*gg[i][j-1]+ sx[2][0]*gg[i+1][j-1]
                                + sx[0][1]*gg[i-1][j]+ sx[1][1]*gg[i][j]+ sx[2][1]*gg[i+1][j]
                                + sx[0][2]*gg[i-1][j+1] + sx[1][2]*gg[i][j+1]+ sx[2][2]*gg[i+1][j+1];

                        output.setPixel(i, j , Color.argb(255, Grx[i][j], Grx[i][j], Grx[i][j]));
                    }
                }
            }else if(a0==1){
                int[][]Gry=new int[w][h];
                for (int i=1;i<w-1;i++) {
                    for (int j = 1; j < h-1; j++) {

                        Gry[i][j] =sy[0][0]*gg[i-1][j-1] + sy[1][0]*gg[i][j-1]+ sy[2][0]*gg[i+1][j-1]
                                + sy[0][1]*gg[i-1][j]+ sy[1][1]*gg[i][j]+ sy[2][1]*gg[i+1][j]
                                + sy[0][2]*gg[i-1][j+1] + sy[1][2]*gg[i][j+1]+ sy[2][2]*gg[i+1][j+1];

                        output.setPixel(i, j , Color.argb(255, Gry[i][j], Gry[i][j], Gry[i][j]));
                    }
                }
            }else if (a0 == 2){
                int[][]Grx=new int[w][h];
                int[][]Gry=new int[w][h];
                for (int i=1;i<w-1;i++) {
                    for (int j = 1; j < h-1; j++) {

                        Grx[i][j] = sx[0][0]*gg[i-1][j-1] + sx[1][0]*gg[i][j-1]+ sx[2][0]*gg[i+1][j-1]
                                + sx[0][1]*gg[i-1][j]+ sx[1][1]*gg[i][j]+ sx[2][1]*gg[i+1][j]
                                + sx[0][2]*gg[i-1][j+1] + sx[1][2]*gg[i][j+1]+ sx[2][2]*gg[i+1][j+1];

                        Gry[i][j] =sy[0][0]*gg[i-1][j-1] + sy[1][0]*gg[i][j-1]+ sy[2][0]*gg[i+1][j-1]
                                + sy[0][1]*gg[i-1][j]+ sy[1][1]*gg[i][j]+ sy[2][1]*gg[i+1][j]
                                + sy[0][2]*gg[i-1][j+1] + sy[1][2]*gg[i][j+1]+ sy[2][2]*gg[i+1][j+1];

                        int Gr = (int) Math.sqrt(Grx[i][j]*Grx[i][j] + Gry[i][j]*Gry[i][j]);

                        output.setPixel(i, j , Color.argb(255, Gr, Gr, Gr));
                    }
                }
            }
            */
}



JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_SobelEdgeFilter_getSobelEdgeFilter
  (JNIEnv* env, jclass jc, jint a0, jobject input, jobject grayscale, jobject output){
          AndroidBitmapInfo  info_input;
          int ret;
          void* pixels_input;
          void* pixels_output;
          void* pixels_gray;

          // LOGD("a0=%d, a1=%d, size =%d\n", a0, a1, size);
          if ((ret = AndroidBitmap_getInfo(env, input, &info_input)) < 0) {
                  LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
                  return;
              }


          /*
          if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
              LOGE("Bitmap format is not RGBA_8888 !");
              return;
          }*/

          if ((ret = AndroidBitmap_lockPixels(env, input, &pixels_input)) < 0) {
              LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
          }

           if ((ret = AndroidBitmap_lockPixels(env, grayscale, &pixels_gray)) < 0) {
                        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
                    }

            if ((ret = AndroidBitmap_lockPixels(env, output, &pixels_output)) < 0) {
                LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
            }

          process(&info_input, pixels_input, pixels_gray, pixels_output, a0);

          AndroidBitmap_unlockPixels(env, input);
          AndroidBitmap_unlockPixels(env, output);
          AndroidBitmap_unlockPixels(env, grayscale);
  }

