/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tealeaf;

import javax.microedition.khronos.opengles.GL10;

/**
 * A set of GL utilities inspired by the OpenGL Utility Toolkit.
 *
 */

public class GLU {

    /**
     * Return an error string from a GL or GLU error code.
     *
     * @param error - a GL or GLU error code.
     * @return the error string for the input error code, or NULL if the input
     *         was not a valid GL or GLU error code.
     */
    public static String gluErrorString(int error) {
        switch (error) {
        case GL10.GL_NO_ERROR:
            return "no error";
        case GL10.GL_INVALID_ENUM:
            return "invalid enum";
        case GL10.GL_INVALID_VALUE:
            return "invalid value";
        case GL10.GL_INVALID_OPERATION:
            return "invalid operation";
        case GL10.GL_STACK_OVERFLOW:
            return "stack overflow";
        case GL10.GL_STACK_UNDERFLOW:
            return "stack underflow";
        case GL10.GL_OUT_OF_MEMORY:
            return "out of memory";
        default:
            return null;
        }
    }
 }

