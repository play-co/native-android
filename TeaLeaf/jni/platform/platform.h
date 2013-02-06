/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 
 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 
 * You should have received a copy of the GNU General Public License
 * along with the Game Closure SDK.  If not, see <http://www.gnu.org/licenses/>.
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

#ifdef __cplusplus
extern "C" {
#endif

void native_enter_thread();
void native_leave_thread();

#ifdef __cplusplus
}
#endif

#endif
