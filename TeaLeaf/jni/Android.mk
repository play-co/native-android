LOCAL_PATH:= $(call my-dir)

LOCAL_LDFLAGS := -Wl,-Map,tealeaf.map

APP_ABI := armeabi

-include ${LOCAL_PATH}/profiler/android-ndk-profiler.mk

include $(CLEAR_VARS)
LOCAL_MODULE := libv8a
LOCAL_SRC_FILES := lib/libv8.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libzip
LOCAL_SRC_FILES := lib/libzip.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libpng
LOCAL_SRC_FILES := lib/libpng.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libjpeg
LOCAL_SRC_FILES := lib/libjpeg.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libjansson
LOCAL_SRC_FILES := lib/libjansson.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)


LOCAL_MODULE    := libtealeaf
LOCAL_SRC_FILES :=  	js/js.cpp                             \
			js/js_animate.cpp                                 \
			js/js_build.cpp			                          \
			js/js_console.cpp                                 \
			js/js_context.cpp                                 \
			js/js_device.cpp                                  \
			js/js_dialog.cpp                                  \
			js/js_events.cpp                                  \
			js/js_gc.cpp                                      \
			js/js_haptics.cpp                                 \
			js/js_input_prompt.cpp	                	      \
			js/js_local_storage.cpp                           \
			js/js_locale.cpp		                          \
			js/js_location.cpp                                \
			js/js_native.cpp                                  \
			js/js_navigator.cpp                               \
			js/js_overlay.cpp                                 \
			js/js_photo.cpp                                   \
			js/js_profiler.cpp		                          \
			js/js_purchase.cpp                                \
			js/js_plugins.cpp                                  \
			js/js_socket.cpp                                  \
			js/js_sound.cpp                                   \
			js/js_textbox.cpp                                 \
			js/js_timer.cpp                                   \
			js/js_timestep_events.cpp                         \
			js/js_timestep_image_map.cpp                      \
			js/js_timestep_view.cpp                           \
			js/js_xhr.cpp                                     \
			core/config.c                                     \
			core/core.c                                       \
			core/draw_textures.c                              \
			core/events.c                                     \
			core/geometry.c                                   \
			core/image_loader.c                               \
			core/object_pool.c                                \
			core/rgba.c                                       \
			core/tealeaf_canvas.c                             \
			core/tealeaf_context.c                            \
			core/tealeaf_shaders.c                            \
			core/texture_2d.c                                 \
			core/texture_manager.c                            \
			core/timer.c                                      \
			core/url_loader.c                                 \
			platform/build.cpp		                          \
			platform/device.cpp                               \
			platform/dialog.cpp                               \
			platform/haptics.cpp                              \
			platform/input_prompt.cpp	                      \
			platform/local_storage.cpp                        \
			platform/get_locale.cpp		                      \
			platform/location_manager.cpp                     \
			platform/native.cpp                               \
			platform/native_shim.cpp	                      \
			platform/navigator.cpp                            \
			platform/overlay.cpp                              \
			platform/photo.cpp                                \
			platform/profiler.cpp		                      \
			platform/purchase.cpp                             \
			platform/resource_loader.cpp                      \
			platform/plugins.cpp                              \
			platform/socket.cpp                               \
			platform/sound_manager.cpp                        \
			platform/text_manager.cpp                         \
			platform/textbox.cpp                              \
			platform/threads.cpp                              \
			platform/xhr.cpp                                  \
			core/timestep/timestep_animate.cpp                \
			core/timestep/timestep_events.cpp                 \
			core/timestep/timestep_image_map.cpp              \
			core/timestep/timestep_view.cpp                   \
			gen/js_timestep_view_template.gen.cpp             \
			js/js_string_cache.cpp                           \
			gen/js_timestep_image_map_template.gen.cpp

PROFILE_SRC_FILES := 	lib/v8-profiler/cpu_profiler.cpp	  \
			lib/v8-profiler/heap_profiler.cpp	              \
			lib/v8-profiler/node.cpp		                  \
			lib/v8-profiler/node_buffer.cpp		              \
			lib/v8-profiler/profiler.cpp

LOCAL_STATIC_LIBRARIES := libzip cpufeatures libjpeg libpng libjansson
LOCAL_LDLIBS :=-llog -lGLESv2 -lz #-fuse-ld=gold REQUIRES: android-ndk-r8b
LOCAL_CFLAGS += -Wall -Werror -Wno-psabi -Wno-unused-function -Wno-unused-but-set-variable -march=armv5te -mtune=arm926ej-s -mfloat-abi=soft

LOCAL_STATIC_LIBRARIES += libv8a

LOCAL_C_INCLUDES := $(LOCAL_PATH)/deps
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core/deps

#RELEASE will stub out the LOG function
ifeq (${RELEASE}, 1)
LOCAL_CFLAGS += -DRELEASE
APP_OPTIM := release

# Add profiler to release mode
ifeq (${JSPROF}, 1)
LOCAL_CFLAGS += -DENABLE_PROFILER -DREMOTE_DEBUG
LOCAL_SRC_FILES += $(PROFILE_SRC_FILES)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/deps/v8
endif

# DEBUG build
else
# Profiler is always on for debug mode
LOCAL_CFLAGS += -DHASH_DEBUG=1 -DDEBUG -gstabs+ -DENABLE_PROFILER -DREMOTE_DEBUG
APP_OPTIM := debug
LOCAL_SRC_FILES += $(PROFILE_SRC_FILES)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/deps/v8
endif

ifeq (${GPROF}, 1)
LOCAL_CFLAGS += -DPROFILE -pg -fno-omit-frame-pointer -fno-function-sections
LOCAL_STATIC_LIBRARIES += andprof
endif
include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)

