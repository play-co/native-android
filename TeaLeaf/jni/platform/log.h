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
#ifndef LOG_H
#define LOG_H

#if defined(ANDROID)

#include <android/log.h>
#define LOG_TAG "JS"
#define DEBUG_TAG "JSDEBUG"

#ifndef RELEASE
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGDEBUG(...) __android_log_print(ANDROID_LOG_INFO,DEBUG_TAG,__VA_ARGS__)
#else
#define LOG(...)
#define LOGDEBUG(...)
#endif


#define LOG_FUNCTION_CALLS 0
#if LOG_FUNCTION_CALLS
#define LOGFN LOG
#else
#define LOGFN(...)
#endif

void nop_log(const char *f, ...);

#elif __amd64__

#include <stdio.h>
#define LOG(...) printf(__VA_ARGS__)
#define LOGDEBUG(...) printf(__VA_ARGS__)

#endif // ARCH
#endif // LOG_H
