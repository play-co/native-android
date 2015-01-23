LOCAL_PATH:= $(call my-dir)

# build libpng
include $(CLEAR_VARS)
LOCAL_MODULE := libpng
LOCAL_SRC_FILES := \
	libpng/png.c \
	libpng/pngerror.c \
	libpng/pngget.c \
	libpng/pngmem.c \
	libpng/pngpread.c \
	libpng/pngread.c \
	libpng/pngrio.c \
	libpng/pngrtran.c \
	libpng/pngrutil.c \
	libpng/pngset.c \
	libpng/pngtrans.c \
	libpng/pngwio.c \
	libpng/pngwrite.c \
	libpng/pngwtran.c \
	libpng/pngwutil.c \

LOCAL_EXPORT_LDLIBS := -lz
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/libpng
include $(BUILD_STATIC_LIBRARY)

# build libzip
include $(CLEAR_VARS)
LOCAL_MODULE    := libzip
LOCAL_SRC_FILES :=\
	libzip/zip_add.c \
	libzip/zip_add_dir.c \
	libzip/zip_close.c \
	libzip/zip_delete.c \
	libzip/zip_dirent.c \
	libzip/zip_entry_free.c \
	libzip/zip_entry_new.c \
	libzip/zip_err_str.c \
	libzip/zip_error.c \
	libzip/zip_error_clear.c \
	libzip/zip_error_get.c \
	libzip/zip_error_get_sys_type.c \
	libzip/zip_error_strerror.c \
	libzip/zip_error_to_str.c \
	libzip/zip_fclose.c \
	libzip/zip_file_error_clear.c \
	libzip/zip_file_error_get.c \
	libzip/zip_file_get_offset.c \
	libzip/zip_file_strerror.c \
	libzip/zip_filerange_crc.c \
	libzip/zip_fopen.c \
	libzip/zip_fopen_index.c \
	libzip/zip_fread.c \
	libzip/zip_free.c \
	libzip/zip_get_archive_comment.c \
	libzip/zip_get_archive_flag.c \
	libzip/zip_get_file_comment.c \
	libzip/zip_get_num_files.c \
	libzip/zip_get_name.c \
	libzip/zip_memdup.c \
	libzip/zip_name_locate.c \
	libzip/zip_new.c \
	libzip/zip_open.c \
	libzip/zip_rename.c \
	libzip/zip_replace.c \
	libzip/zip_set_archive_comment.c \
	libzip/zip_set_archive_flag.c \
	libzip/zip_set_file_comment.c \
	libzip/zip_source_buffer.c \
	libzip/zip_source_file.c \
	libzip/zip_source_filep.c \
	libzip/zip_source_free.c \
	libzip/zip_source_function.c \
	libzip/zip_source_zip.c \
	libzip/zip_set_name.c \
	libzip/zip_stat.c \
	libzip/zip_stat_index.c \
	libzip/zip_stat_init.c \
	libzip/zip_strerror.c \
	libzip/zip_unchange.c \
	libzip/zip_unchange_all.c \
	libzip/zip_unchange_archive.c \
	libzip/zip_unchange_data.c

LOCAL_LDLIBS := -lz

#include $(BUILD_SHARED_LIBRARY)
include $(BUILD_STATIC_LIBRARY)

LOCAL_LDFLAGS := -Wl,-Map,tealeaf.map

-include ${LOCAL_PATH}/profiler/android-ndk-profiler.mk

include $(CLEAR_VARS)
LOCAL_MODULE := curl-prebuilt
LOCAL_SRC_FILES := lib/libcurl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := crypto-prebuilt
LOCAL_SRC_FILES := lib/libcrypto.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ssl-prebuilt
LOCAL_SRC_FILES := lib/libssl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libv8a
LOCAL_SRC_FILES := lib/libv8.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libturbojpeg
LOCAL_SRC_FILES := lib/libturbojpeg.a
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
			js/js_image_cache.cpp                             \
			js/js_input.cpp	                                  \
			js/js_status_bar.cpp	                	      \
			js/js_local_storage.cpp                           \
			js/js_locale.cpp		                          \
			js/js_location.cpp                                \
			js/js_native.cpp                                  \
			js/js_navigator.cpp                               \
			js/js_overlay.cpp                                 \
			js/js_photo.cpp                                   \
			js/js_profiler.cpp		                          \
			js/js_plugins.cpp                                 \
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
			core/graphics_utils.c                             \
			core/image-cache/src/image_cache.c                \
			core/image-cache/src/murmur.c                     \
			core/image_loader.c                               \
			core/image_writer.c                               \
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
			platform/input.cpp                                \
			platform/status_bar.cpp                           \
			platform/local_storage.cpp                        \
			platform/get_locale.cpp		                      \
			platform/location_manager.cpp                     \
			platform/native.cpp                               \
			platform/native_shim.cpp	                      \
			platform/navigator.cpp                            \
			platform/overlay.cpp                              \
			platform/photo.cpp                                \
			platform/profiler.cpp		                      \
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
			js/js_string_cache.cpp                            \
			gen/js_timestep_image_map_template.gen.cpp

PROFILE_SRC_FILES := 	lib/v8-profiler/cpu_profiler.cpp	  \
			lib/v8-profiler/heap_profiler.cpp	              \
			lib/v8-profiler/node.cpp		                  \
			lib/v8-profiler/node_buffer.cpp		              \
			lib/v8-profiler/profiler.cpp

QR_SRC_FILES := \
	core/qr/libqrencode/bitstream.c \
	core/qr/libqrencode/mask.c \
	core/qr/libqrencode/mmask.c \
	core/qr/libqrencode/mqrspec.c \
	core/qr/libqrencode/qrencode.c \
	core/qr/libqrencode/qrinput.c \
	core/qr/libqrencode/qrspec.c \
	core/qr/libqrencode/rsecc.c \
	core/qr/libqrencode/split.c \
	core/qr/quirc/decode.c \
	core/qr/quirc/identify.c \
	core/qr/quirc/quirc.c \
	core/qr/quirc/version_db.c \
	core/qr/adapter/qrprocess.c

LOCAL_STATIC_LIBRARIES := curl-prebuilt libzip cpufeatures libturbojpeg libjansson libpng libv8a
LOCAL_LDLIBS := -llog -lGLESv2 -lz
LOCAL_CFLAGS += -Wall -Werror -Wno-narrowing -Wno-psabi -Wno-unused-function -Wno-unused-but-set-variable -O3 -funroll-loops -ftree-vectorize -ffast-math -Wno-maybe-uninitialized

ifeq ($(APP_ABI),armeabi-v7a)
	LOCAL_CFLAGS += -march=armv7-a -mfloat-abi=softfp 
endif

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/deps
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core/deps
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core/image-cache/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core/qr
LOCAL_SHARED_LIBRARIES += ssl-prebuilt
LOCAL_SHARED_LIBRARIES += crypto-prebuilt

# QR codes
LOCAL_SRC_FILES += $(QR_SRC_FILES)

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

