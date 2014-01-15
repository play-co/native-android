LOCAL_PATH:= $(call my-dir)

LOCAL_LDFLAGS := -Wl,-Map,tealeaf.map

-include ${LOCAL_PATH}/profiler/android-ndk-profiler.mk

include $(CLEAR_VARS)
LOCAL_MODULE := curl-prebuilt
LOCAL_SRC_FILES := lib/libcurl.a 
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := crypto-prebuilt
LOCAL_SRC_FILES := lib/libgcypto.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ssl-prebuilt
LOCAL_SRC_FILES := lib/libgcl.so
include $(PREBUILT_SHARED_LIBRARY)

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
	core/qr/zxing/BarcodeFormat.cpp \
	core/qr/zxing/Binarizer.cpp \
	core/qr/zxing/BinaryBitmap.cpp \
	core/qr/zxing/ChecksumException.cpp \
	core/qr/zxing/DecodeHints.cpp \
	core/qr/zxing/Exception.cpp \
	core/qr/zxing/FormatException.cpp \
	core/qr/zxing/InvertedLuminanceSource.cpp \
	core/qr/zxing/LuminanceSource.cpp \
	core/qr/zxing/MultiFormatReader.cpp \
	core/qr/zxing/Reader.cpp \
	core/qr/zxing/Result.cpp \
	core/qr/zxing/ResultIO.cpp \
	core/qr/zxing/ResultPoint.cpp \
	core/qr/zxing/ResultPointCallback.cpp \
	core/qr/zxing/aztec/AztecDetectorResult.cpp \
	core/qr/zxing/aztec/AztecReader.cpp \
	core/qr/zxing/common/BitArray.cpp \
	core/qr/zxing/common/BitArrayIO.cpp \
	core/qr/zxing/common/BitMatrix.cpp \
	core/qr/zxing/common/BitSource.cpp \
	core/qr/zxing/common/CharacterSetECI.cpp \
	core/qr/zxing/common/DecoderResult.cpp \
	core/qr/zxing/common/DetectorResult.cpp \
	core/qr/zxing/common/GlobalHistogramBinarizer.cpp \
	core/qr/zxing/common/GreyscaleLuminanceSource.cpp \
	core/qr/zxing/common/GreyscaleRotatedLuminanceSource.cpp \
	core/qr/zxing/common/GridSampler.cpp \
	core/qr/zxing/common/HybridBinarizer.cpp \
	core/qr/zxing/common/IllegalArgumentException.cpp \
	core/qr/zxing/common/PerspectiveTransform.cpp \
	core/qr/zxing/common/Str.cpp \
	core/qr/zxing/common/StringUtils.cpp \
	core/qr/zxing/datamatrix/DataMatrixReader.cpp \
	core/qr/zxing/datamatrix/Version.cpp \
	core/qr/zxing/multi/ByQuadrantReader.cpp \
	core/qr/zxing/multi/GenericMultipleBarcodeReader.cpp \
	core/qr/zxing/multi/MultipleBarcodeReader.cpp \
	core/qr/zxing/oned/CodaBarReader.cpp \
	core/qr/zxing/oned/Code128Reader.cpp \
	core/qr/zxing/oned/Code39Reader.cpp \
	core/qr/zxing/oned/Code93Reader.cpp \
	core/qr/zxing/oned/EAN13Reader.cpp \
	core/qr/zxing/oned/EAN8Reader.cpp \
	core/qr/zxing/oned/ITFReader.cpp \
	core/qr/zxing/oned/MultiFormatOneDReader.cpp \
	core/qr/zxing/oned/MultiFormatUPCEANReader.cpp \
	core/qr/zxing/oned/OneDReader.cpp \
	core/qr/zxing/oned/OneDResultPoint.cpp \
	core/qr/zxing/oned/UPCAReader.cpp \
	core/qr/zxing/oned/UPCEANReader.cpp \
	core/qr/zxing/oned/UPCEReader.cpp \
	core/qr/zxing/pdf417/PDF417Reader.cpp \
	core/qr/zxing/qrcode/ErrorCorrectionLevel.cpp \
	core/qr/zxing/qrcode/FormatInformation.cpp \
	core/qr/zxing/qrcode/QRCodeReader.cpp \
	core/qr/zxing/qrcode/Version.cpp \
	core/qr/zxing/aztec/decoder/Decoder.cpp \
	core/qr/zxing/aztec/detector/Detector.cpp \
	core/qr/zxing/common/detector/MonochromeRectangleDetector.cpp \
	core/qr/zxing/common/detector/WhiteRectangleDetector.cpp \
	core/qr/zxing/common/reedsolomon/GenericGF.cpp \
	core/qr/zxing/common/reedsolomon/GenericGFPoly.cpp \
	core/qr/zxing/common/reedsolomon/ReedSolomonDecoder.cpp \
	core/qr/zxing/common/reedsolomon/ReedSolomonException.cpp \
	core/qr/zxing/datamatrix/decoder/BitMatrixParser.cpp \
	core/qr/zxing/datamatrix/decoder/DataBlock.cpp \
	core/qr/zxing/datamatrix/decoder/DecodedBitStreamParser.cpp \
	core/qr/zxing/datamatrix/decoder/Decoder.cpp \
	core/qr/zxing/datamatrix/detector/CornerPoint.cpp \
	core/qr/zxing/datamatrix/detector/Detector.cpp \
	core/qr/zxing/datamatrix/detector/DetectorException.cpp \
	core/qr/zxing/multi/qrcode/QRCodeMultiReader.cpp \
	core/qr/zxing/pdf417/decoder/BitMatrixParser.cpp \
	core/qr/zxing/pdf417/decoder/DecodedBitStreamParser.cpp \
	core/qr/zxing/pdf417/decoder/Decoder.cpp \
	core/qr/zxing/pdf417/detector/Detector.cpp \
	core/qr/zxing/pdf417/detector/LinesSampler.cpp \
	core/qr/zxing/qrcode/decoder/BitMatrixParser.cpp \
	core/qr/zxing/qrcode/decoder/DataBlock.cpp \
	core/qr/zxing/qrcode/decoder/DataMask.cpp \
	core/qr/zxing/qrcode/decoder/DecodedBitStreamParser.cpp \
	core/qr/zxing/qrcode/decoder/Decoder.cpp \
	core/qr/zxing/qrcode/decoder/Mode.cpp \
	core/qr/zxing/qrcode/detector/AlignmentPattern.cpp \
	core/qr/zxing/qrcode/detector/AlignmentPatternFinder.cpp \
	core/qr/zxing/qrcode/detector/Detector.cpp \
	core/qr/zxing/qrcode/detector/FinderPattern.cpp \
	core/qr/zxing/qrcode/detector/FinderPatternFinder.cpp \
	core/qr/zxing/qrcode/detector/FinderPatternInfo.cpp \
	core/qr/zxing/multi/qrcode/detector/MultiDetector.cpp \
	core/qr/zxing/multi/qrcode/detector/MultiFinderPatternFinder.cpp \
	core/qr/zxing/pdf417/decoder/ec/ErrorCorrection.cpp \
	core/qr/zxing/pdf417/decoder/ec/ModulusGF.cpp \
	core/qr/zxing/pdf417/decoder/ec/ModulusPoly.cpp \
	core/qr/bigint/BigInteger.cc \
	core/qr/bigint/BigIntegerAlgorithms.cc \
	core/qr/bigint/BigIntegerUtils.cc \
	core/qr/bigint/BigUnsigned.cc \
	core/qr/bigint/BigUnsignedInABase.cc \
	core/qr/libqrencode/bitstream.c \
	core/qr/libqrencode/mask.c \
	core/qr/libqrencode/mmask.c \
	core/qr/libqrencode/mqrspec.c \
	core/qr/libqrencode/qrencode.c \
	core/qr/libqrencode/qrinput.c \
	core/qr/libqrencode/qrspec.c \
	core/qr/libqrencode/rsecc.c \
	core/qr/libqrencode/split.c \
	core/qr/adapter/QRCodeProcessor.cpp \
	core/qr/adapter/BufferBitmapSource.cpp

LOCAL_SRC_FILES += $(QR_SRC_FILES)
LOCAL_CFLAGS += -Ijni/core/qr

LOCAL_STATIC_LIBRARIES := curl-prebuilt libzip cpufeatures libturbojpeg libpng libjansson
LOCAL_LDLIBS :=-llog -lGLESv2 -lz
LOCAL_CFLAGS += -Wall -Werror -Wno-psabi -Wno-unused-function -Wno-unused-but-set-variable -O3 -funroll-loops -ftree-vectorize -ffast-math

ifeq ($(APP_ABI),armeabi-v7a)
	LOCAL_CFLAGS += -march=armv7-a -mfloat-abi=softfp 
endif

LOCAL_STATIC_LIBRARIES += libv8a

LOCAL_C_INCLUDES := $(LOCAL_PATH)/deps
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core/deps
LOCAL_C_INCLUDES += $(LOCAL_PATH)/core/image-cache/include
LOCAL_SHARED_LIBRARIES += ssl-prebuilt
LOCAL_SHARED_LIBRARIES += crypto-prebuilt

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

