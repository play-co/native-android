
include $(CLEAR_VARS)
cmd-strip :=
LOCAL_MODULE := tealeaf
LOCAL_SRC_FILES := $(PWD)/jniLibs/$(TARGET_ARCH_ABI)/libtealeaf.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libtealeafNative
LOCAL_SRC_FILES := $(PWD)/jni/native_initialization.cpp
LOCAL_LDLIBS  := -ltealeaf
LOCAL_LDFLAGS += -L$(PWD)/jniLibs/$(TARGET_ARCH_ABI)
include $(BUILD_SHARED_LIBRARY)
