LCOAL_HURRAY := $(shell echo "THIS THING IS ACTUALLY RUNNING")

include $(CLEAR_VARS)

LOCAL_MODULE := libpng
LOCAL_SRC_FILES := \
	png.c \
	pngerror.c \
	pngget.c \
	pngmem.c \
	pngpread.c \
	pngread.c \
	pngrio.c \
	pngrtran.c \
	pngrutil.c \
	pngset.c \
	pngtrans.c \
	pngwio.c \
	pngwrite.c \
	pngwtran.c \
	pngwutil.c \

LOCAL_EXPORT_LDLIBS := -lz
LOCAL_EXPORT_C_INCLUDES := $(call mydir)

include $(BUILD_STATIC_LIBRARY)
