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
#include "platform/resource_loader.h"
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "libzip/zip.h"
#include "libzip/zipint.h"
#include "core/platform/native.h"
#include "core/config.h"
#include "deps/cpu-features.h"
#include "libzip/zip.h"
#include "libzip/zipint.h"

extern "C" {
#include "core/texture_manager.h"
#include "core/image_loader.h"
#include "core/core.h"
}

#define FILESYSTEM_PREFIX "/"
#define APK_PREFIX "assets/resources/"

static zip * APKArchive=NULL;
static char *storage_dir = NULL;
static pthread_mutex_t m_mutex = PTHREAD_MUTEX_INITIALIZER;

CEXPORT void resource_loader_initialize(const char *path) {
    APKArchive = zip_open(path, 0, NULL);
    if (APKArchive == NULL) {
        LOG("{resources} ERROR: Unable to open APK %s", path);
        return;
    }
    storage_dir = get_storage_directory();
    image_cache_init(storage_dir, &image_cache_load_callback);
}
CEXPORT void resource_loader_deinitialize() {
    image_cache_destroy();
    zip_close(APKArchive);
}

resource_p resource_loader_load_url(const char *url) {
    // DANGER: This is called from a thread other than GLThread!
    JNIEnv *env = NULL;
    native_shim *shim = get_native_thread_shim(&env);
    jobject instance = shim->instance;
    jclass type = shim->type;
    resource *result = NULL;
    result = (resource*)malloc(sizeof(resource));
    jmethodID fetch_id = env->GetMethodID(type, "loadSourceFile", "(Ljava/lang/String;)Ljava/lang/String;");
    jstring s = env->NewStringUTF(url);
    jstring str = (jstring)env->CallObjectMethod(instance, fetch_id, s);
    env->DeleteLocalRef(s);
    if (str == NULL) {
        result->text = NULL;
        result->mapped = false;
    } else {
        char *contents = NULL;
        GET_STR(env, str, contents);
        env->DeleteLocalRef(str);
        int len;
        if (contents == NULL) {
            len = 0;
            contents = strdup("");
        } else {
            len = strlen(contents);
        }
        result->text = contents;
        result->size = len + 1;
        result->mapped = false;
    }
    return result;
}

CEXPORT char *resource_loader_string_from_url(const char *url) {
    // try loading from a file first
    char* contents = NULL;
    unsigned long dummy;
    contents = (char*)resource_loader_read_file(url, &dummy);
    if(contents == NULL) {
        // otherwise, pass it up to Java
        resource *res = resource_loader_load_url(url);
        if (res->text) {
            contents = strdup(res->text);
        }
        resource_loader_destroy_resource(res);
    }
    return contents;
}


CEXPORT void launch_remote_texture_load(const char *url) {
    // load from java
    JNIEnv *env = NULL;
    native_shim *shim = get_native_thread_shim(&env);

    size_t url_len = strlen(url);
    jbyteArray jbuff = env->NewByteArray(url_len);
    env->SetByteArrayRegion(jbuff, 0, url_len, (jbyte*) url);
    jmethodID load_texture_id = env->GetMethodID(shim->type, "loadTexture", "([B)V");
    env->CallVoidMethod(shim->instance, load_texture_id, jbuff);

    env->DeleteLocalRef(jbuff);
}

CEXPORT bool resource_loader_load_image_with_c(texture_2d * texture) {
    texture->pixel_data = NULL;

    // check if it is a special url (text, contacts, etc.), if it is then load in java
    bool skip = texture->url[0] == '@' || texture->is_canvas || texture->is_text;
    if (!skip) {
        unsigned long sz;
        unsigned char *data = resource_loader_read_file(texture->url, &sz);
        texture->pixel_data = texture_2d_load_texture_raw(texture->url, data, sz, &texture->num_channels, &texture->width, &texture->height, &texture->originalWidth, &texture->originalHeight, &texture->scale, &texture->used_texture_bytes, &texture->compression_type);
        free(data);
    }

    // if we have pixel data...
    if (texture->pixel_data != NULL) {
        // load using C
        return true;
    } else {
        launch_remote_texture_load(texture->url);
        // load using java
        return false;
    }
}

CEXPORT void resource_loader_destroy_resource(resource_p res) {
    if (res->text) {
        if (res->mapped) {
            munmap(res->text, res->size);
        } else {
            free(res->text);
        }
    }
    free(res);
}




CEXPORT unsigned char *resource_loader_read_file(const char * url, unsigned long * sz) {
    unsigned char *data = NULL;
    /* check if the image resides on the file system first */
    if (!url || strlen(url) == 0) {
        return NULL;
    }
    char *base_path = storage_dir;
    size_t len = strlen(base_path) + strlen(FILESYSTEM_PREFIX) + strlen(url) + 1;
    char *path = (char*)malloc(len);
    snprintf(path, len, "%s%s%s", base_path, FILESYSTEM_PREFIX, url);

    struct stat statBuf;
    int result = stat(path, &statBuf);
    // try the file system first
    bool on_file_sys = (result != -1);
    if(on_file_sys) {
        FILE *file_from_sys = fopen(path, "r");

        if(!file_from_sys) {
            on_file_sys = false;
        } else {
            //adding a byte to null terminate the byte stream
            *sz = statBuf.st_size + 1;
            data = (unsigned char*)malloc(*sz);
            memset(data, 0, *sz);
            fread(data, sizeof(unsigned char), *sz, file_from_sys);
            fclose(file_from_sys);
        }
    }

    // if not on the filesystem (or opening it from the fs failed), unzip and load
    if(!on_file_sys) {
        int flen = strlen(url) + strlen(APK_PREFIX) + 1;
        char *filename = (char*)malloc(flen);
        strcpy(filename, APK_PREFIX);
        strcat(filename, url);

        pthread_mutex_lock(&m_mutex);

        zip_file *file = zip_fopen(APKArchive, filename, 0);
        if (!file) {
            pthread_mutex_unlock(&m_mutex);
            LOG("{resources} WARNING: Unable to open %s from APK", filename);
            free(filename);
            return 0;
        }
        //stat file for size
        struct zip_stat stats;
        zip_stat(APKArchive, filename, 4, &stats);
        // + 1 for the null termination
        *sz = file->bytes_left;
        //read file to buffer
        data = (unsigned char*)malloc(*sz + 1);
        memset(data, 0, *sz);
        // only read up to size - 1 so as not to read the null termination
        zip_fread(file, data, *sz);

        zip_fclose(file);
        pthread_mutex_unlock(&m_mutex);

        free(filename);
    }

    free(path);
    return data;
}
