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
var mkdirp = require('mkdirp');

var androidVersion = require('./package.json').version;

var logger;


function spawnWithLogger(api, name, args, opts, cb) {
  var logger = api.logging.get(name);
  logger.log(name, args.join(' '));
  var child = spawn(name, args, opts);
  child.stdout.pipe(logger, {end: false});
  child.stderr.pipe(logger, {end: false});
  child.on('close', function (err) {
    cb(err);
  });
}

function copyFileSync(from, to) {
  fs.writeFileSync(to, fs.readFileSync(from));
}


//// Addons

var getModuleConfig = function(api, app, config, cb) {
  var config = {};
  var f = ff(function () {
    Object.keys(app.modules).forEach(function (moduleName) {
      var modulePath = app.modules[moduleName].path;
      var configFile = path.join(modulePath, 'android', 'config.json');
      var next = f.wait();
      fs.readFile(configFile, 'utf8', function(err, data) {
        // modules are not required to have a config.json, so if loading config.json fails, consume the error
        if (err && err.code == 'ENOENT') { return next(); }
        if (err) { next(err); }

        try {
          config[moduleName] = {
            config: JSON.parse(data),
            path: modulePath
          };
        } catch (e) {
          return next(e);
        }

        next();
      });
    });

    f(config);
  }).cb(cb);
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

function injectPluginXML(opts, cb) {
  var moduleConfig = opts.moduleConfig;
  var outputPath = opts.outputPath;
  var manifestXml = path.join(outputPath, "AndroidManifest.xml");

  var f = ff(function() {
    var group = f.group();

    for (var moduleName in moduleConfig) {
      var injectionXML = moduleConfig[moduleName].config.injectionXML;

      if (injectionXML) {
        var filepath = path.join(moduleConfig[moduleName].path, 'android', injectionXML);
        logger.log("Reading plugin XML:", filepath);

        fs.readFile(filepath, "utf-8", group());
      }
    }

    fs.readFile(manifestXml, "utf-8", f());
  }, function(results, xml) {
    // TODO: don't use regular expressions

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
  }).cb(cb);
}

var installModuleCode = function (api, app, opts, cb) {
  var moduleConfig = opts.moduleConfig;
  var outputPath = opts.outputPath;

  var filePaths = [];
  var replacers = [];
  var libraries = [];
  var jars = [];
  var jarPaths = [];

  var f = ff(function() {
    var group = f.group();

    for (var moduleName in moduleConfig) {
      var config = moduleConfig[moduleName].config;
      var modulePath = moduleConfig[moduleName].path;

      if (config.copyFiles) {
        for (var ii = 0; ii < config.copyFiles.length; ++ii) {
          var filePath = path.join(modulePath, 'android', config.copyFiles[ii]);

          logger.log("Installing module Java code:", filePath);

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
          var library = path.join(modulePath, 'android', config.libraries[ii]);

          logger.log("Installing module library:", library);

          libraries.push(library);

          // Set up library build files
          spawnWithLogger(api, 'android', [
              "update", "project", "--target", opts.target,
              "--path", library
              ], {}, f.wait());
        }
      }

      if (config.jars) {
        for (var ii = 0; ii < config.jars.length; ++ii) {
          var jar = path.join(modulePath, 'android', config.jars[ii]);

          logger.log("Installing module JAR:", jar);

          jars.push(jar);
        }
      }
    }

    fs.readFile(path.join(outputPath, "project.properties"), "utf-8", f());

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
            var outFile = path.join(outputPath, "src", pkgDir, path.basename(filePath));

            logger.log("Installing Java package", pkgName, "to", outFile);

            wrench.mkdirSyncRecursive(path.dirname(outFile));

            // Run injectionSource section of associated module
            var replacer = replacers[ii];
            if (replacer && replacer.length > 0) {
              for (var jj = 0; jj < replacer.length; ++jj) {
                var findString = replacer[jj].regex;
                var keyForReplace = replacer[jj].keyForReplace;
                var replaceString = app.manifest.android[keyForReplace];
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
              var outFile = path.join(outputPath, armeabi_out[jj], path.basename(filePath));
              logger.log("Writing shared object", filePath, "to", outFile);
              wrench.mkdirSyncRecursive(path.dirname(outFile));
              fs.writeFile(outFile, data, 'binary', f.wait());
            }
          } else {
            var outFile = path.join(outputPath, filePath);

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
      for (var i = 0; i < libraries.length; i ++) {
        var libProp = refStr + refNum + "=" + path.relative(outputPath, libraries[i]);

        logger.log("Installing library property:", libProp);

        libStr += libProp + "\n";
        refNum++;
      }

      properties += libStr;

      fs.writeFile(path.join(outputPath, "project.properties"), properties, "utf-8", f.wait());
    } else {
      logger.log("No library properties to add");
    }

    if (jars && jars.length > 0) {
      for (var ii = 0; ii < jars.length; ++ii) {
        var jarPath = jars[ii];
        var jarDestPath = path.join(outputPath, "libs", path.basename(jarPath));
        logger.log("Installing JAR file:", jarDestPath);
        try { fs.unlinkSync(jarDestPath); } catch (e) {}
        fs.symlinkSync(jarPath, jarDestPath, 'junction');
      }
    } else {
      logger.log("No JAR file data to install");
    }
  }).cb(cb);
}


//// Utilities

function nextStep() {
  var func = arguments[arguments.length - 1];
  return func(null);
}

function transformXSL(api, inFile, outFile, xslFile, params, cb) {
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

    api.jvmtools.exec({
      tool: 'xslt',
      args: [
        "--in", inFile,
        "--out", outFileTemp,
        "--stylesheet", xslFile,
        "--params", JSON.stringify(params)
        ]
      }, f());
  }, function (xslt) {
    var logger = api.logging.get('xslt');
    xslt.on('out', logger.out);
    xslt.on('err', logger.err);
    xslt.on('end', f.wait());
  }, function () {
    fs.readFile(outFileTemp, 'utf-8', f());
  }, function(dat) {
    dat = dat.replace(/android:label=\"[^\"]*\"/g, "android:label=\""+params.title+"\"");

    fs.writeFile(outFile, dat, 'utf-8', f.wait());
  }).cb(cb);
}

var PUNCTUATION_REGEX = /[!"#$%&'()*+,\-.\/:;<=>?@\[\\\]^_`{|}~]/g;
var PUNCTUATION_OR_SPACE_REGEX = /[!"#$%&'()*+,\-.\/:;<=>?@\[\\\]^_`{|}~ ]/g;

function buildSupportProjects(api, opts, cb) {
  var f = ff(this, function() {
    if (opts.clean || opts.arch) {
      spawnWithLogger(api, 'make', ['clean'], {cwd: __dirname}, f.slot());
    }
  }, function() {
    var args = ["-j", "8", (opts.debug ? "DEBUG=1" : "RELEASE=1")];
    if (opts.arch) {
      args.push('APP_ABI=' + opts.arch);
    }
    spawnWithLogger(api, 'ndk-build', args , { cwd: path.join(__dirname, "TeaLeaf") }, f.wait());
  }).cb(cb);
}

function saveLocalizedStringsXmls(outputPath, titles, cb) {
  var stringsXmlPath = path.join(outputPath, "res/values/strings.xml");
  var stringsXml = fs.readFileSync(stringsXmlPath, "utf-8");
  var f = ff(function () {
    Object.keys(titles).forEach(function (lang) {
      var title = titles[lang];
      var i = stringsXml.indexOf('</resources>');
      var first = stringsXml.substring(0, i);
      var second = stringsXml.substring(i);
      var inner = '<string name="title">' + title + '</string>';
      var finalXml = first + inner + second;
      //default en
      var next = f();
      if (lang === 'en') {
        fs.writeFile(path.join(outputPath, "res/values/strings.xml"), finalXml, 'utf-8', next);
      } else {
        mkdirp(path.join(outputPath, "res/values-" + lang), function (err) {
          if (err) { return next(err); }
          fs.writeFile(path.join(outputPath, "res/values-" + lang + "/strings.xml"), finalXml, 'utf-8', next);
        });
      }
    });
  }).cb(cb);
}

function makeAndroidProject(api, app, opts, cb) {
  var projectPropertiesFile = path.join(opts.outputPath, 'project.properties');
  var f = ff(function() {
    fs.unlink(projectPropertiesFile, f.waitPlain()); // ignore error if file doesn't exist
  }, function () {
    spawnWithLogger(api, 'android', [
      "create", "project", "--target", opts.target, "--name", opts.shortName,
      "--path", opts.outputPath, "--activity", opts.activity,
      "--package", opts.namespace
    ], {}, f());
  }, function() {
    var tealeafDir = path.relative(opts.outputPath, path.join(__dirname, "TeaLeaf"));
    spawnWithLogger(api, 'android', [
      "update", "project", "--target", opts.target,
      "--path", opts.outputPath,
      "--library", tealeafDir
    ], {}, f());
  }, function() {
    fs.appendFile(projectPropertiesFile, 'out.dexed.absolute.dir=../.dex/\nsource.dir=src\n',f());
  }, function() {
    var titles = opts.titles;
    if (titles) {
      if (titles.length == 0) {
        titles['en'] = opts.title;
      }
    }  else {
      titles = {};
      titles['en'] = opts.title;
    }
    saveLocalizedStringsXmls(opts.outputPath, titles, f.wait());
    updateManifest(api, app, opts, f.wait());
    updateActivity(opts, f.wait());
  }).cb(cb);
}

function signAPK(api, shortName, outputPath, debug, cb) {
  var signArgs, alignArgs;
  var binDir = path.join(outputPath, "bin");

  logger.log('Signing APK at ', binDir);
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
    var keystore = process.env['DEVKIT_ANDROID_KEYSTORE'];
    if (!keystore) { throw new Error('missing environment variable DEVKIT_ANDROID_KEYSTORE'); }

    var storepass = process.env['DEVKIT_ANDROID_STOREPASS'];
    if (!storepass) { throw new Error('missing environment variable DEVKIT_ANDROID_STOREPASS'); }

    var keypass = process.env['DEVKIT_ANDROID_KEYPASS'];
    if (!keypass) { throw new Error('missing environment variable DEVKIT_ANDROID_KEYPASS'); }

    var key = process.env['DEVKIT_ANDROID_KEY'];
    if (!key) { throw new Error('missing environment variable DEVKIT_ANDROID_KEY'); }

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
    spawnWithLogger(api, 'jarsigner', signArgs, {cwd: binDir}, f.wait());
  }, function () {
    spawnWithLogger(api, 'zipalign', alignArgs , {cwd: binDir}, f.wait());
  }).cb(cb);
}

function repackAPK(api, outputPath, apkName, cb) {
  var apkPath = path.join('bin', apkName);
  spawnWithLogger(api, 'zip', [apkPath, '-d', 'META-INF/*'], {cwd: outputPath}, function (err) {
    if (err) { return cb(err); }
    spawnWithLogger(api, 'zip', [apkPath, '-u'], {cwd: outputPath}, cb);
  });
};

var DEFAULT_ICON_PATH = {
  "36": "drawable-ldpi/icon.png",
  "48": "drawable-mdpi/icon.png",
  "72": "drawable-hdpi/icon.png",
  "96": "drawable-xhdpi/icon.png",
  "144": "drawable-xxhdpi/icon.png"
};

function copyIcons(app, outputPath) {
  copyIcon(app, outputPath, "l", "36");
  copyIcon(app, outputPath, "m", "48");
  copyIcon(app, outputPath, "h", "72");
  copyIcon(app, outputPath, "xh", "96");
  copyIcon(app, outputPath, "xxh", "144");
  copyIcon(app, outputPath, "xxxh", "192");
  copyNotifyIcon(app, outputPath, "l", "low");
  copyNotifyIcon(app, outputPath, "m", "med");
  copyNotifyIcon(app, outputPath, "h", "high");
  copyNotifyIcon(app, outputPath, "xh", "xhigh");
  copyNotifyIcon(app, outputPath, "xxh", "xxhigh");
}

function copyIcon(app, outputPath, tag, size) {
  var destPath = path.join(outputPath, "res/drawable-" + tag + "dpi/icon.png");
  var android = app.manifest.android;
  var iconPath = android && android.icons && android.icons[size];

  if (iconPath && fs.existsSync(iconPath)) {
    wrench.mkdirSyncRecursive(path.dirname(destPath));
    copyFileSync(iconPath, destPath);
  } else {
    logger.warn("No icon specified in the manifest for '", size, "'. Using the default icon for this size. This is probably not what you want");

    // Do not copy a default icon to this location -- Android will fill in
    // the blanks intelligently.
    //copyFileSync(path.join(__dirname, "TeaLeaf/res", DEFAULT_ICON_PATH[size]), destPath);
  }
}

var DEFAULT_NOTIFY_ICON_PATH = {
  "low": "drawable-ldpi/notifyicon.png",
  "med": "drawable-mdpi/notifyicon.png",
  "high": "drawable-hdpi/notifyicon.png",
  "xhigh": "drawable-xhdpi/notifyicon.png",
  "xxhigh": "drawable-xxhdpi/notifyicon.png"
};

function copyNotifyIcon(app, outputPath, tag, name) {
  var destPath = path.join(outputPath, "res/drawable-" + tag + "dpi/notifyicon.png");
  wrench.mkdirSyncRecursive(path.dirname(destPath));

  var android = app.manifest.android;
  var iconPath = android && android.icons && android.icons.alerts && android.icons.alerts[name];

  if (iconPath && fs.existsSync(iconPath)) {
    copyFileSync(iconPath, destPath);
  } else {
    logger.warn("No alert icon specified in the manifest for '", name, "'");

    // Do not copy a default icon to this location -- Android will fill in
    // the blanks intelligently.
    //copyFileSync(path.join(__dirname, "TeaLeaf/res", DEFAULT_NOTIFY_ICON_PATH[name]), destPath);
  }
}

function copySplash(api, app, outputDir, next) {
  var destPath = path.join(outputDir, "assets/resources");
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
/*        "portrait512",
        "portrait480"*/
      ],
      outFile: "splash-1024.png",
      outSize: "1024"
    },
    {
      copyFile: "portrait2048",
      inFiles: [
        "universal"
/*        "portrait1136",
        "portrait1024",
        "portrait960",
        "portrait512",
        "portrait480"*/
      ],
      outFile: "splash-2048.png",
      outSize: "2048"
    }
  ];

  var splashPaths = app.manifest.splash || {};

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

      // TODO: what's the default splash

      // // If no input file exists,
      // if (!srcFile) {
      //   srcFile = api.common.paths.lib("defsplash.png");

      //   if (!srcFile || !fs.existsSync(srcFile)) {
      //     logger.warn("Default splash file does not exist:", srcFile);
      //     srcFile = null;
      //   } else {
      //     logger.warn("No splash screen images provided for size", outSize, "so using default image", srcFile);
      //   }
      // }

      // If a source file was found,
      if (srcFile) {
        outFile = path.join(destPath, outFile);

        // If copying,
        if (copying) {
          logger.log("Copying size", outSize, "splash:", outFile, "from", srcFile);

          copyFileSync(srcFile, outFile);
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

      api.jvmtools.exec({
        tool: 'splasher',
        args: [
          "-i", task.srcFile,
          "-o", task.outFile,
          "-resize", task.outSize,
          "-rotate", "auto"
        ]
      }, function (err, splasher) {
        var logger = api.logging.get('splash' + task.outSize);
        splasher.on('out', logger.out);
        splasher.on('err', logger.err);
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

function copyMusic(app, outputDir, cb) {
  if (app.manifest.splash) {
    var destPath = path.join(outputDir, "res/raw");
    mkdirp(destPath, function () {
      var musicPath = app.manifest.splash.song;
      if (musicPath && fs.existsSync(musicPath)) {
        copyFileSync(musicPath, path.join(destPath, "loadingsound.mp3"));
      } else {
        logger.warn("No splash music specified in the manifest");
      }

      cb && cb();
    });
  }
}

function copyResDir(app, outputDir) {
  if (app.manifest.android && app.manifest.android.resDir) {
    var destPath = path.join(outputDir, "res");
    var sourcePath = path.resolve(app.manifest.android.resDir);
    try {
      wrench.copyDirSyncRecursive(sourcePath, destPath, {preserve: true});
    } catch (e) {
      logger.warn("Could not copy your android resource dir [" + e.toString() + "]");
    }
  }
}

function updateManifest(api, app, opts, cb) {
  var defaults = {
    // Empty defaults
    installShortcut: "false",

    entryPoint: "devkit.native.launchClient",
    studioName: opts.studioName,

    disableLogs: String(!opts.enableLogging),

    // Filled defaults
    // TODO: REMOVE ALL OF THESE FLAGS
    codeHost: "s.wee.cat",
    tcpHost: "s.wee.cat",
    codePort: "80",
    tcpPort: "4747",
    activePollTimeInSeconds: "10",
    passivePollTimeInSeconds: "20",
    syncPolling: "false",
    develop: String(opts.debug),
  };

  var f = ff(function() {
    versionCode(app, opts.debug, f());
  }, function(versionCode) {
    var orientations = app.manifest.supportedOrientations;
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

    var gameVersion = require(path.join(app.paths.root, 'package.json')).version;

    var params = {};
    f(params);
    copy(params, defaults);
    copy(params, app.manifest.android);
    copyAndFlatten(params, app.manifest.addons);
    copy(params, {
      "package": opts.namespace,
      title: "@string/title",
      activity: opts.namespace + "." + opts.activity,
      version: "" + opts.version,
      appid: opts.appID,
      shortname: opts.shortName,
      orientation: orientation,
      studioName: opts.studioName,
      gameHash: gameVersion,
      sdkHash: opts.sdkVersion,
      androidHash: androidVersion,
      versionCode: versionCode,
      debuggable: opts.debug ? 'true' : 'false'
    });

    wrench.mkdirSyncRecursive(opts.outputPath);

  }, function(params) {
    f(params);

    fs.readFile(path.join(__dirname, "TeaLeaf/AndroidManifest.xml"), "utf-8", f());
  }, function(params, xmlContent) {
    f(params);

    fs.writeFile(path.join(opts.outputPath, "AndroidManifest.xml"), xmlContent, "utf-8", f.wait());
  }, function(params) {
    f(params);

    injectPluginXML(opts, f());
  }, function(params) {
    f(params);

    var xmlPath = path.join(opts.outputPath, "AndroidManifest.xml");

    var xslPaths = [];

    for (var moduleName in opts.moduleConfig) {
      var module = opts.moduleConfig[moduleName];
      var config = module.config;
      if (config.injectionXSL) {
        var xslPath = path.join(module.path, 'android', config.injectionXSL);
        xslPaths.push(xslPath);
      }
    }

    // Run the plugin XSLT in series instead of parallel
    if (xslPaths.length > 0) {
      var allDone = f.wait();

      var runPluginXSLT = function(index) {
        if (index >= xslPaths.length) {
          allDone();
        } else {
          var xslPath = xslPaths[index];

          logger.log("Transforming XML with plugin XSL for", xslPath);

          transformXSL(api, xmlPath, xmlPath, xslPath, params, function (err) {
            if (err) {
              return allDone(err);
            }

            runPluginXSLT(index + 1);
          });
        }
      }

      runPluginXSLT(0);
    }
  }, function(params) {
    logger.log("Applying final XSL transformation");
    f(params);

    var xmlPath = path.join(opts.outputPath, "AndroidManifest.xml");
    transformXSL(api, xmlPath, xmlPath,
        path.join(__dirname, "AndroidManifest.xsl"),
        params, f());
  }).cb(cb);
}

function versionCode(app, debug, cb) {
  var versionPath = path.join(app.paths.root, '.version');

  var f = ff(this, function() {
    fs.exists(versionPath, f.slotPlain());
  }, function (exists) {
    if (!exists) {
      fs.writeFile(versionPath, '0', f.wait());
    }
  }, function() {
    //read the version
    fs.readFile(versionPath, f());
  }, function(readVersion) {
    var version = parseInt(readVersion, 10);

    if (isNaN(version)) {
      throw new Error(".version file seems incorrect. Make sure it's correctly formatted");
    }

    if (!debug) {
      ++version;
      fs.writeFile(versionPath, version, f.wait());
    }

    f(version);
  }).error(function(err) {
    if (!debug) {
      logger.error("Could not get version code");
    }
  }).cb(cb);
}

function updateActivity(opts, next) {
  var activityFile = path.join(opts.outputPath, "src/" + opts.namespace.replace(/\./g, "/") + "/" + opts.activity + ".java");

  if (fs.existsSync(activityFile)) {
    fs.readFile(activityFile, 'utf-8', function (err, contents) {
      contents = contents
        .replace(/extends Activity/g, "extends com.tealeaf.TeaLeaf")
        .replace(/setContentView\(R\.layout\.main\);/g, "startGame();");
      fs.writeFile(activityFile, contents, next);
    });
  }
}

exports.build = function(api, app, config, cb) {
  logger = api.logging.get('native-android');

  var argv = config.argv;

  // Extracted values from options.
  var packageName = config.packageName;

  // Extract manifest properties.
  var appID = app.manifest.appID;
  var shortName = app.manifest.shortName;
  // Verify they exist.
  if (appID === null || shortName === null) {
    throw new Error("Build aborted: No appID or shortName in the manifest");
  }

  appID = appID.replace(PUNCTUATION_REGEX, ""); // Strip punctuation.

  // app title
  var title = app.manifest.title;
  var titles = app.manifest.titles;
  if (title === null && titles === null) {
    title = shortName;
  }

  var studioName = app.manifest.studio && app.manifest.studio.name;

  // Create Android Activity name.
  var activity = shortName + "Activity";
  var androidTarget = "android-15";

  var apkPath;
  var moduleConfig;
  var apkBuildName = "";
  if (!config.debug) {
    apkBuildName = shortName + "-aligned.apk";
  } else {
    apkBuildName = shortName + "-debug.apk";
  }
  var f = ff(function () {
    if (!config.repack) {
      getModuleConfig(api, app, config, f());
    }
  }, function (_moduleConfig) {
    moduleConfig = _moduleConfig;

    if (!config.repack) {
      makeAndroidProject(api, app, {
        namespace: packageName,
        activity: activity,
        title: title,
        appID: appID,
        shortName: shortName,
        version: config.version,
        debug: config.debug,
        outputPath: config.outputPath,
        studioName: studioName,
        moduleConfig: moduleConfig,
        enableLogging: config.enableLogging,
        titles: titles,
        target: androidTarget,
        sdkVersion: config.sdkVersion
      }, f());

      // TODO: if build switches between release to debug, clean project
      // var cleanProj = (builder.common.config.get("lastBuildWasDebug") != config.debug) || config.clean;
      // builder.common.config.set("lastBuildWasDebug", config.debug);
      buildSupportProjects(api, {
        outputPath: config.outputPath,
        debug: config.debug,
        arch: argv.arch,
        clean: config.clean
      }, f());
    }
  }, function() {
    copyIcons(app, config.outputPath);
    copyMusic(app, config.outputPath, f());
    copyResDir(app, config.outputPath);
    copySplash(api, app, config.outputPath, f());

    if (!config.repack) {
      installModuleCode(api, app, {
        moduleConfig: moduleConfig,
        outputPath: config.outputPath,
        target: androidTarget
      }, f());
    }
  }, function () {
    if (!config.repack) {
      spawnWithLogger(api, 'ant', [(config.debug ? "debug" : "release")], {
          cwd: config.outputPath
        }, f());
    } else if(!argv.noapk) {
      repackAPK(api, config.outputPath, apkBuildName, f());
    }
  }, function () {
    if (!argv.noapk) {
      if (!config.debug || config.repack) {
        signAPK(api, shortName, config.outputPath, config.debug, f());
      }
    }
  }, function () {
    if (!argv.noapk) {
      apkPath = path.join(config.outputPath, shortName + ".apk");
      if (fs.existsSync(apkPath)) {
        fs.unlinkSync(apkPath);
      }
      var destApkPath = path.join(config.outputPath, "bin", apkBuildName);
      if (fs.existsSync(destApkPath)) {
        wrench.mkdirSyncRecursive(path.dirname(apkPath), 0777);
        copyFileSync(destApkPath, apkPath);
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
        spawnWithLogger(api, 'adb', argz, {}, f.waitPlain()); //this is waitPlain because it can fail and not break.
      }
    }
  }, function() {
    if (!argv.noapk) {
      if (argv.install || argv.open) {
        var cmd = 'adb install -r "' + apkPath + '"';
        logger.log('Install: Running ' + cmd + '...');
        spawnWithLogger(api, 'adb', ['install', '-r', apkPath], {}, f.waitPlain()); //this is waitPlain because it can fail and not break.
      }
    }
  }, function () {
    if (!argv.noapk) {
      if (argv.open) {
        var startCmd = packageName + '/' + packageName + '.' + shortName + 'Activity';
        var cmd = 'adb shell am start -n ' + startCmd;
        logger.log('Install: Running ' + cmd + '...');
        spawnWithLogger(api, 'adb', ['shell', 'am', 'start', '-n', startCmd], {}, f.waitPlain()); //this is waitPlain because it can fail and not break.
      }

      if (argv.reveal) {
        require('child_process').exec('open --reveal "' + apkPath + '"');
      }
    }

    f(config.outputPath);
  }).cb(cb);
};
