LOCAL_PATH := $(call my-dir)

    # Build library 1
	include $(CLEAR_VARS)
	LOCAL_SRC_FILES := edu_asu_msrs_artcelerationlibrary_MotionBlur.cpp
	LOCAL_LDLIBS := -llog
	LOCAL_LDFLAGS += -ljnigraphics
	LOCAL_MODULE := MotionBlurLib
	include $(BUILD_SHARED_LIBRARY)

	# Build library 2
    include $(CLEAR_VARS)
    LOCAL_SRC_FILES := edu_asu_msrs_artcelerationlibrary_ColorFilter.cpp
    LOCAL_LDLIBS := -llog
    LOCAL_LDFLAGS += -ljnigraphics
    LOCAL_MODULE := ColorFilterLib
    include $(BUILD_SHARED_LIBRARY)

    # Build library 3
    include $(CLEAR_VARS)
    LOCAL_SRC_FILES := edu_asu_msrs_artcelerationlibrary_SobelEdgeFilter.cpp
    LOCAL_LDLIBS := -llog
    LOCAL_LDFLAGS += -ljnigraphics
    LOCAL_MODULE := SobelEdgeFilterLib
    LOCAL_ARM_NEON := true
    include $(BUILD_SHARED_LIBRARY)

    # Build library 4
    include $(CLEAR_VARS)
    LOCAL_SRC_FILES := edu_asu_msrs_artcelerationlibrary_GaussianBlur.cpp
    LOCAL_LDLIBS := -llog
    LOCAL_LDFLAGS += -ljnigraphics
    LOCAL_MODULE := GaussianBlurLib
    LOCAL_ARM_NEON := true
    include $(BUILD_SHARED_LIBRARY)

    # Build library 5
    include $(CLEAR_VARS)
    LOCAL_SRC_FILES := edu_asu_msrs_artcelerationlibrary_UnsharpMask.cpp
    LOCAL_LDLIBS := -llog
    LOCAL_LDFLAGS += -ljnigraphics
    LOCAL_MODULE := UnsharpMaskLib
    LOCAL_ARM_NEON := true
    include $(BUILD_SHARED_LIBRARY)

