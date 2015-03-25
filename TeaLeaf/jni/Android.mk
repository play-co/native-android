LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libtealeaf
LOCAL_SRC_FILES := lib/libtealeaf.so
include $(PREBUILT_SHARED_LIBRARY)
