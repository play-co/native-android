/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License v. 2.0 as published by Mozilla.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v. 2.0 for more details.

 * You should have received a copy of the Mozilla Public License v. 2.0
 * along with the Game Closure SDK.  If not, see <http://mozilla.org/MPL/2.0/>.
 */
#ifndef PLATFORM_H
#define PLATFORM_H

#include <jni.h>
#include <stddef.h>
#include "platform/log.h"
#include <string.h>

typedef struct native_shim_t {
    jobject instance;
    jclass type;
    JNIEnv *env;
} native_shim;

native_shim *get_native_shim();

// Grab native_shim and a JNIEnv separate from the native_shim to avoid a
// thread race condition accessing the embedded JNIEnv.  Note that in this
// case the native_shim 'env' member value is undefined.
native_shim *get_native_thread_shim(JNIEnv **env);

#define GET_STR(env, str, result) do { \
	if((str) == NULL) { result = NULL; } else { \
		const char* c_str = (env)->GetStringUTFChars((str), NULL); \
		if(c_str != NULL) { \
			(result) = strdup(c_str); \
		} else { \
			(result) = NULL; \
		} \
		(env)->ReleaseStringUTFChars((str), c_str); \
	} \
} while(0)

#define UTF8_BYTES_TO_STR(env, byte_array, result) do { \
	if((byte_array) == NULL) { result = NULL; } else { \
		int len = (env)->GetArrayLength(byte_array); \
		(result) = (char*) malloc(sizeof(char) * (len+1)); \
		(result)[len] = 0; \
		(env)->GetByteArrayRegion(byte_array, 0, len, (jbyte*) (result)); \
	} \
} while(0)


#ifdef __cplusplus
extern "C" {
#endif

void native_enter_thread();
void native_leave_thread();

#ifdef __cplusplus
}
#endif

#endif
