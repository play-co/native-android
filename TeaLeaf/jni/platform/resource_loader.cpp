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
#include "core/image_loader.h"
#include "core/core.h"
}

#define FILESYSTEM_PREFIX "/"
#define APK_PREFIX "assets/resources/"

static zip * APKArchive=NULL;
static char *storage_dir = NULL;

CEXPORT void resource_loader_initialize(const char *path) {
	APKArchive = zip_open(path, 0, NULL);
	if (APKArchive == NULL) {
		LOG("{resources} ERROR: Unable to open APK %s", path);
		return;
	}
	storage_dir = get_storage_directory();
}
CEXPORT void resource_loader_deinitialize() {
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

	jstring s = env->NewStringUTF(url);

	jmethodID load_texture_id = env->GetMethodID(shim->type, "loadTexture", "(Ljava/lang/String;)V");
	env->CallVoidMethod(shim->instance, load_texture_id, s);

	env->DeleteLocalRef(s);
}

CEXPORT bool resource_loader_load_image_with_c(texture_2d * texture) {
	texture->pixel_data=NULL;

	bool skip = false;
	// check if it is a special url (text, contacts, etc.), if it is then load in java
	if(texture->url[0] == '@') {
		skip = true;
	}

	if(!skip) {
		unsigned long sz;
		unsigned char *data = resource_loader_read_file(texture->url, &sz);

		texture->pixel_data = texture_2d_load_texture_raw(texture->url, data, sz, &texture->num_channels, &texture->width, &texture->height, &texture->originalWidth, &texture->originalHeight, &texture->scale);

		free(data);
	}

	// if we have pixel data...
	if (texture->pixel_data != NULL) {
		// load using C
		return true;
	} else {
		launch_remote_texture_load(texture->url);

		// using java
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
		zip_file *file = zip_fopen(APKArchive, filename, 0);
		if (!file) {
			LOG("{resources} WARNING: Unable to open %s from APK", filename);
			free(filename);
			return 0;
		}
		//stat file for size
		struct zip_stat stats;
		zip_stat(APKArchive, filename, 4, &stats);
		// + 1 for the null termination
		*sz = file->bytes_left + 1;
		//read file to buffer
		data = (unsigned char*)malloc(*sz);
		memset(data, 0, *sz);
		// only read up to size - 1 so as not to read the null termination
		zip_fread(file, data, *sz - 1);

		zip_fclose(file);
		free(filename);
	}

	free(path);
	return data;
}

unsigned char *load_texture(texture_2d * texture) { //take in a texture_2d pointer instead
	unsigned long sz = 0;
	unsigned char *data = resource_loader_read_file(texture->url, &sz);
	//if we don't get data back from this, we need to load from java
	if (!data) {
		return NULL;
	}
	int sw = 0, sh = 0, ch = 0;
	unsigned char *bits = load_image_from_memory(data, (long)sz, &sw, &sh, &ch);

	//check for image being a power of 2, if not, place it into an image
	//which is power of 2, in the upper left corner
	int w_old = sw;
	int h_old = sh;
	int w = w_old;
	int h = h_old;

	unsigned char *copy_image = NULL;

	// bit tricks for checking if w/h is a power of 2
	if( !(w && !(w & (w - 1))) || !(h && !(h & (h - 1))) ) {
		w--;
		w |= w >> 1;
		w |= w >> 2;
		w |= w >> 4;
		w |= w >> 8;
		w |= w >> 16;
		w++;

		h--;
		h |= h >> 1;
		h |= h >> 2;
		h |= h >> 4;
		h |= h >> 8;
		h |= h >> 16;
		h++;
	}


	//values to help with modifying image coordinates if halfsizing gets used
	int scale = 1;
	int ratio = 1;
	int div_mod = 1;

	if (use_halfsized_textures && (h > 64 || w > 64)) {
		scale = 2;
		ratio = 4;
		div_mod = ch;
	}

	int row_bytes = ch * sizeof(unsigned char) * w_old;
	int old_image_size = row_bytes * h_old;
	int new_row_bytes = ch * sizeof(unsigned char) * w / scale;
	int new_image_size = new_row_bytes * h / scale;

	if (new_image_size == 0) {
		LOG("{resources} WARNING: Trying to load 0 sized image");
		return NULL;
	}

	//if old_image_size == new_image_size, we only need to premultiply the alpha channel
	if (old_image_size == new_image_size) {
		if (ch == 4) {
			// 4 channels
			for(int i = 0; i < h; i++) {
				for(int j = 0; j < row_bytes; j += ch) {
					unsigned char a = bits[row_bytes * i + j + 3];
					bits[row_bytes * i + j] = ((bits[row_bytes * i + j] * a + 128 ) >> 8);
					bits[row_bytes * i + j + 1] = ((bits[row_bytes * i + j + 1] * a + 128) >> 8);
					bits[row_bytes * i + j + 2] = ((bits[row_bytes * i + j + 2] * a + 128) >> 8);
				}
			}
		}
		copy_image = bits;

	} else {
		copy_image = (unsigned char *) malloc(new_image_size);

		//set copy_image to all zeroes so we can add in for nearest neighbor
		memset(copy_image, 0, new_image_size);

		// ix - will be the "new" i pos, if using halfsized, i = i / 2, otherwise i is unchanged

		// jx - will be the "new" j pos, it will get adjusted to the proper horizontal position
		// in the image and is dependant on if the image is halfszied and how many channels it has
		if (ch == 4) {
			// 4 channels
			for(int i = 0; i < h; i++) {
				int ix = i / scale;
				if(i < h_old) {
					for(int j = 0; j < row_bytes; j += ch) {
						int jx = (j / div_mod) / scale * div_mod;
						copy_image[new_row_bytes * ix + jx] += ((bits[row_bytes * i + j] * bits[row_bytes * i + j + 3] + 128 ) >> 8) / ratio;
						copy_image[new_row_bytes * ix + jx + 1] += ((bits[row_bytes * i + j + 1] * bits[row_bytes * i + j + 3] + 128) >> 8) / ratio;
						copy_image[new_row_bytes * ix + jx + 2] += ((bits[row_bytes * i + j + 2] * bits[row_bytes * i + j + 3] + 128) >> 8) / ratio;
						copy_image[new_row_bytes * ix + jx + 3] += (bits[row_bytes * i + j + 3]) / ratio;


					}

				}
			}

		} else {
			// 3 channels
			for(int i = 0; i < h; i++) {
				int ix = i / scale;
				if(i < h_old) {
					for(int j = 0; j < row_bytes; j += ch) {
						int jx = (j / div_mod) / scale * div_mod;
						copy_image[new_row_bytes * ix + jx] += (bits[row_bytes * i + j]) / ratio;
						copy_image[new_row_bytes * ix + jx + 1] += (bits[row_bytes * i + j + 1]) / ratio;
						copy_image[new_row_bytes * ix + jx + 2] += (bits[row_bytes * i + j + 2]) / ratio;


					}
				}

			}


		}

	}

	//clean up
	free(data);
	if (copy_image != bits) {
		free(bits);
	}

	//set texture params
	texture->num_channels = ch;
	texture->scale = scale;
	texture->width= w;
	texture->height = h;
	texture->originalWidth = w_old;
	texture->originalHeight = h_old;

	//return the correct bytes
	return copy_image;
}

