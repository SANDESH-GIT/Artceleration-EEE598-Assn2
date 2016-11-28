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



static void process(AndroidBitmapInfo* info, void* pixels, jintArray array){

}

JNIEXPORT void JNICALL Java_edu_asu_msrs_artcelerationlibrary_ColorFilter_getColorFilter
  (JNIEnv * env, jclass  jc, jintArray array, jobject bitmap){
     AndroidBitmapInfo  info;
          int ret;
          void* pixels;

          //LOGD("a0=%d, a1=%d, size =%d\n", a0, a1, size);
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

          process(&info,pixels, array);

          AndroidBitmap_unlockPixels(env, bitmap);
  }

