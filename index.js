var path = require("path");
var fs = require("fs");
var ff = require("ff");
var clc = require("cli-color");
var common = require("../../src/common");
var root = common.paths.root; //save the root path function

function registerTestApp() {
	require("../../src/testapp").registerTarget("native-android", __dirname);
}

//called when addon is activated
exports.init = function () {
	console.log("Running install.sh");
	common.child("sh", ["install.sh"], {
		cwd: __dirname
	}, function () {
		console.log("Install complete");
	});

	exports.load();
};

exports.load = function () {
	common.config.set("android:root", path.resolve(__dirname))

	//check to see the misc keys are at least present
	if (!common.config.get("android:keystore")) 
		common.config.set("android:keystore", "");

	if (!common.config.get("android:key")) 
		common.config.set("android:key", "");

	if (!common.config.get("android:keypass")) 
		common.config.set("android:keypass", "");

	if (!common.config.get("android:storepass")) 
		common.config.set("android:storepass", "");
	
	common.config.write();

	registerTestApp();
}

exports.testapp = function (opts, next) {
	var cwd = process.cwd();

	var f = ff(this, function () {
		process.chdir(__dirname);
		common.child('make', [], {}, f.wait());
	}, function() {
		common.child('make', ['install'], {}, f.wait());
	}, function() {
		process.chdir(cwd);
		require("../../src/serve").cli();
	}).error(function(err) {
		process.chdir(cwd);
		console.log(clc.red("ERROR"), err);
	}).cb(function() {
		process.chdir(cwd);
		next();
	});
}

