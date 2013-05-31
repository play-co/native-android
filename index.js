var path = require("path");
var fs = require("fs");
var ff = require("ff");
var clc = require("cli-color");
var wrench = require('wrench');
var util = require('util');
var request = require('request');
var crypto = require('crypto');
var spawn = require('child_process').spawn;
var read = require('read');

var logger;


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

	require(common.paths.root('src', 'testapp')).registerTarget("native-android", __dirname);
}

exports.testapp = function(opts, next) {
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


//// Addons

var installAddons = function(builder, project, opts, addonConfig, next) {
	var paths = builder.common.paths;
	var addons = project && project.manifest && project.manifest.addons;

	var f = ff(this, function() {
		// For each addon,
		if (addons) {
			for (var ii = 0; ii < addons.length; ++ii) {
				var addon = addons[ii];

				// Prefer paths in this order:
				var addon_js_android = paths.addons(addon, 'js', 'android');
				var addon_js_native = paths.addons(addon, 'js', 'native');
				var addon_js = paths.addons(addon, 'js');

				if (fs.existsSync(addon_js_android)) {
					logger.log("Installing addon:", addon, "-- Adding ./js/android to jsio path");
					require(paths.root('src', 'AddonManager')).registerPath(addon_js_android);
				} else if (fs.existsSync(addon_js_native)) {
					logger.log("Installing addon:", addon, "-- Adding ./js/native to jsio path");
					require(paths.root('src', 'AddonManager')).registerPath(addon_js_native);
				} else if (fs.existsSync(addon_js)) {
					logger.log("Installing addon:", addon, "-- Adding ./js to jsio path");
					require(paths.root('src', 'AddonManager')).registerPath(addon_js);
				} else {
					logger.warn("Installing addon:", addon, "-- No js directory so no JavaScript will be installed");
				}
			}
		}
	}, function() {
		var group = f.group();

		// For each addon,
		if (addons) {
			for (var ii = 0; ii < addons.length; ++ii) {
				var addon = addons[ii];
				var addonConfig = paths.addons(addon, 'android', 'config.json');

				if (fs.existsSync(addonConfig)) {
					fs.readFile(addonConfig, 'utf8', group.slot());
				} else {
					logger.warn("Unable to find Android addon config file", addonConfig);
				}
			}
		}
	}, function(results) {
		if (results) {
			for (var ii = 0; ii < results.length; ++ii) {
				var addon = addons[ii];
				addonConfig[addon] = JSON.parse(results[ii]);

				logger.log("Configured addon:", addon);
			}
		}
	}).error(function(err) {
		logger.error("Failure to install addons:", err);
	}).cb(next);
}

var getTextBetween = function(text, startToken, endToken) {
	var start = text.indexOf(startToken);
	var end = text.indexOf(endToken);
	if (start == -1 || end == -1) {
		return "";
	}
	var offset = text.substring(start).indexOf("\n") + 1;
	var afterStart = start + offset;
	return text.substring(afterStart, end);
	
}

var replaceTextBetween = function(text, startToken, endToken, replaceText) {
	var newText = "";
	var start = text.indexOf(startToken);
	var end = text.indexOf(endToken);
	if (start == -1 || end == -1) {
		return text;
	}
	var offset = text.substring(start).indexOf("\n") + 1;
	var afterStart = start + offset;
	newText += text.substring(0, afterStart);
	newText += replaceText;
	newText += text.substring(end);
	return newText;
}

function injectPluginXML(builder, opts, next) {
	var addonConfig = opts.addonConfig;
	var destDir = opts.destDir;
	var manifestXml = path.join(destDir, "AndroidManifest.xml");

	var f = ff(function() {
		var group = f.group();

		for (var key in addonConfig) {
			var addonAndroidDir = builder.common.paths.addons(key, "android");

			fs.readFile(path.join(addonAndroidDir, addonConfig[key].injectionXML), "utf-8", group());
		}

		fs.readFile(manifestXml, "utf-8", f());
	}, function(results, xml) {
		if (results && results.length > 0 && xml && xml.length > 0) {
			var XML_START_PLUGINS_MANIFEST = "<!--START_PLUGINS_MANIFEST-->";
			var XML_END_PLUGINS_MANIFEST = "<!--END_PLUGINS_MANIFEST-->";
			var XML_START_PLUGINS_APPLICATION = "<!--START_PLUGINS_APPLICATION-->";
			var XML_END_PLUGINS_APPLICATION = "<!--END_PLUGINS_APPLICATION-->";

			var manifestXmlManifestStr = "";
			var manifestXmlApplicationStr = "";

			for (var i = 0; i < results.length; ++i) {
				var pluginXml = results[i];

				manifestXmlManifestStr += getTextBetween(pluginXml, XML_START_PLUGINS_MANIFEST,	XML_END_PLUGINS_MANIFEST);
				manifestXmlApplicationStr += getTextBetween(pluginXml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION);
			}

			xml = replaceTextBetween(xml, XML_START_PLUGINS_MANIFEST, XML_END_PLUGINS_MANIFEST, manifestXmlManifestStr);
			xml = replaceTextBetween(xml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION, manifestXmlApplicationStr);
			fs.writeFile(manifestXml, xml, "utf-8", f.wait());
		} else {
			logger.log("No plugin XML to inject");
		}
	}).success(next).error(function(err) {
		logger.error("Inject plugin XML failure:", err);
		process.exit(1);
	});
}

var installAddonCode = function(builder, opts, next) {
	var addonConfig = opts.addonConfig;
	var destDir = opts.destDir;

	var filePaths = [];
	var libraries = [];
	var jars = [];
	var jarPaths = [];

	var f = ff(function() {
		var group = f.group();

		for (var addon in addonConfig) {
			var config = addonConfig[addon];

			if (config.copyFiles) {
				for (var ii = 0; ii < config.copyFiles.length; ++ii) {
					var filePath = builder.common.paths.addons(addon, 'android', config.copyFiles[ii]);

					logger.log("Installing addon Java code:", filePath);

					filePaths.push(filePath);

					fs.readFile(filePath, "utf-8", group.slot());
				}
			}

			if (config.libraries) {
				for (var ii = 0; ii < config.libraries.length; ++ii) {
					var library = builder.common.paths.addons(addon, 'android', config.libraries[ii]);

					logger.log("Installing addon library:", library);

					libraries.push(library);
				}
			}

			if (config.jars) {
				for (var ii = 0; ii < config.jars.length; ++ii) {
					var jar = builder.common.paths.addons(addon, 'android', config.jars[ii]);

					logger.log("Installing addon JAR:", jar);

					jars.push(jar);
				}
			}
		}

		fs.readFile(path.join(destDir, "project.properties"), "utf-8", f());

		var jarGroup = f.group();

		for (var ii = 0; ii < jars.length; ++ii) {
			var jar = jars[ii];

			fs.readFile(jar, "binary", jarGroup.slot());

			jarPaths.push(jar);
		}
	}, function(results, properties, jarResults) {
		if (results && results.length > 0) {
			for (var ii = 0; ii < results.length; ++ii) {
				var data = results[ii];
				var filePath = filePaths[ii];

				if (data) {
					var pkgName = data.match(/(package[\s]+)([a-z.A-Z0-9]+)/g)[0].split(' ')[1];
					var pkgDir = pkgName.replace(/\./g, "/");
					var outFile = path.join(destDir, "src", pkgDir, path.basename(filePath));

					logger.log("Installing Java package", pkgName, "to", outFile);

					wrench.mkdirSyncRecursive(path.dirname(outFile));

					fs.writeFile(outFile, data, 'utf-8', f.wait());
				} else {
					logger.warn("Unable to read Java package", filePath);
				}
			}
		} else {
			logger.log("No Java packages to add");
		}

		if (properties && properties.length > 0) {
			//find largest uncommented library reference number
			var i = 0;
			var refStr = "android.library.reference.";
			var refNum = 1;
			for (;;) {
				var offset = properties.substring(i).indexOf("android.library.reference.");
				i = offset + i;
				if (offset == -1) {
					break;
				}
				if (properties[i - 1] == "#") {
					i += refStr.length;
					continue;
				}
				i += refStr.length;
				refNum++;
			}

			var libStr = "";
			for (var i = 0; i < libraries.length; i ++)	{
				var libProp = refStr + refNum + "=" + path.relative(destDir, libraries[i]);

				logger.log("Installing library property:", libProp);

				libStr += libProp + "\n";
				refNum++;
			}

			properties += libStr;

			fs.writeFile(path.join(destDir, "project.properties"), properties, "utf-8", f.wait());
		} else {
			logger.log("No library properties to add");
		}

		if (jarResults && jarResults.length > 0) {
			for (var ii = 0; ii < jarResults.length; ++ii) {
				var data = jarResults[ii];

				if (data) {
					var jarPath = jarPaths[ii];
					var jarDestPath = path.join(destDir, "libs", path.basename(jarPath));

					logger.log("Installing JAR file:", jarDestPAth);

					fs.writeFile(jarDestPath, data, "binary", f.wait());
				}
			}
		} else {
			logger.log("No JAR file data to install");
		}
	}).success(next).error(function(err) {
		logger.error("Error while installing addon code:", err);
		process.exit(1);
	});
}


//// Utilities

function nextStep() {
	var func = arguments[arguments.length - 1];
	return func(null);
}

function transformXSL(builder, inFile, outFile, xslFile, params, next) {
	var outFileTemp = outFile + ".temp";

	var f = ff(function() {
		for (var key in params) {
			if (typeof params[key] != 'string') {
				if (params[key] == undefined || typeof params[key] == 'object') {
					logger.error("settings for AndroidManifest: value for", clc.yellow.bright(key), "is not a string");
				}

				params[key] = JSON.stringify(params[key]);
			}
		}

		builder.jvmtools.exec('xslt', [
			"--in", inFile,
			"--out", outFileTemp,
			"--stylesheet", xslFile,
			"--params", JSON.stringify(params)
			], f.slotPlain());
	}, function(xslt) {
		var formatter = new builder.common.Formatter('xslt');

		xslt.on('out', formatter.out);
		xslt.on('err', formatter.err);
		xslt.on('end', f.slotPlain());
	}, function(data) {
		fs.readFile(outFileTemp, 'utf-8', f());
	}, function(dat) {
		dat = dat.replace(/android:label=\"[^\"]*\"/g, "android:label=\""+params.title+"\"");

		fs.writeFile(outFile, dat, 'utf-8', f.wait());
	}).success(next).error(function(err) {
		logger.error("Transform XSL failure:", err);
		process.exit(1);
	});
}

var PUNCTUATION_REGEX = /[!"#$%&'()*+,\-.\/:;<=>?@\[\\\]^_`{|}~]/g;
var PUNCTUATION_OR_SPACE_REGEX = /[!"#$%&'()*+,\-.\/:;<=>?@\[\\\]^_`{|}~ ]/g;

function validateSubmodules(next) {
	var submodules = [
		"native-core/core.h",
		"barista/src/engine.js"
	];

	var f = ff(function() {
		var group = f.group();

		for (var i = 0; i < submodules.length; ++i) {
			fs.exists(path.join(__dirname, submodules[i]), group.slotPlain());
		}
	}, function(results) {
		var allGood = results.every(function(element, index) {
			if (!element) {
				logger.error("Submodule " + path.dirname(submodules[index]) + " not found");
			}
			return element;
		});

		if (!allGood) {
			f.fail("One of the submodules was not found.  Make sure you have run submodule update --init on your clone of the Android repo");
		}
	}).success(next).error(function(err) {
		logger.error("Validate submodule failure:", err);
		process.exit(1);
	});
}

function buildSupportProjects(builder, project, destDir, debug, clean, next) {
	var tealeafDir;
	
	var f = ff(this, function() {
		tealeafDir = path.join(__dirname, "TeaLeaf");
		if (clean) {
			builder.common.child('make', ['clean'], {cwd: __dirname}, f.slot());
		}
	}, function() {
		builder.common.child('ndk-build', ["-j", "8", (debug ? "DEBUG=1" : "RELEASE=1")], { cwd: tealeafDir }, f.wait()); 
	}, function() {
		builder.common.child('ant', [(debug ? "debug" : "release")], { cwd: tealeafDir }, f.wait());
	}).failure(function(e) {
		logger.error("Could not build support projects:", e);
		process.exit(2);
	}).success(next);
}

function buildAndroidProject(builder, destDir, debug, next) {
	builder.common.child('ant', [(debug ? "debug" : "release")], {
		cwd: destDir
	}, next);
}

function makeAndroidProject(builder, project, namespace, activity, title, appID,
		shortName, version, debug,
		destDir, servicesURL, metadata, studioName, addonConfig, next)
{
	var target = "android-15";
	var f = ff(function() {
		builder.common.child('android', [
			"create", "project", "--target", target, "--name", shortName,
			"--path", destDir, "--activity", activity,
			"--package", namespace
		], {}, f());
	}, function() {
		builder.common.child('android', [
			"update", "project", "--target", target,
			"--path", destDir,
			"--library", "../../TeaLeaf"
		], {}, f());
	}, function() {
		fs.appendFile(path.join(destDir, 'project.properties'), 'out.dexed.absolute.dir=../.dex/\nsource.dir=src\n',f());
	}, function() {
		updateManifest(builder, project, namespace, activity, title, appID, shortName, version, debug, destDir, servicesURL, metadata, studioName, addonConfig, f.waitPlain());
		updateActivity(project, namespace, activity, destDir, f.waitPlain());
	}).error(function(code) {
		logger.error("Build failed creating android project:", code);
		process.exit(2);
	}).success(next);
}

function signAPK(builder, shortName, destDir, next) {
	logger.log('Signing APK at', path.join(destDir, "bin"));

	var keystore = builder.common.config.get('android.keystore');
	var storepass = builder.common.config.get('android.storepass');
	var keypass = builder.common.config.get('android.keypass');
	var key = builder.common.config.get('android.key');

	builder.common.child('jarsigner', [
		"-sigalg", "MD5withRSA", "-digestalg", "SHA1",
		"-keystore", keystore, "-storepass", storepass, "-keypass", keypass,
		"-signedjar", shortName + "-unaligned.apk",
		shortName + "-release-unsigned.apk", key
	], {
		cwd: path.join(destDir, "bin")
	}, function (err) {
		builder.common.child('zipalign', [
			"-f", "-v", "4", shortName + "-unaligned.apk", shortName + "-aligned.apk"
		], {
			cwd: path.join(destDir, 'bin')
		}, function () {
			next();
		});
	});
}

function copyFonts(builder, project, destDir) {
	var fontDir = path.join(destDir, 'assets/fonts');
	wrench.mkdirSyncRecursive(fontDir);

	var ttf = project.ttf;

	if (!ttf) {
		logger.warn("No \"ttf\" section found in the manifest.json, so no custom TTF fonts will be installed. This does not affect bitmap fonts");
	} else if (ttf.length <= 0) {
		logger.warn("No \"ttf\" fonts specified in manifest.json, so no custom TTF fonts will be built in. This does not affect bitmap fonts");
	} else {
		for (var i = 0, ilen = ttf.length; i < ilen; ++i) {
			var filePath = ttf[i];

			builder.common.copyFileSync(filePath, path.join(fontDir, path.basename(filePath)));
		}
	}
}

var DEFAULT_ICON_PATH = {
	"36": "drawable-ldpi/icon.png",
	"48": "drawable-mdpi/icon.png",
	"72": "drawable-hdpi/icon.png",
	"96": "drawable-xhdpi/icon.png"
};

function copyIcon(builder, project, destDir, tag, size) {
	var destPath = path.join(destDir, "res/drawable-" + tag + "dpi/icon.png");
	wrench.mkdirSyncRecursive(path.dirname(destPath));

	var android = project.manifest.android;
	var iconPath = android && android.icons && android.icons[size];

	if (iconPath && fs.existsSync(iconPath)) {
		builder.common.copyFileSync(iconPath, destPath);
	} else {
		logger.warn("No icon specified in the manifest for '", size, "'. Using the default icon for this size. This is probably not what you want");

		// Do not copy a default icon to this location -- Android will fill in
		// the blanks intelligently.
		//builder.common.copyFileSync(path.join(__dirname, "TeaLeaf/res", DEFAULT_ICON_PATH[size]), destPath);
	}
}

var DEFAULT_NOTIFY_ICON_PATH = {
	"low": "drawable-ldpi/notifyicon.png",
	"med": "drawable-mdpi/notifyicon.png",
	"high": "drawable-hdpi/notifyicon.png",
	"xhigh": "drawable-xhdpi/notifyicon.png"
};

function copyNotifyIcon(builder, project, destDir, tag, name) {
	var destPath = path.join(destDir, "res/drawable-" + tag + "dpi/notifyicon.png");
	wrench.mkdirSyncRecursive(path.dirname(destPath));

	var android = project.manifest.android;
	var iconPath = android && android.icons && android.icons.alerts && android.icons.alerts[name];

	if (iconPath && fs.existsSync(iconPath)) {
		builder.common.copyFileSync(iconPath, destPath);
	} else {
		logger.warn("No alert icon specified in the manifest for '", name, "'");

		// Do not copy a default icon to this location -- Android will fill in
		// the blanks intelligently.
		//builder.common.copyFileSync(path.join(__dirname, "TeaLeaf/res", DEFAULT_NOTIFY_ICON_PATH[name]), destPath);
	}
}

function copyIcons(builder, project, destDir) {
	copyIcon(builder, project, destDir, "l", "36");
	copyIcon(builder, project, destDir, "m", "48");
	copyIcon(builder, project, destDir, "h", "72");
	copyIcon(builder, project, destDir, "xh", "96");
	copyNotifyIcon(builder, project, destDir, "l", "low");
	copyNotifyIcon(builder, project, destDir, "m", "med");
	copyNotifyIcon(builder, project, destDir, "h", "high");
	copyNotifyIcon(builder, project, destDir, "xh", "xhigh");
}

function copySplash(builder, project, destDir, next) {
	var destPath = path.join(destDir, "assets/resources");
	wrench.mkdirSyncRecursive(destPath);
	
	if (project.manifest.splash) {
		var potentialSplashFiles = ["universal", "portrait1136", "portrait960", "portrait480"];

		// Try to find a potential splash
		for (var i in potentialSplashFiles) {
			if(project.manifest.splash[potentialSplashFiles[i]]) {
				var splashFile = path.resolve(project.manifest.splash[potentialSplashFiles[i]]);
				break;
			}
		}
		
		if (!splashFile) {
			logger.warn("Could not find a suitable splash field for generating splash images in the project manifest. Posible options are:" + JSON.stringify(potentialSplashFiles));
		}

		var splashes = [
			{ outFile: "splash-512.png", outSize: "512" },
			{ outFile: "splash-1024.png", outSize: "1024"},
			{ outFile: "splash-2048.png", outSize: "2048"}
		];

		var f = ff(function () {	
			var sLeft = splashes.length;
			var nextF = f();
			
			var makeSplash = function(i) {
				if (i < 0) {
					nextF();
					return;
				}

				var splash = splashes[i];
				var splashOut = path.join(destPath, splash.outFile);

				logger.log("Creating splash: " + splashOut + " from: " + splashFile);

				builder.jvmtools.exec('splasher', [
					"-i", splashFile,
					"-o", splashOut,
					"-resize", splash.outSize,
					"-rotate", "auto"
				], function (splasher) {
					var formatter = new builder.common.Formatter('splasher');

					splasher.on('out', formatter.out);
					splasher.on('err', formatter.err);
					splasher.on('end', function (data) {
						console.log("at end");
						makeSplash(i-1);
					})
				});
			}

			makeSplash(sLeft-1);
		}).cb(next);
	} else {
		logger.warn("No splash image provided in the manifest");

		next();
	}
}

function copyMusic(builder, project, destDir) {
	if (project.manifest.splash) {
		var destPath = path.join(destDir, "res/raw");
		wrench.mkdirSyncRecursive(destPath);

		var musicPath = project.manifest.splash.song;
		if (musicPath && fs.existsSync(musicPath)) {
			builder.common.copyFileSync(musicPath, path.join(destPath, "loadingsound.mp3"));
		} else {
			logger.warn("No splash music specified in the manifest");
		}
	}
}

function copyResDir(project, destDir) {
	if (project.manifest.android && project.manifest.android.resDir) {
		var destPath = path.join(destDir, "res");
		var sourcePath = path.resolve(project.manifest.android.resDir);
		try {
			wrench.copyDirSyncRecursive(sourcePath, destPath, {preserve: true});
		} catch (e) {
			logger.warn("Could not copy your android resource dir [" + e.toString() + "]");
		}
	}
}

function getAndroidHash(builder, next) {
	builder.git.currentTag(__dirname, function (hash) {
		next(hash || 'unknown');
	});
}

function updateManifest(builder, project, namespace, activity, title, appID, shortName, version, debug, destDir, servicesURL, metadata, studioName, addonConfig, next) {
	var defaults = {
		// Empty defaults
		installShortcut: "false",
		
		// Filled defaults
		entryPoint: "gc.native.launchClient",
		codeHost: "s.wee.cat",
		tcpHost: "s.wee.cat",
		codePort: "80",
		tcpPort: "4747",
		activePollTimeInSeconds: "10",
		passivePollTimeInSeconds: "20",
		syncPolling: "false",
		disableLogs: String(!debug), 
		develop: String(debug),
		servicesUrl: servicesURL,
		pushUrl: servicesURL + "push/%s/?key=%s&version=%s",
		contactsUrl: servicesURL + "users/me/contacts/?key=%s",
		userdataUrl: "",
		studioName: studioName,
	};
	
	var f = ff(function() {
		builder.packager.getGameHash(project, f.slotPlain());
		builder.packager.getSDKHash(f.slotPlain());
		getAndroidHash(builder, f.slotPlain());
		versionCode(project, debug, f.slotPlain());
	}, function(gameHash, sdkHash, androidHash, versionCode) {
		var orientations = project.manifest.supportedOrientations;
		var orientation = "portrait";

		if (orientations.indexOf("portrait") != -1 && orientations.indexOf("landscape") != -1) {
			orientation = "unspecified";
		} else if (orientations.indexOf("landscape") != -1) { 
			orientation = "landscape";
		}

		function copy(target, src) {
			for (var key in src) {
				target[key] = src[key];
			}
		}

		function rename(target, oldKey, newKey) {
			if ('oldKey' in target) {
				target[newKey] = target[oldKey];
				delete target[newKey];
			}
		}

		function explode(target, key, mapping) {
			if (key in target) {
				for (var subKey in mapping) {
					if (target[key][subKey]) {
						target[mapping[subKey]] = target[key][subKey];
					}
				}

				delete target[key];
			}
		}

		var params = {};
			f(params);
			copy(params, defaults);
			copy(params, project.manifest.android);
			copy(params, {
					"package": namespace,
					title: title,
					activity: "." + activity,
					version: "" + version,
					appid: appID,
					shortname: shortName,
					orientation: orientation,
					studioName: studioName,
				gameHash: gameHash,
				sdkHash: sdkHash,
				androidHash: androidHash,
				versionCode: versionCode,
				debuggable: debug ? 'true' : 'false'
			});

		wrench.mkdirSyncRecursive(destDir);

		}, function(params) {
			f(params);

			fs.readFile(path.join(__dirname, "TeaLeaf/AndroidManifest.xml"), "utf-8", f());
		}, function(params, xmlContent) {
			f(params);

			fs.writeFile(path.join(destDir, "AndroidManifest.xml"), xmlContent, "utf-8", f.wait());
		}, function(params) {
			f(params);

			injectPluginXML(builder, {
				addonConfig: addonConfig,
				destDir: destDir
			}, f());
		}, function(params) {
			f(params);

			var xmlPath = path.join(destDir, "AndroidManifest.xml");

			for (var key in addonConfig) {
				var addon = addonConfig[key];

				if (addon.injectionXSL) {
					var xslPath = builder.common.paths.addons(key, "android", addon.injectionXSL);

					transformXSL(builder, xmlPath, xmlPath, xslPath, params, f());
				}
			}
		}, function(params) {
			f(params);

			var xmlPath = path.join(destDir, "AndroidManifest.xml");

			transformXSL(builder, xmlPath, xmlPath,
					path.join(__dirname, "AndroidManifest.xsl"),
					params,
                    f());
        }).error(function(err) {
			logger.error("Error transforming XSL for AndroidManifest.xml:", err);
			process.exit(2);
		}).success(function() {
			next();
		}
	);
}

function versionCode(proj, debug, next) {
	var versionPath = path.join(proj.paths.root, '.version');
	var version;

	var f = ff(this, function() {
		fs.exists(versionPath, f.slotPlain());
	}, function(exists) {
		//if !exists create it
		var onFinish = f.wait();
		if (!exists) {
			fs.writeFile(versionPath, '0', onFinish);
		} else {
			onFinish();
		}
	}, function() {
		//read the version
		fs.readFile(versionPath, f());
	}, function(readVersion) {
		version = parseInt(readVersion, 10);

		if (isNaN(version)) {
			logger.error(".version file seems incorrect. Make sure it's correctly formatted");
			if (!debug) process.exit();
		}

		var onFinish = f.wait();

		if (!debug) {
			fs.writeFile(versionPath, version+=1, onFinish);
		} else {
			onFinish();
		}
	}, function() {
		next(version);
	}).error(function(err) {
		if (!debug) {
			logger.error("Could not get version code:", err);
			process.exit();
		} else {
			logger.warn("Could not get version code. In a release build this would be an error");
			next(0);
		}
	});
}

function updateActivity(project, namespace, activity, destDir, next) {
	var activityFile = path.join(destDir, "src/" + namespace.replace(/\./g, "/") + "/" + activity + ".java");

	if (fs.existsSync(activityFile)) {
		fs.readFile(activityFile, 'utf-8', function (err, contents) {
			contents = contents
				.replace(/extends Activity/g, "extends com.tealeaf.TeaLeaf")
				.replace(/setContentView\(R\.layout\.main\);/g, "startGame();");
			fs.writeFile(activityFile, contents, next);
		});
	}
}

exports.build = function(builder, project, opts, next) {
	logger = new builder.common.Formatter('native-android');

	var argv = opts.argv;

	// Command line options.
	var debug = argv.debug;
	var clean = argv.clean;

	// Extracted values from options.
	var packageName = opts.packageName;
	var studio = opts.studio;
	var metadata = opts.metadata;

	var f = ff(this, function() {
		validateSubmodules(f());
	}).error(function(err) {
		logger.error("Failure to validate submodules:", err);
		process.exit(2);
	});

	// Extract manifest properties.
	var appID = project.manifest.appID;
	var shortName = project.manifest.shortName;
	// Verify they exist.
	if (appID === null || shortName === null) {
		throw new Error("Build aborted: No appID or shortName in the manifest");
	}

	appID = appID.replace(PUNCTUATION_REGEX, ""); // Strip punctuation.
	// Destination directory is the android build directory.
	var destDir = path.join(__dirname, "build/" + shortName);

	// Remove existing build directory.
	wrench.rmdirSyncRecursive(destDir, true);

	// Project title.
	var title = project.manifest.title;
	if (title === null) {
		title = shortName;
	}
	// Create Android Activity name.
	var activity = shortName + "Activity";
	// Studio qualified name.
	if (studio === null) {
		studio = "wee.cat";
	}
	var names = studio.split(/\./g).reverse();
	studio = names.join('.');

	var studioName = project.manifest.studio && project.manifest.studio.name;
	var servicesURL = opts.servicesURL;

	if (packageName === null || packageName.length === 0) {
		packageName = studio + "." + shortName;
	}

	// Build the project archive. Save the APK dir now, since we're going to redirect
	// all output to the native build directory
	var apkDir = opts.output;
	opts.output = path.join(destDir, "assets/resources");

	// Parallelize android project setup and sprite building.
	var apkPath;
	var addonConfig = {};

	var f = ff(function () {
		installAddons(builder, project, opts, addonConfig, f());
	}, function() {
		require(builder.common.paths.nativeBuild("native")).writeNativeResources(project, opts, f.waitPlain());

		makeAndroidProject(builder, project, packageName, activity, title, appID,
			shortName, opts.version, debug, destDir, servicesURL, metadata,
			studioName, addonConfig, f.waitPlain());

		var cleanProj = (builder.common.config.get("lastBuildWasDebug") != debug) || clean;
		builder.common.config.set("lastBuildWasDebug", debug);
		buildSupportProjects(builder, project, destDir, debug, cleanProj, f.waitPlain());
	}, function() {
		copyFonts(builder, project, destDir);
		copyIcons(builder, project, destDir);
		copyMusic(builder, project, destDir);
		copyResDir(project, destDir);
		copySplash(builder, project, destDir, f());

		installAddonCode(builder, {
			addonConfig: addonConfig,
			destDir: destDir
		}, f());
	}, function() {
		var onDoneBuilding = f();

		buildAndroidProject(builder, destDir, debug, function (success) {
			var apk = "";
			if (!debug) {
				apk = shortName + "-aligned.apk";
			} else {
				apk = shortName + "-debug.apk";
			}

			(!debug ? signAPK : nextStep)(builder, shortName, destDir, function () {
				apkPath = path.join(apkDir, shortName + ".apk");
				if (fs.existsSync(apkPath)) {
					fs.unlinkSync(apkPath);
				}

				var destApkPath = path.join(destDir, "bin/" + apk);
				if (fs.existsSync(destApkPath)) {
					wrench.mkdirSyncRecursive(path.dirname(apkPath), 0777);
					builder.common.copyFileSync(destApkPath, apkPath);
					logger.log("built", clc.yellow.bright(packageName));
					logger.log("saved to " + clc.blue.bright(apkPath));
					onDoneBuilding();
				} else {
					logger.error("No file at " + destApkPath);
					next(2);
				}

			});
		});
	}, function() {
		if (argv.install || argv.open) {
			var cmd = 'adb uninstall "' + packageName + '"';
			logger.log('Install: Running ' + cmd + '...');
			builder.common.child('adb', ['uninstall', packageName], {}, f.waitPlain()); //this is waitPlain because it can fail and not break.
		}
	}, function() {
		if (argv.install || argv.open) {
			var cmd = 'adb install -r "' + apkPath + '"';
			logger.log('Install: Running ' + cmd + '...');
			builder.common.child('adb', ['install', '-r', apkPath], {}, f.waitPlain()); //this is waitPlain because it can fail and not break.
		}
	}, function () {
		if (argv.open) {
			var startCmd = packageName + '/' + packageName + '.' + shortName + 'Activity';
			var cmd = 'adb shell am start -n ' + startCmd;
			logger.log('Install: Running ' + cmd + '...');
			builder.common.child('adb', ['shell', 'am', 'start', '-n', startCmd], {}, f.waitPlain()); //this is waitPlain because it can fail and not break.
		}
	}, function () {
		next(0);
	}).error(function (err) {
		logger.error("Build failure:", err);
	});
};

