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
#include <stdlib.h>

extern "C" {
#include "core/config.h"
#include "core/url_loader.h"
#include "core/core.h"
}

#include "js/js_native.h"
#include "js/js_console.h"
#include "js/js_haptics.h"
#include "js/js_purchase.h"
#include "js/js_sound.h"
#include "js/js_overlay.h"
#include "js/js_device.h"
#include "js/js_dialog.h"
#include "js/js_textbox.h"
#include "js/js_photo.h"
#include "js/js_context.h"
#include "js/js_local_storage.h"
#include "js/js_socket.h"
#include "js/js_xhr.h"
#include "js/js_device.h"
#include "js/js_timestep_view.h"
#include "js/js_plugins.h"
#include "js/js_gc.h"
#include "js/js_build.h"
#include "js/js_locale.h"
#include "js/js_profiler.h"
#include "js/js_input_prompt.h"
#include "js/js_location.h"

#include "platform/textbox.h"
#include "platform/native.h"

using namespace v8;


Handle<Value> native_eval(const Arguments& args) {
	LOGFN("eval");
	String::Utf8Value str(args[0]);
	const char *cstr = ToCString(str);
	String::Utf8Value str2(args[1]);
	const char *pstr = ToCString(str2);

	Handle<Value> ret = ExecuteString(String::New(cstr), String::New(pstr), true);
	LOGFN("endeval");
	return ret;
}

Handle<Value> native_fetch(const Arguments& args) {
	LOGFN("fetch");
	String::Utf8Value url(args[0]);
	const char *url_str = (char*) ToCString(url);
	char *contents = core_load_url(url_str);
	LOGFN("endfetch");
	if (contents) {
		Handle<String> jscontents = String::New(contents);
		free(contents);
		return jscontents;
	} else {
		return Boolean::New(false);
	}
}

Handle<Value> native_start_game(const Arguments& args) {
	LOGFN("startGame");

	String::Utf8Value app(args[0]);
	const char* appid = ToCString(app);
	start_game(appid);

	LOGFN("end startGame");
	return Undefined();
}

Handle<Value> native_apply_update(const Arguments& args) {
	LOGFN("applyUpdate");
	apply_update();
	LOGFN("end applyUpdate");
	return Undefined();
}

Handle<Value> native_done_loading(const Arguments& args) {
	core_hide_preloader();

	LOG("{js} Game is done loading");
	return Undefined();
}

Handle<Value> js_native_send_activity_to_back(const Arguments& args) {
	LOGFN("send_activity_to_back");
	bool result = native_send_activity_to_back();
	LOGFN("end send_activity_to_back");
	return Boolean::New(result);
}

Handle<Value> js_native_upload_device_info(const Arguments& args) {
	LOGFN("upload_device_info");
	upload_device_info();
	LOGFN("end upload_device_info");
	return Undefined();
}

Handle<Value> js_native_reload(const Arguments& args) {
	LOGFN("reload");
	native_reload();
	LOGFN("reload");
	return Undefined();
}

Handle<Value> js_native_get_microseconds(const Arguments& args) {
	struct timeval tm;
	gettimeofday(&tm, NULL);
	return Number::New((unsigned)(tm.tv_sec * 1000000 + tm.tv_usec));
}

// Cached install referrer
// NOTE: This will leave some memory allocated during shutdown, which should
// not cause problems but may show up in an automated leak tester.
// NOTE: Not thread-safe but it doesn't need to be afaik. -cat
static const char *m_cached_install_referrer = 0;

Handle<Value> js_install_referrer(Local<String> property,
		              const AccessorInfo& info) {
	LOGFN("install_referrer");
	Handle<Value> result;
	if (m_cached_install_referrer)
		result = String::New(m_cached_install_referrer);
	else
		result = String::New(m_cached_install_referrer = get_install_referrer());
	LOGFN("end install_referrer");
	return result;
}

Handle<Value> js_used_heap(Local<String> property,
		              const AccessorInfo& info) {
	LOGFN("used_heap");

	HandleScope handle_scope;
	HeapStatistics stats;

	V8::GetHeapStatistics(&stats);

	int used_heap = (int)stats.used_heap_size();

	LOGFN("end used_heap");
	return handle_scope.Close(Number::New(used_heap));
}

Handle<ObjectTemplate> js_native_get_template(const char* uri, const char* native_hash) {
	Handle<ObjectTemplate> NATIVE = ObjectTemplate::New();

	// functions
	NATIVE->Set(String::New("getFileSync"), FunctionTemplate::New(native_fetch));
	NATIVE->Set(String::New("eval"), FunctionTemplate::New(native_eval));
	NATIVE->Set(String::New("startGame"), FunctionTemplate::New(native_start_game));
	NATIVE->Set(String::New("doneLoading"), FunctionTemplate::New(native_done_loading));
	NATIVE->Set(String::New("applyUpdate"), FunctionTemplate::New(native_apply_update));
	NATIVE->Set(String::New("Socket"), FunctionTemplate::New(js_socket_ctor));
	NATIVE->Set(String::New("sendActivityToBack"), FunctionTemplate::New(js_native_send_activity_to_back));
	NATIVE->Set(String::New("uploadDeviceInfo"), FunctionTemplate::New(js_native_upload_device_info));
	NATIVE->Set(String::New("getCurrentTimeMicroseconds"), FunctionTemplate::New(js_native_get_microseconds));
	NATIVE->Set(String::New("reload"), FunctionTemplate::New(js_native_reload));

	// templates
	NATIVE->Set(String::New("console"), js_console_get_template());
	NATIVE->Set(String::New("gl"), js_gl_get_template()->NewInstance());
	NATIVE->Set(String::New("localStorage"), js_local_storage_get_template()->NewInstance());
	NATIVE->Set(String::New("sound"), js_sound_get_template()->NewInstance());
	NATIVE->Set(String::New("overlay"), js_overlay_get_template()->NewInstance());
	NATIVE->Set(String::New("purchase"), js_purchase_get_template()->NewInstance());
	NATIVE->Set(String::New("device"), js_device_get_template()->NewInstance());
	NATIVE->Set(String::New("textbox"), js_textbox_get_template()->NewInstance());
	NATIVE->Set(String::New("dialogs"), js_dialog_get_template()->NewInstance());
	NATIVE->Set(String::New("haptics"), js_haptics_get_template()->NewInstance());
	NATIVE->Set(String::New("camera"), js_camera_get_template()->NewInstance());
	NATIVE->Set(String::New("gallery"), js_gallery_get_template()->NewInstance());
	NATIVE->Set(String::New("timestep"), js_timestep_get_template());
	NATIVE->Set(String::New("xhr"), js_xhr_get_template()->NewInstance());
	NATIVE->Set(String::New("plugins"),js_plugins_get_template()->NewInstance());
	NATIVE->Set(String::New("gc"), js_gc_get_template()->NewInstance());
	NATIVE->Set(String::New("build"), js_build_get_template()->NewInstance());
	NATIVE->Set(String::New("locale"), js_locale_get_template()->NewInstance());
	NATIVE->Set(String::New("profiler"), js_profiler_get_template()->NewInstance());
	NATIVE->Set(String::New("inputPrompt"), js_input_prompt_get_template()->NewInstance());

	// market
	Handle<Object> market = Object::New();
	const char *marketUrl = get_market_url();
	market->Set(String::New("url"), String::New(marketUrl), ReadOnly);
	free((void*)marketUrl);
	NATIVE->Set(String::New("market"), market);

	// Values
	NATIVE->SetAccessor(String::New("deviceUUID"), js_device_global_id);
	NATIVE->SetAccessor(String::New("installReferrer"), js_install_referrer);
	NATIVE->SetAccessor(String::New("usedHeap"), js_used_heap);
	NATIVE->Set(String::New("simulateID"), String::New(config_get_simulate_id()));
	NATIVE->Set(String::New("screen"), Object::New());
	NATIVE->Set(String::New("uri"), String::New(uri));
	NATIVE->Set(String::New("tcpHost"), String::New(config_get_tcp_host()));
	NATIVE->Set(String::New("tcpPort"), Number::New(config_get_tcp_port()));
	const char *versionCode = get_version_code(); // versionCode
	NATIVE->Set(String::New("versionCode"), String::New(versionCode), ReadOnly);
	free((void*)versionCode);
	NATIVE->Set(String::New("nativeHash"), String::New(native_hash));
	NATIVE->SetAccessor(String::New("location"), jsGetLocation, jsSetLocation);

	return NATIVE;
}
