#include <edu_asu_msrs_artcelerationlibrary_ColorFilter.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "libimageprocessing"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)



static void process(AndroidBitmapInfo* info, void* input, void* output, int arr[]){
        int i, j, k;
        uint32_t* line;
        uint32_t* line1;
    	void* px = input;
        void* pxo = output;
        int r, g, b;
        int w = info->width;
        int h = info->height;

        // Red channel slopes and constant
        float Rm1 = 0;
        float Rc2 = 0;
        float Rm2 = 0;
        float Rc3 = 0;
        float Rm3 = 0;
        float Rc4 = 0;
        float Rm4 = 0;
        float Rc5 = 0;
        float Rm5 = 0;

        // Green channel slopes and constant
        float Gm1 = 0;
        float Gc2 = 0;
        float Gm2 = 0;
        float Gc3 = 0;
        float Gm3 = 0;
        float Gc4 = 0;
        float Gm4 = 0;
        float Gc5 = 0;
        float Gm5 = 0;

        // Blue channel slopes and constant
        float Bm1 = 0;
        float Bc2 = 0;
        float Bm2 = 0;
        float Bc3 = 0;
        float Bm3 = 0;
        float Bc4 = 0;
        float Bm4 = 0;
        float Bc5 = 0;
        float Bm5 = 0;

        // Slopes for red; when p0!=0 and p6!=255
        if (arr[0]!=0 && arr[6]!=255) {
            Rm1 = ((float) arr[1] / arr[0]);
            Rm2 = ((float) arr[3] - arr[1]) / (arr[2] - arr[0]);
            Rc2 = arr[1] - (Rm2 * arr[0]);
            Rm3 = ((float) arr[5] - arr[3]) / (arr[4] - arr[2]);
            Rc3 = arr[3] - (Rm3 * arr[2]);
            Rm4 = ((float) arr[7] - arr[5]) / (arr[6] - arr[4]);
            Rc4 = arr[5] - (Rm4 * arr[4]);
            Rm5 = (((float) 255 - arr[7]) / (255 - arr[6]));
            Rc5 = arr[7] - (Rm5 * arr[6]);
        }else if (arr[0]==0 && arr[6]!=255){
            Rm2 = ((float) arr[3] - arr[1]) / (arr[2] - arr[0]);
            Rc2 = arr[1];
            Rm3 = ((float) arr[5] - arr[3]) / (arr[4] - arr[2]);
            Rc3 = arr[3] - (Rm3 * arr[2]);
            Rm4 = ((float) arr[7] - arr[5]) / (arr[6] - arr[4]);
            Rc4 = arr[5] - (Rm4 * arr[4]);
            Rm5 = (((float) 255 - arr[7]) / (255 - arr[6]));
            Rc5 = arr[7] - (Rm5 * arr[6]);
        }else if (arr[0]==0 && arr[6]==255){
            Rm2 = ((float) arr[3] - arr[1]) / (arr[2] - arr[0]);
            Rc2 = arr[1];
            Rm3 = ((float) arr[5] - arr[3]) / (arr[4] - arr[2]);
            Rc3 = arr[3] - (Rm3 * arr[2]);
            Rm4 = ((float) arr[7] - arr[5]) / (arr[6] - arr[4]);
            Rc4 = arr[5] - (Rm4 * arr[4]);
        }else if (arr[0]!=0 && arr[6]==255){
            Rm1 = ((float) arr[1] / arr[0]);
            Rm2 = ((float) arr[3] - arr[1]) / (arr[2] - arr[0]);
            Rc2 = arr[1] - (Rm2 * arr[0]);
            Rm3 = ((float) arr[5] - arr[3]) / (arr[4] - arr[2]);
            Rc3 = arr[3] - (Rm3 * arr[2]);
            Rm4 = ((float) arr[7] - arr[5]) / (arr[6] - arr[4]);
            Rc4 = arr[5] - (Rm4 * arr[4]);
        }

        // Slopes for green
        if (arr[8]!=0 && arr[14]!=255){
            Gm1 = ((float) arr[9] / arr[8]);
            Gm2 = ((float) arr[11] - arr[9]) / (arr[10] - arr[8]);
            Gc2 = arr[9] - (Gm2 * arr[8]);
            Gm3 = ((float) arr[13] - arr[11]) / (arr[12] - arr[10]);
            Gc3 = arr[11] - (Gm3 * arr[10]);
            Gm4 = ((float) arr[15] - arr[13]) / (arr[14] - arr[12]);
            Gc4 = arr[13] - (Gm4 * arr[12]);
            Gm5 = (((float) 255 - arr[15]) / (255 - arr[14]));
            Gc5 = arr[15] - (Gm5 * arr[14]);
        }else if (arr[8]==0 && arr[14]!=255){
            Gm2 = ((float) arr[11] - arr[9]) / (arr[10] - arr[8]);
            Gc2 = arr[9];
            Gm3 = ((float) arr[13] - arr[11]) / (arr[12] - arr[10]);
            Gc3 = arr[11] - (Gm3 * arr[10]);
            Gm4 = ((float) arr[15] - arr[13]) / (arr[14] - arr[12]);
            Gc4 = arr[13] - (Gm4 * arr[12]);
            Gm5 = (((float) 255 - arr[15]) / (255 - arr[14]));
            Gc5 = arr[15] - (Gm5 * arr[14]);
        }else if (arr[8]==0 && arr[14]==255){
            Gm2 = ((float) arr[11] - arr[9]) / (arr[10] - arr[8]);
            Gc2 = arr[9];
            Gm3 = ((float) arr[13] - arr[11]) / (arr[12] - arr[10]);
            Gc3 = arr[11] - (Gm3 * arr[10]);
            Gm4 = ((float) arr[15] - arr[13]) / (arr[14] - arr[12]);
            Gc4 = arr[13] - (Gm4 * arr[12]);
        }else if (arr[8]!=0 && arr[14]==255){
            Gm1 = ((float) arr[9] / arr[8]);
            Gm2 = ((float) arr[11] - arr[9]) / (arr[10] - arr[8]);
            Gc2 = arr[9] - (Gm2 * arr[8]);
            Gm3 = ((float) arr[13] - arr[11]) / (arr[12] - arr[10]);
            Gc3 = arr[11] - (Gm3 * arr[10]);
            Gm4 = ((float) arr[15] - arr[13]) / (arr[14] - arr[12]);
            Gc4 = arr[13] - (Gm4 * arr[12]);
        }

        // Slopes for blue
        if (arr[16]!=0 && arr[22]!=255){
            Bm1 = ((float) arr[17] / arr[16]);
            Bm2 = ((float) arr[19] - arr[17]) / (arr[18] - arr[16]);
            Bc2 = arr[17] - (Bm2 * arr[16]);
            Bm3 = ((float) arr[21] - arr[19]) / (arr[20] - arr[18]);
            Bc3 = arr[19] - (Bm3 * arr[18]);
            Bm4 = ((float) arr[23] - arr[21]) / (arr[22] - arr[20]);
            Bc4 = arr[21] - (Bm4 * arr[20]);
            Bm5 = (((float) 255 - arr[23]) / (255 - arr[22]));
            Bc5 = arr[23] - ((((float) 255 - arr[23]) / (255 - arr[22])) * arr[22]);
        }else if (arr[16]==0 && arr[22]!=255){
            Bm2 = ((float) arr[19] - arr[17]) / (arr[18] - arr[16]);
            Bc2 = arr[17];
            Bm3 = ((float) arr[21] - arr[19]) / (arr[20] - arr[18]);
            Bc3 = arr[19] - (Bm3 * arr[18]);
            Bm4 = ((float) arr[23] - arr[21]) / (arr[22] - arr[20]);
            Bc4 = arr[21] - (Bm4 * arr[20]);
            Bm5 = (((float) 255 - arr[23]) / (255 - arr[22]));
            Bc5 = arr[23] - ((((float) 255 - arr[23]) / (255 - arr[22])) * arr[22]);
        }else if (arr[16]==0 && arr[22]==255){
            Bm2 = ((float) arr[19] - arr[17]) / (arr[18] - arr[16]);
            Bc2 = arr[17];
            Bm3 = ((float) arr[21] - arr[19]) / (arr[20] - arr[18]);
            Bc3 = arr[19] - (Bm3 * arr[18]);
            Bm4 = ((float) arr[23] - arr[21]) / (arr[22] - arr[20]);
            Bc4 = arr[21] - (Bm4 * arr[20]);
        }else if (arr[16]!=0 && arr[22]==255){
            Bm1 = ((float) arr[17] / arr[16]);
            Bm2 = ((float) arr[19] - arr[17]) / (arr[18] - arr[16]);
            Bc2 = arr[17] - (Bm2 * arr[16]);
            Bm3 = ((float) arr[21] - arr[19]) / (arr[20] - arr[18]);
            Bc3 = arr[19] - (Bm3 * arr[18]);
            Bm4 = ((float) arr[23] - arr[21]) / (arr[22] - arr[20]);
            Bc4 = arr[21] - (Bm4 * arr[20]);
        }

        // Iterate over all pixels
        for(int j=0; j<h; j++){
            line1 = (uint32_t *)px;
            line = (uint32_t *)pxo;
            for(int i=0; i< w; i++){
                r = ((line1[i] & 0x00FF0000) >> 16);
                g = ((line1[i] & 0x0000FF00) >> 8);
                b = (line1[i] & 0x00000FF);

                // Mapping for red channel
                if (arr[0]!=0 && arr[6]!=255){
                    if (r<=arr[0]){
                        r = (int) (Rm1*r);
                    }else if(r>arr[0] && r<=arr[2]) {
                        r= (int) ((Rm2*r)+Rc2);
                    }else if(r>arr[2] && r<=arr[4]){
                        r=(int) ((Rm3*r)+Rc3);
                    }else if (r>arr[4] && r<=arr[6]){
                        r= (int) ((Rm4*r)+Rc4);
                    }else{
                        r= (int) ((Rm5*r)+Rc5);
                    }
                }else if (arr[0]==0 && arr[6]!=255){
                    if(r<=arr[2]) {
                        r= (int) ((Rm2*r)+Rc2);
                    }else if(r>arr[2] && r<=arr[4]){
                        r=(int) ((Rm3*r)+Rc3);
                    }else if (r>arr[4] && r<=arr[6]){
                        r= (int) ((Rm4*r)+Rc4);
                    }else{
                        r= (int) ((Rm5*r)+Rc5);
                    }
                }else if (arr[0]==0 && arr[6]==255){
                    if(r<=arr[2]) {
                        r= (int) ((Rm2*r)+Rc2);
                    }else if(r>arr[2] && r<=arr[4]){
                        r=(int) ((Rm3*r)+Rc3);
                    }else{
                        r= (int) ((Rm4*r)+Rc4);
                    }
                }else if (arr[0]!=0 && arr[6]==255){
                    if (r<=arr[0]){
                        r = (int) (Rm1*r);
                    }else if(r>arr[0] && r<=arr[2]) {
                        r= (int) ((Rm2*r)+Rc2);
                    }else if(r>arr[2] && r<=arr[4]){
                        r=(int) ((Rm3*r)+Rc3);
                    }else{
                        r= (int) ((Rm4*r)+Rc4);
                    }
                }

                // Mapping for green channel
                if (arr[8]!=0 && arr[14]!=255){
                    if (g<=arr[8]){
                        g = (int) (Gm1*g);
                    }else if(g>=arr[8] && g<=arr[10]) {
                        g= (int) ((Gm2*g)+Gc2);
                    }else if(g>=arr[10] && g<=arr[12]){
                        g=(int) ((Gm3*g)+Gc3);
                    }else if (g>=arr[12] && g<=arr[14]){
                        g= (int) ((Gm4*g)+Gc4);
                    }else{
                        g= (int) ((Gm5*g)+Gc5);
                    }
                }else if (arr[8]==0 && arr[14]!=255){
                    if(g<=arr[10]) {
                        g= (int) ((Gm2*g)+Gc2);
                    }else if(g>=arr[10] && g<=arr[12]){
                        g=(int) ((Gm3*g)+Gc3);
                    }else if (g>=arr[12] && g<=arr[14]){
                        g= (int) ((Gm4*g)+Gc4);
                    }else{
                        g= (int) ((Gm5*g)+Gc5);
                    }
                }else if (arr[8]==0 && arr[14]==255){
                    if(g<=arr[10]) {
                        g= (int) ((Gm2*g)+Gc2);
                    }else if(g>=arr[10] && g<=arr[12]){
                        g=(int) ((Gm3*g)+Gc3);
                    }else{
                        g= (int) ((Gm4*g)+Gc4);
                    }
                }else if (arr[8]!=0 && arr[14]==255){
                    if (g<=arr[8]){
                        g = (int) (Gm1*g);
                    }else if(g>=arr[8] && g<=arr[10]) {
                        g= (int) ((Gm2*g)+Gc2);
                    }else if(g>=arr[10] && g<=arr[12]){
                        g=(int) ((Gm3*g)+Gc3);
                    }else{
                        g= (int) ((Gm4*g)+Gc4);
                    }
                }

                // Mapping for blue channel
                if (arr[16]!=0 && arr[22]!=255){
                    if (b<=arr[16]){
                        b = (int) (Bm1*b);
                    }else if(b>=arr[16] && b<=arr[18]) {
                        b= (int) ((Bm2*b)+Bc2);
                    }else if(b>=arr[18] && b<=arr[20]){
                        b=(int) ((Bm3*b)+Bc3);
                    }else if (b>=arr[20] && b<=arr[22]){
                        b= (int) ((Bm4*b)+Bc4);
                    }else{
                        b= (int) ((Bm5*b)+Bc5);
                    }
                }else if (arr[16]==0 && arr[22]!=255){
                    if(b<=arr[18]) {
                        b= (int) ((Bm2*b)+Bc2);
                    }else if(b>=arr[18] && b<=arr[20]){
                        b=(int) ((Bm3*b)+Bc3);
                    }else if (b>=arr[20] && b<=arr[22]){
                        b= (int) ((Bm4*b)+Bc4);
                    }else{
                        b= (int) ((Bm5*b)+Bc5);
                    }
                }else if (arr[16]==0 && arr[22]==255){
                    if(b<=arr[18]) {
                        b= (int) ((Bm2*b)+Bc2);
                    }else if(b>=arr[18] && b<=arr[20]){
                        b=(int) ((Bm3*b)+Bc3);
                    }else{
                        b= (int) ((Bm4*b)+Bc4);
                    }
                }else if (arr[16]!=0 && arr[22]==255){
                    if (b<=arr[16]){
                        b = (int) (Bm1*b);
                    }else if(b>=arr[16] && b<=arr[18]) {
                        b= (int) ((Bm2*b)+Bc2);
                    }else if(b>=arr[18] && b<=arr[20]){
                        b=(int) ((Bm3*b)+Bc3);
                    }else{
                        b= (int) ((Bm4*b)+Bc4);
                    }
                }

                line[i] =
                (((r << 16) & 0x00FF0000) |
                ((g << 8) & 0x0000FF00) |
                (b & 0x000000FF) |
                (0xFF000000));
            }
            px = (char *) px + info->stride;
            pxo = (char *) pxo + info->stride;
        }
}

JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_ColorFilter_getColorFilter
  (JNIEnv * env, jclass  jc, jintArray array, jobject input, jobject output){
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


              jint *inCArray = (env)->GetIntArrayElements(array, NULL);
              //if (NULL == inCArray) return NULL;
              jsize length = (env)->GetArrayLength(array);

              process(&info_input, pixels_input, pixels_output, inCArray);

              (env)->ReleaseIntArrayElements(array, inCArray, 0); // release resources

              AndroidBitmap_unlockPixels(env, input);
              AndroidBitmap_unlockPixels(env, output);
  }

