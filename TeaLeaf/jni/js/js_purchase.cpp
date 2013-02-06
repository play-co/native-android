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
#include "js/js_purchase.h"

#include "platform/purchase.h"
#include <stdlib.h>

using namespace v8;

Handle<Value> js_purchase_buy(const Arguments& args) {
	LOGFN("purchase buy");
	String::Utf8Value product(args[0]);
	const char* product_id = ToCString(product);
	purchase_buy(product_id);
	LOGFN("end purchase buy");
	return Undefined();
}

Handle<Value> js_purchase_restore(const Arguments& args) {
	LOGFN("purchase restore");
	purchase_restore();
	LOGFN("end purchase restore");
	return Undefined();
}

Handle<Value> js_purchase_on_result_default(const Arguments& args) {
	LOGFN("purchase on result default");
	LOGFN("end purchase on result default");
	return Boolean::New(false);
}

Handle<Value> js_purchase_get_supported(Local<String> property, const AccessorInfo &info) {
	LOGFN("purchase get supported");
	bool available = purchase_available();
	LOGFN("end purchase get supported");
	return Boolean::New(available);
}

Handle<Value> js_purchase_confirm_purchase(const Arguments &args) {
	LOGFN("purchase confirm purchase");
	String::Utf8Value product(args[0]);
	const char *product_id = ToCString(product);
	purchase_confirm(product_id);
	LOGFN("end purchase confirm purchase");
	return Undefined();
}

Handle<ObjectTemplate> js_purchase_get_template() {
	Handle<ObjectTemplate> purchase = ObjectTemplate::New();
	purchase->Set(String::New("onResult"), FunctionTemplate::New(js_purchase_on_result_default));
	purchase->Set(String::New("buy"), FunctionTemplate::New(js_purchase_buy));
	purchase->Set(String::New("restore"), FunctionTemplate::New(js_purchase_restore));
	purchase->Set(String::New("confirmPurchase"), FunctionTemplate::New(js_purchase_confirm_purchase));
	purchase->SetAccessor(String::New("supported"), js_purchase_get_supported);
	return purchase;
}
