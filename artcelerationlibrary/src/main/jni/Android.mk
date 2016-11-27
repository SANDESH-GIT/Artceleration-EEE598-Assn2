LOCAL_PATH := $(call my-dir)

	include $(CLEAR_VARS)

	LOCAL_SRC_FILES := edu_asu_msrs_artcelerationlibrary_MotionBlur.cpp

	LOCAL_LDLIBS := -llog
	LOCAL_LDFLAGS += -ljnigraphics
	LOCAL_MODULE := MotionBlurLib


	include $(BUILD_SHARED_LIBRARY)