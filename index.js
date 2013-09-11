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

var path = require("path");
var ff = require("ff");
var clc = require("cli-color");

exports.init = function(common) {
	console.log("Running install.sh");
	common.child("sh", ["install.sh"], {
		cwd: __dirname
	}, function () {
		console.log("Install complete");
	});

	exports.load(common);
};

exports.load = function(common) {
	var defaults = {
		"android:keystore": "",
		"android:key": "",
		"android:keypass": "",
		"android:storepass": ""
	};

	common.config.set("android:root", path.resolve(__dirname))

	// check to see the misc keys are at least present
	Object.keys(defaults).forEach(function (key) {
		if (!common.config.get(key)) {
			common.config.set(key, defaults[key]);
		}
	});

	require(common.paths.root('src', 'testapp')).registerTarget("native-android", __dirname);
}

exports.testapp = function(common, opts, next) {
	var cwd = process.cwd();

	var f = ff(this, function() {
		process.chdir(__dirname);
		common.child('make', [], {}, f.wait());
	}, function() {
		common.child('make', ['install'], {}, f.wait());
	}, function() {
		process.chdir(cwd);
		require(common.paths.root('src', 'serve')).cli();
	}).error(function(err) {
		process.chdir(cwd);
		console.log(clc.red("ERROR"), err);
	}).cb(function() {
		process.chdir(cwd);
		next();
	});
}

