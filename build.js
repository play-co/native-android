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


//// Addons

var installAddons = function(builder, project, opts, addonConfig, next) {
	var paths = builder.common.paths;
	var addons = project.getAddonConfig();
	if (!Array.isArray(addons)) {
		addons = Object.keys(addons);
	}

	var f = ff(this, function() {

		var addonConfigMap = {};
		var next = f.slotPlain();
		var addonQueue = [];
		var checkedAddonMap = {};
		if (addons) {
			var missingAddons = [];
			for (var ii = 0; ii < addons.length; ++ii) {
				addonQueue.push(addons[ii]);
			}

			var processAddonQueue = function() {
				var addon = null;
				if (addonQueue.length > 0) {
					addon = addonQueue.shift();
				} else {
					if (missingAddons.length > 0) {
						logger.error("=========================================================================");
						logger.error("Missing addons =>", JSON.stringify(missingAddons));
						logger.error("=========================================================================");
						process.exit(1);
					}

					next(addonConfigMap);
					return;
				}
				var addonConfig = paths.addons(addon, 'android', 'config.json');

				if (fs.existsSync(addonConfig)) {
					fs.readFile(addonConfig, 'utf8', function(err, data) {
						if (!err && data) {
							var config = JSON.parse(data);
							addonConfigMap[addon] = data;
							if (config.addonDependencies && config.addonDependencies.length > 0) {
								for (var a in config.addonDependencies) {
									var dep = config.addonDependencies[a];
									if (!checkedAddonMap[dep]) {
										checkedAddonMap[dep] = true;
										addonQueue.push(dep);
									}
								}
							}
						}
						processAddonQueue();
					});
				} else {
					if (!checkedAddonMap[addon]) {
						checkedAddonMap[addon] = true;
					}
					if (missingAddons.indexOf(addon) == -1) {
						missingAddons.push(addon);
						logger.warn("Unable to find Android addon config file", addonConfig);
					}
					processAddonQueue();
				}
			};

			processAddonQueue();

		} else {
			next({});
		}
	}, function(addonConfigMap) {
		if (addonConfigMap) {
            for (var addon in addonConfigMap) {
                addonConfig[addon] = JSON.parse(addonConfigMap[addon]);
				logger.log("Configured addon:", addon);
            }
        }
	}).error(function(err) {
		logger.error("Failure to install addons:", err, err.stack);
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
			var injectionXML = addonConfig[key].injectionXML;

			if (injectionXML) {
				var filepath = builder.common.paths.addons(key, "android", injectionXML);
				logger.log("Reading plugin XML:", filepath);

				fs.readFile(filepath, "utf-8", group());
			}
		}

		fs.readFile(manifestXml, "utf-8", f());
	}, function(results, xml) {
		if (results && results.length > 0 && xml && xml.length > 0) {
			var XML_START_PLUGINS_MANIFEST = "<!--START_PLUGINS_MANIFEST-->";
			var XML_END_PLUGINS_MANIFEST = "<!--END_PLUGINS_MANIFEST-->";
			var XML_START_PLUGINS_ACTIVITY = "<!--START_PLUGINS_ACTIVITY-->";
			var XML_END_PLUGINS_ACTIVITY = "<!--END_PLUGINS_ACTIVITY-->";
			var XML_START_PLUGINS_APPLICATION = "<!--START_PLUGINS_APPLICATION-->";
			var XML_END_PLUGINS_APPLICATION = "<!--END_PLUGINS_APPLICATION-->";

			var manifestXmlManifestStr = "";
			var manifestXmlActivityStr = "";
			var manifestXmlApplicationStr = "";

			for (var i = 0; i < results.length; ++i) {
				var pluginXml = results[i];

				manifestXmlManifestStr += getTextBetween(pluginXml, XML_START_PLUGINS_MANIFEST, XML_END_PLUGINS_MANIFEST);
				manifestXmlActivityStr += getTextBetween(pluginXml, XML_START_PLUGINS_ACTIVITY, XML_END_PLUGINS_ACTIVITY);
				manifestXmlApplicationStr += getTextBetween(pluginXml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION);
			}

			xml = replaceTextBetween(xml, XML_START_PLUGINS_MANIFEST, XML_END_PLUGINS_MANIFEST, manifestXmlManifestStr);
			xml = replaceTextBetween(xml, XML_START_PLUGINS_ACTIVITY, XML_END_PLUGINS_ACTIVITY, manifestXmlActivityStr);
			xml = replaceTextBetween(xml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION, manifestXmlApplicationStr);
			fs.writeFile(manifestXml, xml, "utf-8", f.wait());
		} else {
			logger.log("No plugin XML to inject");
		}
	}).success(next).error(function(err) {
		logger.error("Inject plugin XML failure:", err, err.stack);
		process.exit(1);
	});
}

var installAddonGameFiles = function(builder, opts, next) {
	var addonConfig = opts.addonConfig;
	var project = opts.project;

	var filePaths = [];

	var f = ff(function() {
		var group = f.group();

		for (var addon in addonConfig) {
			var config = addonConfig[addon];

			if (config.copyGameFiles) {
				for (var ii = 0; ii < config.copyGameFiles.length; ++ii) {
					var filePath = path.join(project.paths.root, config.copyGameFiles[ii]);

					logger.log("Installing game-specific plugin file:", filePath);

					fs.readFile(filePath, "binary", group.slot());

					// Record target path
					filePaths.push(builder.common.paths.addons(addon, 'android', config.copyGameFiles[ii]));
				}
			}
		}
	}, function(results) {
		if (results && results.length > 0) {
			for (var ii = 0; ii < results.length; ++ii) {
				var data = results[ii];
				var filePath = filePaths[ii];

				if (data) {
					logger.log(" - Writing to:", filePath);

					fs.writeFile(filePath, data, 'binary', f.wait());
				} else {
					logger.error("Unable to read file expected in game directory:", filePath, "(requested by addons)");
				}
			}
		} else {
			logger.log("No game files to add");
		}
	}).success(next).error(function(err) {
		logger.error("Error while installing addon game files code:", err, err.stack);
		process.exit(1);
	});
}

var installAddonCode = function(builder, opts, next) {
	var addonConfig = opts.addonConfig;
	var destDir = opts.destDir;
	var project = opts.project;

	var filePaths = [];
	var replacers = [];
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

					replacers.push(config.injectionSource);

					if (path.extname(filePath) === ".java" ||
						path.extname(filePath) === ".aidl") {
						filePaths.push(filePath);
						fs.readFile(filePath, "utf-8", group.slot());
					} else {
						filePaths.push(config.copyFiles[ii]);
						fs.readFile(filePath, "binary", group.slot());
					}
				}
			}

			if (config.libraries) {
				for (var ii = 0; ii < config.libraries.length; ++ii) {
					var library = builder.common.paths.addons(addon, 'android', config.libraries[ii]);

					logger.log("Installing addon library:", library);

					libraries.push(library);

					// Set up library build files
					builder.common.child('android', [
							"update", "project", "--target", opts.target,
							"--path", library
							], {}, f.wait());
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

	}, function(results, properties) {
		if (results && results.length > 0) {
			for (var ii = 0; ii < results.length; ++ii) {
				var data = results[ii];
				var filePath = filePaths[ii];

				if (data) {
					if (path.extname(filePath) === ".java" ||
						path.extname(filePath) === ".aidl") {
						var pkgName = data.match(/(package[\s]+)([a-z.A-Z0-9]+)/g)[0].split(' ')[1];
						var pkgDir = pkgName.replace(/\./g, "/");
						var outFile = path.join(destDir, "src", pkgDir, path.basename(filePath));

						logger.log("Installing Java package", pkgName, "to", outFile);

						wrench.mkdirSyncRecursive(path.dirname(outFile));

						// Run injectionSource section of associated addon
						var replacer = replacers[ii];
						if (replacer && replacer.length > 0) {
							for (var jj = 0; jj < replacer.length; ++jj) {
								var findString = replacer[jj].regex;
								var keyForReplace = replacer[jj].keyForReplace;
								var replaceString = project.manifest.android[keyForReplace];
								if (replaceString) {
									logger.log(" - Running find-replace for", findString, "->", replaceString, "(android:", keyForReplace + ")");
									var rexp = new RegExp(findString, "g");
									data = data.replace(rexp, replaceString);
								} else {
									logger.error(" - Unable to find android key for", keyForReplace);
								}
							}
						}

						fs.writeFile(outFile, data, 'utf-8', f.wait());
					} else if (path.extname(filePath) === ".so") {
						var armeabi_out = ["libs/armeabi", "libs/armeabi-v7a"];
						for(var jj = 0; jj != armeabi_out.length; jj++) {
							var outFile = path.join(destDir, armeabi_out[jj], path.basename(filePath));
							logger.log("Writing shared object", filePath, "to", outFile);
							wrench.mkdirSyncRecursive(path.dirname(outFile));
							fs.writeFile(outFile, data, 'binary', f.wait());
						}
					} else {
						var outFile = path.join(destDir, filePath);

						fs.writeFile(outFile, data, 'binary', f.wait());
					}
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

		if (jars && jars.length > 0) {
			for (var ii = 0; ii < jars.length; ++ii) {
				var jarPath = jars[ii];
				var jarDestPath = path.join(destDir, "libs", path.basename(jarPath));
				logger.log("Installing JAR file:", jarDestPath);
				fs.symlinkSync(jarPath, jarDestPath, 'junction');
			}
		} else {
			logger.log("No JAR file data to install");
		}
	}).success(next).error(function(err) {
		logger.error("Error while installing addon code:", err, err.stack);
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
					logger.error("settings for AndroidManifest: value for", clc.yellowBright(key), "is not a string");
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
		logger.error("Transform XSL failure:", err, err.stack);
		process.exit(1);
	});
}

exports.transformXSL = transformXSL;

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
		logger.error("Validate submodule failure:", err, err.stack);
		process.exit(1);
	});
}

function buildSupportProjects(builder, opts, next) {
	var tealeafDir;

	var f = ff(this, function() {
		tealeafDir = path.join(__dirname, "TeaLeaf");
		if (opts.clean || opts.arch) {
			builder.common.child('make', ['clean'], {cwd: __dirname}, f.slot());
		}
	}, function() {
		var args = ["-j", "8", (opts.debug ? "DEBUG=1" : "RELEASE=1")];
		if (opts.arch) {
			args.push('APP_ABI=' + opts.arch);
		}
		builder.common.child('ndk-build', args , { cwd: tealeafDir }, f.wait());
	}).failure(function(e) {
		logger.error("Could not build support projects:", e, e.stack);
		process.exit(2);
	}).success(next);
}

function buildAndroidProject(builder, destDir, debug, next) {
	builder.common.child('ant', [(debug ? "debug" : "release")], {
		cwd: destDir
	}, next);
}

function saveLocalizedStringsXmls(destDir, titles) {
	var stringsXmlPath = path.join(destDir, "res/values/strings.xml");
	var stringsXml = fs.readFileSync(stringsXmlPath, "utf-8");
	for (var t in titles) {
		var title = titles[t];
		var i = stringsXml.indexOf('</resources>');
		var first = stringsXml.substring(0, i);
		var second = stringsXml.substring(i);
		var inner = '<string name="title">' + title + '</string>';
		var finalXml = first + inner + second;
		//default en
		if (t === 'en') {
			fs.writeFileSync(path.join(destDir, "res/values/strings.xml"), finalXml, 'utf-8');
		} else {
			fs.mkdirSync(path.join(destDir, "res/values-" + t));
			fs.writeFileSync(path.join(destDir, "res/values-" + t + "/strings.xml"), finalXml, 'utf-8');
		}
	}
}

function makeAndroidProject(builder, opts, next) {
	var f = ff(function() {
		builder.common.child('android', [
			"create", "project", "--target", opts.target, "--name", opts.shortName,
			"--path", opts.destDir, "--activity", opts.activity,
			"--package", opts.namespace
		], {}, f());
	}, function() {
		builder.common.child('android', [
			"update", "project", "--target", opts.target,
			"--path", opts.destDir,
			"--library", "../../TeaLeaf"
		], {}, f());
	}, function() {
		fs.appendFile(path.join(opts.destDir, 'project.properties'), 'out.dexed.absolute.dir=../.dex/\nsource.dir=src\n',f());
	}, function() {
		var titles = opts.titles;
		console.log(titles);
		if (titles) {
			if (titles.length == 0) {
				titles['en'] = opts.title;
			}
		}  else {
			titles = {};
			titles['en'] = opts.title;
		}
		saveLocalizedStringsXmls(opts.destDir, titles);
		updateManifest(builder, opts, f.waitPlain());
		updateActivity(opts, f.waitPlain());
	}).error(function(err) {
		logger.error("Build failed creating android project:", err, err.stack);
		process.exit(2);
	}).success(next);
}

function signAPK(builder, shortName, destDir, debug, next) {
	var signArgs, alignArgs;
	var binDir = path.join(destDir, "bin");

	logger.log('Signing APK at ', binDir);

	var keystore = builder.common.config.get('android.keystore');
	var storepass = builder.common.config.get('android.storepass');
	var keypass = builder.common.config.get('android.keypass');
	var key = builder.common.config.get('android.key');

	if (debug) {
		var keyPath = path.join(process.env['HOME'], '.android', 'debug.keystore');
		signArgs = [
			"-sigalg", "MD5withRSA", "-digestalg", "SHA1",
			"-keystore", keyPath, "-storepass", "android",
			"-signedjar", shortName + "-debug-unaligned.apk",
			shortName + "-debug.apk", "androiddebugkey"
				];
		alignArgs = [
			"-f", "-v", "4", shortName + "-debug-unaligned.apk", shortName + "-debug.apk"
			];
	} else {
		signArgs = [
			"-sigalg", "MD5withRSA", "-digestalg", "SHA1",
			"-keystore", keystore, "-storepass", storepass, "-keypass", keypass,
			"-signedjar", shortName + "-unaligned.apk",
			shortName + "-release-unsigned.apk", key
				];
		alignArgs = [
			"-f", "-v", "4", shortName + "-unaligned.apk", shortName + "-aligned.apk"
			];
	}

	var f = ff(function() {
		builder.common.child('jarsigner', signArgs, {cwd: binDir}, f.slotPlain());
	}, function(err) {
		if(err) {
			logger.error("Unable to sign APK");
			process.exit(2);
		}
		builder.common.child('zipalign', alignArgs , {cwd: binDir}, next);
	});
}


function repackAPK(builder, destDir, apkName, next) {
	var apkPath = path.join('bin', apkName);
	console.log(destDir, apkPath, apkName);
	builder.common.child('zip', [apkPath, '-d', 'META-INF/*'], {cwd: destDir}, function() {
		builder.common.child('zip', [apkPath, '-u'], {cwd: destDir}, next)
	});
};

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
	"96": "drawable-xhdpi/icon.png",
	"144": "drawable-xxhdpi/icon.png"
};

function copyIcon(builder, project, destDir, tag, size) {
	var destPath = path.join(destDir, "res/drawable-" + tag + "dpi/icon.png");
	var android = project.manifest.android;
	var iconPath = android && android.icons && android.icons[size];

	if (iconPath && fs.existsSync(iconPath)) {
		wrench.mkdirSyncRecursive(path.dirname(destPath));
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
	"xhigh": "drawable-xhdpi/notifyicon.png",
	"xxhigh": "drawable-xxhdpi/notifyicon.png"
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
	copyIcon(builder, project, destDir, "xxh", "144");
	copyIcon(builder, project, destDir, "xxxh", "192");
	copyNotifyIcon(builder, project, destDir, "l", "low");
	copyNotifyIcon(builder, project, destDir, "m", "med");
	copyNotifyIcon(builder, project, destDir, "h", "high");
	copyNotifyIcon(builder, project, destDir, "xh", "xhigh");
	copyNotifyIcon(builder, project, destDir, "xxh", "xxhigh");
}

function copySplash(builder, project, destDir, next) {
	var destPath = path.join(destDir, "assets/resources");
	wrench.mkdirSyncRecursive(destPath);

	var splashes = [
		{
			copyFile: "portrait512",
			inFiles: [
				"portrait960",
				"portrait1024",
				"portrait1136",
				"portrait480",
				"universal",
				"portrait2048"
			],
			outFile: "splash-512.png",
			outSize: "512"
		},
		{
			copyFile: "portrait1024",
			inFiles: [
				"portrait1136",
				"universal",
				"portrait960",
				"portrait2048"
/*				"portrait512",
				"portrait480"*/
			],
			outFile: "splash-1024.png",
			outSize: "1024"
		},
		{
			copyFile: "portrait2048",
			inFiles: [
				"universal"
/*				"portrait1136",
				"portrait1024",
				"portrait960",
				"portrait512",
				"portrait480"*/
			],
			outFile: "splash-2048.png",
			outSize: "2048"
		}
	];

	var splashPaths = project.manifest.splash || {};

	var f = ff(function () {
		var group = f.group();
		var splasherTasks = [];

		// For each splash image to produce,
		for (var ii = 0; ii < splashes.length; ++ii) {
			var splash = splashes[ii];
			var outFile = splash.outFile;
			var outSize = splash.outSize;
			var copyFile = splash.copyFile;
			var inFiles = splash.inFiles;

			// Look up default copy file
			var srcFile = splashPaths[copyFile];
			var copying = true;

			// If copy source file DNE,
			if (!srcFile) {
				// Will not be copying
				copying = false;

				// For each candidate replacement,
				for (var jj = 0; jj < inFiles.length; ++jj) {
					// Read manifest to see if it is specified
					var candidate = inFiles[jj];
					srcFile = splashPaths[candidate];

					// If found one that exists,
					if (srcFile) {
						// Stop at the first (best) one
						break;
					}
				}
			}

			// If file was specified,
			if (srcFile) {
				// Resolve it to a valid path
				srcFile = path.resolve(srcFile);

				// If file does not exist,
				if (!srcFile || !fs.existsSync(srcFile)) {
					logger.warn("Splash file specified by your game manifest does not exist:", srcFile);
					srcFile = null;
				}
			}

			// If no input file exists,
			if (!srcFile) {
				srcFile = builder.common.paths.lib("defsplash.png");

				if (!srcFile || !fs.existsSync(srcFile)) {
					logger.warn("Default splash file does not exist:", srcFile);
					srcFile = null;
				} else {
					logger.warn("No splash screen images provided for size", outSize, "so using default image", srcFile);
				}
			}

			// If a source file was found,
			if (srcFile) {
				outFile = path.join(destPath, outFile);

				// If copying,
				if (copying) {
					logger.log("Copying size", outSize, "splash:", outFile, "from", srcFile);

					builder.common.copyFileSync(srcFile, outFile);
				} else {
					splasherTasks.push({
						'outSize': outSize,
						'outFile': outFile,
						'srcFile': srcFile
					});
				}
			} // end if input file exists
		} // next splash screen

		// Run splasher one at a time because the jvm tool is unable to run multiple
		// instances of the same Java application in parallel.  FIXME
		function runSplasher() {
			var task = splasherTasks.pop();
			if (!task) {
				return;
			}

			var slot = group();

			logger.log("Creating size", task.outSize, "splash:", task.outFile, "from", task.srcFile);

			builder.jvmtools.exec('splasher', [
				"-i", task.srcFile,
				"-o", task.outFile,
				"-resize", task.outSize,
				"-rotate", "auto"
			], function (splasher) {
				var formatter = new builder.common.Formatter('splash' + task.outSize);

				splasher.on('out', formatter.out);
				splasher.on('err', formatter.err);
				splasher.on('end', function (data) {
					logger.log("Done splashing size", task.outSize);
					runSplasher();
					slot();
				});
			});
		}

		// Run splasher on tasks
		runSplasher();
	}).cb(next);
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

function updateManifest(builder, opts, next) {
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
		disableLogs: String(opts.disableLogs),
		develop: String(opts.debug),
		servicesUrl: opts.servicesURL,
		pushUrl: opts.servicesURL + "push/%s/?key=%s&version=%s",
		contactsUrl: opts.servicesURL + "users/me/contacts/?key=%s",
		userdataUrl: "",
		studioName: opts.studioName,
	};

	var f = ff(function() {
		builder.packager.getGameHash(opts.project, f.slotPlain());
		builder.packager.getSDKHash(f.slotPlain());
		getAndroidHash(builder, f.slotPlain());
		versionCode(opts.project, opts.debug, f.slotPlain());
	}, function(gameHash, sdkHash, androidHash, versionCode) {
		var orientations = opts.project.manifest.supportedOrientations;
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

    function copyAndFlatten(target, src, prefix) {
      prefix = prefix || '';

      for (var key in src) {
        var val = src[key];
        var newPrefix = prefix.length === 0 ? key : prefix + '.' + key;
        if(typeof val === "object") {
          copyAndFlatten(target, val, newPrefix);
        } else {
          // Push to final object
          target[newPrefix] = val;
        }
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
			copy(params, opts.project.manifest.android);
			copyAndFlatten(params, opts.project.manifest.addons);
			copy(params, {
				"package": opts.namespace,
				title: "@string/title",
				activity: opts.namespace + "." + opts.activity,
				version: "" + opts.version,
				appid: opts.appID,
				shortname: opts.shortName,
				orientation: orientation,
				studioName: opts.studioName,
				gameHash: gameHash,
				sdkHash: sdkHash,
				androidHash: androidHash,
				versionCode: versionCode,
				debuggable: opts.debug ? 'true' : 'false'
			});

		wrench.mkdirSyncRecursive(opts.destDir);

		}, function(params) {
			f(params);

			fs.readFile(path.join(__dirname, "TeaLeaf/AndroidManifest.xml"), "utf-8", f());
		}, function(params, xmlContent) {
			f(params);

			fs.writeFile(path.join(opts.destDir, "AndroidManifest.xml"), xmlContent, "utf-8", f.wait());
		}, function(params) {
			f(params);

			injectPluginXML(builder, opts, f());
		}, function(params) {
			f(params);

			var xmlPath = path.join(opts.destDir, "AndroidManifest.xml");

			var xslPaths = [];

			for (var key in opts.addonConfig) {
				var addon = opts.addonConfig[key];

				if (addon.injectionXSL) {
					var xslPath = builder.common.paths.addons(key, "android", addon.injectionXSL);

					xslPaths.push(xslPath);
				}
			}

			// Run the plugin XSLT in series instead of parallel
			if (xslPaths.length > 0) {
				var allDone = f.waitPlain();

				var runPluginXSLT = function(index) {
					if (index >= xslPaths.length) {
						allDone();
					} else {
						var xslPath = xslPaths[index];

						logger.log("Transforming XML with plugin XSL for", xslPath);

						transformXSL(builder, xmlPath, xmlPath, xslPath, params, function() {
							runPluginXSLT(index + 1);
						});
					}
				}

				runPluginXSLT(0);
			}
		}, function(params) {
			logger.log("Applying final XSL transformation");
			f(params);

			var xmlPath = path.join(opts.destDir, "AndroidManifest.xml");

			transformXSL(builder, xmlPath, xmlPath,
					path.join(__dirname, "AndroidManifest.xsl"),
					params,
                    f());
        }).error(function(err) {
			logger.error("Error transforming XSL for AndroidManifest.xml:", err, err.stack);
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
			logger.error("Could not get version code:", err, err.stack);
			process.exit();
		} else {
			logger.warn("Could not get version code. In a release build this would be an error");
			next(0);
		}
	});
}

function updateActivity(opts, next) {
	var activityFile = path.join(opts.destDir, "src/" + opts.namespace.replace(/\./g, "/") + "/" + opts.activity + ".java");

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

	var repack = argv.repack;

	// Disable logs if --logging is not specified and in release mode.
	var disableLogs = !argv.logging && !debug;

	if (disableLogs) {
		logger.warn("Disabling JS logs in release mode.  Add --logging to your build command to enable adb logcat JS log output in release mode");
	} else {
		logger.log("Enabling JS logs");
	}

	// Extracted values from options.
	var packageName = opts.packageName;
	var studio = opts.studio;
	var metadata = opts.metadata;

	var f = ff(this, function() {
		validateSubmodules(f());
	}).error(function(err) {
		logger.error("Failure to validate submodules:", err, err.stack);
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
	if (!repack) {
		wrench.rmdirSyncRecursive(destDir, true);
	}

	// Project title.
	var title = project.manifest.title;
	var titles = project.manifest.titles;
	if (title === null && titles === null) {
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

	var androidTarget = "android-15";

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
	var apkBuildName = "";
	if (!debug) {
		apkBuildName = shortName + "-aligned.apk";
	} else {
		apkBuildName = shortName + "-debug.apk";
	}

	var f = ff(function () {
		if (!repack) {
			installAddons(builder, project, opts, addonConfig, f());
		}
	}, function() {
			require(builder.common.paths.nativeBuild("native")).writeNativeResources(builder, project, opts, f.waitPlain());

		if (!repack) {
			makeAndroidProject(builder, {
				project: project,
				namespace: packageName,
				activity: activity,
				title: title,
				appID: appID,
				shortName: shortName,
				version: opts.version,
				debug: debug,
				destDir: destDir,
				servicesURL: servicesURL,
				metadata: metadata,
				studioName: studioName,
				addonConfig: addonConfig,
				disableLogs: disableLogs,
				titles: titles,
				target: androidTarget
			}, f.waitPlain());

			var cleanProj = (builder.common.config.get("lastBuildWasDebug") != debug) || clean;
			builder.common.config.set("lastBuildWasDebug", debug);
			buildSupportProjects(builder, {
				debug: debug,
				arch: argv.arch,
				clean: cleanProj
			}, f.waitPlain());
		}
	}, function() {
		if (!repack) {
			installAddonGameFiles(builder, {
				addonConfig: addonConfig,
				project: project
			}, f());
		}
	}, function() {
		if (!repack) {
			copyFonts(builder, project, destDir);
			copyIcons(builder, project, destDir);
			copyMusic(builder, project, destDir);
			copyResDir(project, destDir);
			copySplash(builder, project, destDir, f());

			installAddonCode(builder, {
				addonConfig: addonConfig,
				destDir: destDir,
				project: project,
				target: androidTarget
			}, f());
		}
	}, function () {
		if (!repack) {
			buildAndroidProject(builder, destDir, debug, f());
		} else {
			repackAPK(builder, destDir, apkBuildName, f());
		}
	}, function () {
		if (!argv.noapk) {
			if (!debug || repack) {
				signAPK(builder, shortName, destDir, debug, f());
			}
		}
	}, function () {
		if (!argv.noapk) {
			apkPath = path.join(apkDir, shortName + ".apk");
			if (fs.existsSync(apkPath)) {
				fs.unlinkSync(apkPath);
			}
			var destApkPath = path.join(destDir, "bin", apkBuildName);
			if (fs.existsSync(destApkPath)) {
				wrench.mkdirSyncRecursive(path.dirname(apkPath), 0777);
				builder.common.copyFileSync(destApkPath, apkPath);
				logger.log("built", clc.yellowBright(packageName));
				logger.log("saved to " + clc.blueBright(apkPath));
			} else {
				logger.error("No file at " + destApkPath);
				next(2);
			}
		}
	}, function() {
		if (!argv.noapk) {
			if (argv.install || argv.open) {
				var keepStorage = argv.clearstorage ? "" : "-k";
				var cmd = 'adb uninstall ' + keepStorage + ' "' + packageName + '"';
				logger.log('Install: Running ' + cmd + '...');
				var argz = ['shell', 'pm', 'uninstall'];
				keepStorage && argz.push(keepStorage);
				argz.push(packageName);
				builder.common.child('adb', argz, {}, f.waitPlain()); //this is waitPlain because it can fail and not break.
			}
		}
	}, function() {
		if (!argv.noapk) {
			if (argv.install || argv.open) {
				var cmd = 'adb install -r "' + apkPath + '"';
				logger.log('Install: Running ' + cmd + '...');
				builder.common.child('adb', ['install', '-r', apkPath], {}, f.waitPlain()); //this is waitPlain because it can fail and not break.
			}
		}
	}, function () {
		if (!argv.noapk) {
			if (argv.open) {
				var startCmd = packageName + '/' + packageName + '.' + shortName + 'Activity';
				var cmd = 'adb shell am start -n ' + startCmd;
				logger.log('Install: Running ' + cmd + '...');
				builder.common.child('adb', ['shell', 'am', 'start', '-n', startCmd], {}, f.waitPlain()); //this is waitPlain because it can fail and not break.
			}
		}

		f(destDir);
	}).error(function (err) {
		logger.error("Build failure:", err, err.stack);
		process.exit(2);
	}).next(next);
};
