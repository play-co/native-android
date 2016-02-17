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

var util = require('util');
var path = require('path');
var spawn = require('child_process').spawn;
var Promise = require('bluebird');
var fs = Promise.promisifyAll(require('fs-extra'));
var chalk = require('chalk');

// unexpected exceptions show stack, build errors should just show the error
// message to the user
var BuildError = function (message, showStack) {
  this.message = chalk.red(message);
  this.showStack = showStack || false;
};

util.inherits(BuildError, Error);

exports.BuildError = BuildError;

var existsAsync = function (filename) {
  return new Promise(function (resolve) {
    if (!filename) { return resolve(false); }

    fs.exists(filename, function (exists) {
      resolve(exists);
    });
  });
};

// used to remove punctuation (if any) from the appid
var PUNCTUATION_REGEX = /[!"#$%&'()*+,\-.\/:;<=>?@\[\\\]^_`{|}~]/g;

var ANDROID_TARGET = "android-19";
var androidVersion = require('./package.json').version;

var logger;

function spawnWithLogger(api, name, args, opts) {
  return new Promise(function (resolve, reject) {
    var logger = api.logging.get(name);
    logger.log(chalk.green(name + ' ' + args.join(' ')));
    var streams = logger.createStreams(['stdout'], false);
    var child = spawn(name, args, opts);
    child.stdout.pipe(streams.stdout);
    child.stderr.pipe(streams.stdout);
    child.on('close', function (code) {
      if (code) {
        var err = new BuildError(chalk.green(name) + chalk.red(' exited with non-zero exit code (' + code + ')'));
        err.stdout = streams.get('stdout');
        err.code = code;
        reject(err);
      } else if (opts && opts.capture) {
        resolve(streams.get('stdout'));
      } else {
        resolve();
      }
    });
  });
}

function legacySpawnWithLogger(api, name, args, opts) {
  var logger = api.logging.get(name);
  logger.log(name, args.join(' '));
  var child = spawn(name, args, opts);
  child.stdout.pipe(logger, {end: false});
  child.stderr.pipe(logger, {end: false});

  var stdout = '';
  if (opts && opts.capture) {
    child.stdout.on('data', function (chunk) {
      stdout += chunk;
    });
  }

  return new Promise(function (resolve, reject) {
    child.on('close', function (err) {
      if (err) {
        reject(err);
      } else {
        resolve(stdout);
      }
    });
  });
}

//// Modules

var getModuleConfig = function(api, app) {
  var moduleConfig = {};
  return Promise.map(Object.keys(app.modules), function (moduleName) {
      var modulePath = app.modules[moduleName].path;
      var configFile = path.join(modulePath, 'android', 'config.json');
      return fs.readFileAsync(configFile, 'utf8')
        .then(function (data) {
          moduleConfig[moduleName] = {
            config: JSON.parse(data),
            path: modulePath
          };
        }, function (err) {
          // modules are not required to have a config.json, ignore missing file
          if (err && err.code !== 'ENOENT') {
            throw err;
          }
        });
    })
    .return(moduleConfig);
};

var getTextBetween = function(text, startToken, endToken) {
  var start = text.indexOf(startToken);
  var end = text.indexOf(endToken);
  if (start == -1 || end == -1) {
    return "";
  }
  var offset = text.substring(start).indexOf("\n") + 1;
  var afterStart = start + offset;
  return text.substring(afterStart, end);
};

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
};

function injectPluginXML(opts) {
  var moduleConfig = opts.moduleConfig;
  var outputPath = opts.outputPath;
  var manifestXml = path.join(outputPath, 'AndroidManifest.xml');

  var readPluginXMLFiles = Object.keys(moduleConfig).map(function (moduleName) {
    var injectionXML = moduleConfig[moduleName].config.injectionXML;

    if (injectionXML) {
      var filepath = path.join(moduleConfig[moduleName].path, 'android', injectionXML);
      logger.log('Reading plugin XML:', filepath);

      return fs.readFileAsync(filepath, 'utf-8');
    }
  });

  return Promise.all([
      fs.readFileAsync(manifestXml, 'utf-8')
    ].concat(readPluginXMLFiles))
    .then(function (results) {
      var xml = results.shift();

      // TODO: don't use regular expressions

      if (results && results.length > 0 && xml && xml.length > 0) {
        var XML_START_PLUGINS_MANIFEST = '<!--START_PLUGINS_MANIFEST-->';
        var XML_END_PLUGINS_MANIFEST = '<!--END_PLUGINS_MANIFEST-->';
        var XML_START_PLUGINS_ACTIVITY = '<!--START_PLUGINS_ACTIVITY-->';
        var XML_END_PLUGINS_ACTIVITY = '<!--END_PLUGINS_ACTIVITY-->';
        var XML_START_PLUGINS_APPLICATION = '<!--START_PLUGINS_APPLICATION-->';
        var XML_END_PLUGINS_APPLICATION = '<!--END_PLUGINS_APPLICATION-->';

        var manifestXmlManifestStr = '';
        var manifestXmlActivityStr = '';
        var manifestXmlApplicationStr = '';

        for (var i = 0; i < results.length; ++i) {
          var pluginXml = results[i];
          if (!pluginXml) { continue; }

          manifestXmlManifestStr += getTextBetween(pluginXml, XML_START_PLUGINS_MANIFEST, XML_END_PLUGINS_MANIFEST);
          manifestXmlActivityStr += getTextBetween(pluginXml, XML_START_PLUGINS_ACTIVITY, XML_END_PLUGINS_ACTIVITY);
          manifestXmlApplicationStr += getTextBetween(pluginXml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION);
        }

        xml = replaceTextBetween(xml, XML_START_PLUGINS_MANIFEST, XML_END_PLUGINS_MANIFEST, manifestXmlManifestStr);
        xml = replaceTextBetween(xml, XML_START_PLUGINS_ACTIVITY, XML_END_PLUGINS_ACTIVITY, manifestXmlActivityStr);
        xml = replaceTextBetween(xml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION, manifestXmlApplicationStr);
        return fs.writeFileAsync(manifestXml, xml, 'utf-8');
      } else {
        logger.log('No plugin XML to inject');
      }
    });
}

var installModuleCode = function (api, app, opts) {
  var moduleConfig = opts.moduleConfig;
  var outputPath = opts.outputPath;

  function handleFile(baseDir, filePath, replacer) {
    var ext = path.extname(filePath);
    if (ext == '.java' || ext === '.aidl') {
      return fs.readFileAsync(path.join(baseDir, filePath), 'utf-8')
        .then(function (contents) {
          var pkgName = contents.match(/(package[\s]+)([a-z.A-Z0-9]+)/g)[0].split(' ')[1];
          var pkgDir = pkgName.replace(/\./g, "/");
          var outFile = path.join(outputPath, "src", pkgDir, path.basename(filePath));

          logger.log("Installing Java package", pkgName, "to", outFile);

          // Run injectionSource section of associated module
          if (replacer && replacer.length > 0) {
            for (var jj = 0; jj < replacer.length; ++jj) {
              var findString = replacer[jj].regex;
              var keyForReplace = replacer[jj].keyForReplace;
              var replaceString = app.manifest.android[keyForReplace];
              if (replaceString) {
                logger.log(" - Running find-replace for", findString, "->", replaceString, "(android:", keyForReplace + ")");
                var rexp = new RegExp(findString, "g");
                contents = contents.replace(rexp, replaceString);
              } else {
                logger.error(" - Unable to find android key for", keyForReplace);
              }
            }
          }

          return fs.outputFileAsync(outFile, contents, 'utf-8');
        });
    } else if (ext == '.so') {
      var src = path.join(baseDir, filePath);
      var basename = path.basename(filePath);
      return Promise.all([
        fs.copyAsync(src, path.join(outputPath, 'libs', 'armeabi', basename)),
        fs.copyAsync(src, path.join(outputPath, 'libs', 'armeabi-v7a', basename))
      ]);
    } else {
      return fs.copyAsync(path.join(baseDir, filePath), path.join(outputPath, filePath));
    }
  }

  function installLibraries(libraries) {
    if (!libraries || !libraries.length) { return; }

    var projectPropertiesFile = path.join(outputPath, "project.properties");

    var libraryFiles = libraries.map(function (library) {
      return path.join(modulePath, 'android', library);
    });

    return Promise.map(libraryFiles, function (libraryFile) {
        // Set up library build files
        logger.log("Installing module library:", libraryFile);
        return spawnWithLogger(api, 'android', [
            "update", "project", "--target", ANDROID_TARGET,
            "--path", libraryFile
          ]);
      })
      .then(function () {
        return fs.readFileAsync(projectPropertiesFile, "utf-8");
      })
      .then(function (properties) {
        // find the largest library reference number
        var libraryNumber = 1;
        properties.replace(/^[^#]+android\.library\.reference\.(\d+).*$/gm, function (line, ref) {
          // for each (uncommented) line with a library reference
          ref = parseInt(ref);
          if (ref >= libraryNumber) {
            libraryNumber = ref + 1;
          }
        });

        var libraryReferences = '\n' + libraryFiles.map(function (library) {
            var key = 'android.library.reference.' + (libraryNumber++);
            var value = path.relative(outputPath, library);
            return key + '=' + value;
          })
          .join('\n');

        return fs.appendFileAsync(projectPropertiesFile, libraryReferences);
      });
  }

  function installJar(jarFile) {
    logger.log("Installing module JAR:", jarFile);
    var jarDestPath = path.join(outputPath, "libs", path.basename(jarFile));

    logger.log("Installing JAR file:", jarDestPath);
    return fs.unlinkAsync(jarDestPath)
      .catch(function () {})
      .then(function () {
        return fs.symlinkAsync(jarFile, jarDestPath, 'junction');
      });
  }

  var tasks = [];
  for (var moduleName in moduleConfig) {
    var config = moduleConfig[moduleName].config;
    var modulePath = moduleConfig[moduleName].path;

    config.copyFiles && config.copyFiles.forEach(function (filename) {
      tasks.push(handleFile(path.join(modulePath, 'android'), filename, config.injectionSource));
    });

    config.copyGameFiles && config.copyGameFiles.forEach(function (filename) {
      tasks.push(handleFile(app.paths.root, filename, config.injectionSource));
    });

    config.jars && config.jars.forEach(function (jar) {
      tasks.push(installJar(path.join(modulePath, 'android', jar)));
    });

    tasks.push(installLibraries(config.libraries));
  }

  return Promise.all(tasks);
};


//// Utilities

function transformXSL(api, inFile, outFile, xslFile, params) {
  for (var key in params) {
    if (typeof params[key] !== 'string') {
      if (!params[key] || typeof params[key] === 'object') {
        logger.error("settings for AndroidManifest: value for", chalk.yellow(key), "is not a string");
      }

      params[key] = JSON.stringify(params[key]);
    }
  }

  var outFileTemp = outFile + ".temp";
  return new Promise(function (resolve, reject) {
      api.jvmtools.exec({
        tool: 'xslt',
        args: [
          "--in", inFile,
          "--out", outFileTemp,
          "--stylesheet", xslFile,
          "--params", JSON.stringify(params)
          ]
        }, function (err, xslt) {
          if (err) { return reject(err); }

          var logger = api.logging.get('xslt');
          xslt.on('out', logger.out);
          xslt.on('err', logger.err);
          xslt.on('end', resolve);
        });
    })
    .then(function () {
      return fs.readFileAsync(outFileTemp, 'utf-8');
    })
    .then(function(contents) {
      contents = contents.replace(/android:label=\"[^\"]*\"/g, "android:label=\""+params.title+"\"");
      fs.writeFileAsync(outFile, contents, 'utf-8');
    });
}

function buildSupportProjects(api, config) {
  var rootDir = __dirname;
  var tealeafDir = path.join(__dirname, 'TeaLeaf');
  var singleArch = config.argv.arch;
  return Promise.try(function () {
      if (config.clean || singleArch) {
        return spawnWithLogger(api, 'make', ['clean'], {cwd: rootDir});
      }
    })
    .then(function () {
      var args = ["-j", "8", (config.debug ? "DEBUG=1" : "RELEASE=1")];
      if (singleArch) {
        args.push('APP_ABI=' + singleArch);
      }

      return spawnWithLogger(api, 'ndk-build', args , {cwd: tealeafDir});
    });
}

function saveLocalizedStringsXmls(outputPath, titles) {
  var stringsXmlPath = path.join(outputPath, "res/values/strings.xml");
  var stringsXml = fs.readFileSync(stringsXmlPath, "utf-8");
  return Promise.map(Object.keys(titles), function (lang) {
      var title = titles[lang];
      var i = stringsXml.indexOf('</resources>');
      var first = stringsXml.substring(0, i);
      var second = stringsXml.substring(i);
      var inner = '<string name="title">' + title + '</string>';
      var finalXml = first + inner + second;
      var values = lang == 'en' ? 'values' : 'values-' + lang;
      var stringsFile = path.join(outputPath, 'res', values, 'strings.xml');
      return fs.outputFileAsync(stringsFile, finalXml, 'utf-8');
    });
}

function makeAndroidProject(api, app, config, opts) {
  var projectPropertiesFile = path.join(opts.outputPath, 'project.properties');
  return fs.unlinkAsync(projectPropertiesFile)
    .catch(function () {}) // ignore error if file doesn't exist
    .then(function () {
      return spawnWithLogger(api, 'android', [
          "create", "project", "--target", ANDROID_TARGET, "--name", app.manifest.shortName,
          "--path", opts.outputPath, "--activity", config.activityName,
          "--package", config.packageName
        ])
        .catch(BuildError, function (err) {
          if (err.stdout && /not valid/i.test(err.stdout)) {
            logger.log(chalk.yellow([
                '',
                'Android target ' + ANDROID_TARGET + ' was not available. Please ensure',
                'you have installed the Android SDK properly, and use the',
                '"android" tool to install API Level ' + ANDROID_TARGET.split('-')[1] + '.',
                ''
              ].join('\n')));
          }

          if (err.stdout && /no such file/i.test(err.stdout) || err.code == 126) {
            logger.log(chalk.yellow([
                '',
                'You must install the Android SDK first. Please ensure the ',
                '"android" tool is available from the command line by adding',
                'the sdk\'s "tools/" directory to your system path.',
                ''
              ].join('\n')));
          }

          throw err;
        });
    })
    .then(function () {
      var tealeafDir = path.relative(opts.outputPath, path.join(__dirname, "TeaLeaf"));
      return spawnWithLogger(api, 'android', [
          "update", "project", "--target", ANDROID_TARGET,
          "--path", opts.outputPath,
          "--library", tealeafDir
        ]);
    })
    .then(function () {
      var dexDir = '\nout.dexed.absolute.dir=../.dex/\nsource.dir=src\n';
      return [
        fs.appendFileAsync(projectPropertiesFile, dexDir),
        saveLocalizedStringsXmls(opts.outputPath, config.titles),
        updateManifest(api, app, config, opts),
        updateActivity(config, opts)
      ];
    })
    .all();
}

function signAPK(api, shortName, outputPath, debug) {
  var signArgs, alignArgs;
  var binDir = path.join(outputPath, "bin");

  logger.log('Signing APK at', binDir);
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
    if (!keystore) { throw new BuildError('missing environment variable DEVKIT_ANDROID_KEYSTORE'); }

    var storepass = process.env['DEVKIT_ANDROID_STOREPASS'];
    if (!storepass) { throw new BuildError('missing environment variable DEVKIT_ANDROID_STOREPASS'); }

    var keypass = process.env['DEVKIT_ANDROID_KEYPASS'];
    if (!keypass) { throw new BuildError('missing environment variable DEVKIT_ANDROID_KEYPASS'); }

    var key = process.env['DEVKIT_ANDROID_KEY'];
    if (!key) { throw new BuildError('missing environment variable DEVKIT_ANDROID_KEY'); }

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

  return spawnWithLogger(api, 'jarsigner', signArgs, {cwd: binDir})
    .then(function () {
      return spawnWithLogger(api, 'zipalign', alignArgs , {cwd: binDir});
    });
}

function repackAPK(api, outputPath, apkName, cb) {
  var apkPath = path.join('bin', apkName);
  spawnWithLogger(api, 'zip', [apkPath, '-d', 'META-INF/*'], {cwd: outputPath}, function (err) {
    if (err) { return cb(err); }
    spawnWithLogger(api, 'zip', [apkPath, '-u'], {cwd: outputPath}, cb);
  });
}

function copyIcons(app, outputPath) {
  return Promise.all([
      copyIcon(app, outputPath, "l", "36"),
      copyIcon(app, outputPath, "m", "48"),
      copyIcon(app, outputPath, "h", "72"),
      copyIcon(app, outputPath, "xh", "96"),
      copyIcon(app, outputPath, "xxh", "144"),
      copyIcon(app, outputPath, "xxxh", "192"),
      copyNotifyIcon(app, outputPath, "l", "low"),
      copyNotifyIcon(app, outputPath, "m", "med"),
      copyNotifyIcon(app, outputPath, "h", "high"),
      copyNotifyIcon(app, outputPath, "xh", "xhigh"),
      copyNotifyIcon(app, outputPath, "xxh", "xxhigh"),
      copyNotifyIcon(app, outputPath, "xxxh", "xxxhigh")
    ]);
}

function copyIcon(app, outputPath, tag, size) {
  var destPath = path.join(outputPath, "res/drawable-" + tag + "dpi/icon.png");
  var android = app.manifest.android;
  var iconPath = android.icons && android.icons[size];

  if (iconPath) {
    iconPath = path.resolve(app.paths.root, iconPath);
    return fs.copyAsync(iconPath, destPath);
  }

  logger.warn("No icon specified in the manifest for size '" + size + "'. Using the default icon for this size. This is probably not what you want.");
}

function copyNotifyIcon(app, outputPath, tag, name) {
  var destPath = path.join(outputPath, "res/drawable-" + tag + "dpi/notifyicon.png");
  var android = app.manifest.android;
  var iconPath = android.icons && android.icons.alerts && android.icons.alerts[name];

  if (iconPath) {
    return fs.copyAsync(iconPath, destPath);
  } else {
    // Do not copy a default icon to this location -- Android will fill in
    // the blanks intelligently.
    logger.warn("No alert icon specified in the manifest for density '" + name + "'");
  }
}

var SPLASH_FILES = [
  'portrait480',
  'portrait960',
  'portrait1024',
  'portrait1136',
  'portrait2048',
  'landscape768',
  'landscape1536',
  'universal'
];

var DEFAULT_SPLASH_CONFIG = {};
SPLASH_FILES.forEach(function (key) {
  DEFAULT_SPLASH_CONFIG[key] = "resources/splash/" + key + ".png";
});

function copySplash(api, app, outputDir) {
  var splashPaths = app.manifest.android.splash || app.manifest.splash || DEFAULT_SPLASH_CONFIG;
  var destPath = path.join(outputDir, 'assets/resources');
  return fs.mkdirsAsync(destPath)
    .then(function () {
      return SPLASH_FILES.map(function (key) {
        var filename = splashPaths[key];
        if (!filename) { return false; }

        return existsAsync(filename)
          .then(function (exists) {
            if (!exists) {
              logger.error('Splash file (manifest.splash.' + key + ') does not',
                'exist (' + filename + ')');
            }

            return {
              key: key,
              filename: filename,
              exists: exists
            };
          });
      });
    })
    // remove files that don't exist
    .filter(function (splash) { return splash && splash.exists; })
    .map(function (splash) {
      var filename = 'splash-' + splash.key + '.png';
      var destFile = path.join(destPath, filename);
      logger.log('Copying', splash.filename, 'to "assets/resources/' + filename + '"');
      return fs.copyAsync(splash.filename, destFile);
    });
}

function copyMusic(app, outputDir) {
  if (app.manifest.splash) {
    var musicPath = app.manifest.splash.song;
    var destPath = path.join(outputDir, "res/raw", "loadingsound.mp3");
    return existsAsync(musicPath)
      .then(function (exists) {
        if (!exists) {
          logger.warn('No valid splash music specified in the manifest (at "splash.song")');
        } else {
          return fs.copyAsync(musicPath, destPath);
        }
      });
  }
}

function copyResDir(app, outputDir) {
  if (app.manifest.android.resDir) {
    var destPath = path.join(outputDir, "res");
    var sourcePath = path.resolve(app.manifest.android.resDir);
    return fs.copyAsync(sourcePath, destPath, {preserveTimestamps: true})
      .catch(function (e) {
        logger.warn("Could not copy your android resource dir [" + e.toString() + "]");
        throw e;
      });
  }
}

function updateManifest(api, app, config, opts) {
  var params = {
    // Empty defaults
    installShortcut: "false",

    entryPoint: "devkit.native.launchClient",
    studioName: config.studioName,

    disableLogs: config.debug ? 'false' : 'true',

    // Filled defaults
    // TODO: REMOVE ALL OF THESE FLAGS
    codeHost: "s.wee.cat",
    tcpHost: "s.wee.cat",
    codePort: "80",
    tcpPort: "4747",
    activePollTimeInSeconds: "10",
    passivePollTimeInSeconds: "20",
    syncPolling: "false",
    develop: config.debug ? 'true' : 'false',
  };

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

  copy(params, app.manifest.android);
  copyAndFlatten(params, app.manifest.modules || app.manifest.addons);
  copy(params, {
    "package": config.packageName,
    title: "@string/title",
    activity: config.packageName + "." + config.activityName,
    version: "" + config.version,
    appid: app.manifest.appID.replace(PUNCTUATION_REGEX, ""), // Strip punctuation.,
    shortname: app.manifest.shortName,
    fullscreen: app.manifest.android.fullscreen,
    orientation: orientation,
    studioName: config.studioName,
    gameHash: app.manifest.version,
    sdkHash: config.sdkVersion,
    androidHash: androidVersion,
    minSdkVersion: config.argv['min-sdk-version'] || 8,
    targetSdkVersion: config.argv['target-sdk-version'] || 14,
    debuggable: config.debug ? 'true' : 'false'
  });

  var defaultManifest = path.join(__dirname, "TeaLeaf/AndroidManifest.xml");
  var outputManifest = path.join(opts.outputPath, "AndroidManifest.xml");
  Promise.all([
      fs.copyAsync(defaultManifest, outputManifest),
      getVersionCode(app, config.debug)
        .then(function(versionCode) {
          params.versionCode = versionCode;
        })
    ])
    .then(function () {
      return injectPluginXML(opts);
    })
    .then(function () {
      return Object.keys(opts.moduleConfig);
    })
    .map(function (moduleName) {
      var module = opts.moduleConfig[moduleName];
      var config = module.config;
      if (config.injectionXSL) {
        var xslPath = path.join(module.path, 'android', config.injectionXSL);
        return transformXSL(api, outputManifest, outputManifest, xslPath, params);
      }
    }, {concurrency: 1}) // Run the plugin XSLT in series instead of parallel
    .then(function() {
      logger.log("Applying final XSL transformation");
      var xmlPath = path.join(opts.outputPath, "AndroidManifest.xml");
      return transformXSL(api, xmlPath, xmlPath,
          path.join(__dirname, "AndroidManifest.xsl"),
          params);
    });
}

function getVersionCode(app, debug) {
  var versionPath = path.join(app.paths.root, '.version');
  var versionCode = '0'; // debug version is code 0
  return fs.readFileAsync(versionPath)
    .catch(function (err) {
      if (err && err.code == 'ENOENT') {
        var contents = '0';
        return fs.writeFileAsync(versionPath, contents)
          .return(contents);
      } else {
        throw err;
      }
    })
    .then(function (contents) {
      var version = parseInt(contents, 10);
      if (isNaN(version)) {
        throw new BuildError('Invalid ".version" file. It must contain a single integer.');
      }

      if (!debug) {
        ++version;
        versionCode = '' + version;
        logger.log(chalk.yellow('** release versionCode set to ' + versionCode));
        return fs.writeFileAsync(versionPath, versionCode);
      }
    })
    .catch(function (err) {
      if (!debug) {
        // version code only needed for release builds, ignore errors in debug
        throw err;
      }
    })
    .then(function () {
      return versionCode;
    });
}

function updateActivity(config, opts) {
  var activityFile = path.join(opts.outputPath,
      "src",
      config.packageName.replace(/\./g, "/"),
      config.activityName + ".java");

  return fs.readFileAsync(activityFile, 'utf-8')
    .then(function (contents) {
      contents = contents
        .replace(/extends Activity/g, "extends com.tealeaf.TeaLeaf")
        .replace(/setContentView\(R\.layout\.main\);/g, "startGame();");
      return fs.writeFileAsync(activityFile, contents);
    });
}

function createProject(api, app, config) {

  var tasks = [];
  tasks.push(getModuleConfig(api, app)
    .then(function (moduleConfig) {
      return makeAndroidProject(api, app, config, {
          outputPath: config.outputPath,
          moduleConfig: moduleConfig
        })
        .return(moduleConfig);
    })
    .then(function (moduleConfig) {
      return installModuleCode(api, app, {
          moduleConfig: moduleConfig,
          outputPath: config.outputPath
        });
    }));

  // TODO: if build switches between release to debug, clean project
  // var cleanProj = (builder.common.config.get("lastBuildWasDebug") != config.debug) || config.clean;
  // builder.common.config.set("lastBuildWasDebug", config.debug);

  tasks.push(buildSupportProjects(api, config));

  return Promise.all(tasks);
}

exports.build = function(api, app, config, cb) {
  logger = api.logging.get('android');

  var sdkVersion = parseFloat(config.sdkVersion);
  if (isNaN(sdkVersion) || sdkVersion < 3.1) {
    spawnWithLogger = legacySpawnWithLogger;
  }

  var argv = config.argv;

  var skipAPK = argv.apk === false;
  var skipSigning = skipAPK || argv.signing === false || config.debug;

  var shortName = app.manifest.shortName;
  if (shortName === null) {
    throw new BuildError("Build aborted: No shortName in the manifest");
  }

  var apkBuildName = "";
  if (!config.debug) {
    if (skipSigning) {
      apkBuildName = shortName + "-release-unsigned.apk";
    } else {
      apkBuildName = shortName + "-aligned.apk";
    }
  } else {
    apkBuildName = shortName + "-debug.apk";
  }

  if (!app.manifest.android) {
    logger.warn('you should add an "android" key to your app\'s manifest.json',
                'for android-specific settings');
    app.manifest.android = {};
  }

  return Promise.try(function createAndroidProjectFiles() {
      if (!config.repack) {
        return createProject(api, app, config);
      }
    })
    .then(function copyResourcesToProject() {
      return [
        copyIcons(app, config.outputPath),
        copyMusic(app, config.outputPath),
        copyResDir(app, config.outputPath),
        copySplash(api, app, config.outputPath)
      ];
    })
    .all()
    .then(function buildAPK() {
      if (!skipAPK) {
        if (config.repack) {
          return repackAPK(api, config.outputPath, apkBuildName);
        } else {
          var antScheme = (config.debug ? "debug" : "release");
          return spawnWithLogger(api, 'ant', [antScheme], {
              cwd: config.outputPath
            });
        }
      }
    })
    .then(function () {
      if (!skipSigning) {
        return signAPK(api, shortName, config.outputPath, config.debug);
      }
    })
    .then(function () {
      if (!skipAPK) {
        return moveAPK(api, app, config, apkBuildName)
          .tap(function (apkPath) {
            logger.log("built", chalk.yellow(config.packageName));
            logger.log("saved to " + chalk.blue(apkPath));
          })
          .then(function (apkPath) {
            if (argv.reveal) {
              require('child_process').exec('open --reveal "' + apkPath + '"');
            }

            if (argv.install || argv.open) {
              return installAPK(api, config, apkPath, {
                open: !!argv.open,
                clearStorage: argv.clearStorage
              });
            }
          });
      }
    })
    .nodeify(cb);
};

function moveAPK(api, app, config, apkBuildName) {
  var shortName = app.manifest.shortName;
  var apkPath = path.join(config.outputPath, shortName + ".apk");
  var destApkPath = path.join(config.outputPath, "bin", apkBuildName);
  return Promise.all([
      existsAsync(destApkPath),
      fs.unlinkAsync(apkPath)
        .catch(function () {}) // ignore if it didn't exist
    ])
    .spread(function (exists) {
      if (exists) {
        return fs.copyAsync(destApkPath, apkPath);
      } else {
        throw new BuildError("apk failed to build (missing " + destApkPath + ")");
      }
    })
    .return(destApkPath);
}

function installAPK(api, config, apkPath, opts) {
  var packageName = config.packageName;
  var activityName = config.activityName;

  function getDevices() {
    return spawnWithLogger(api, 'adb', ['devices'], {capture: true})
      .then(function (res) {
        return res.split('\n')
          .map(function (line) {
            return line.match(/^([0-9a-z]+)\s+(device|emulator)$/i);
          })
          .filter(function (match) { return match; })
          .map(function (match) {
            return match[1];
          });
      });
  }

  function tryUninstall(device) {
    var args = ['-s', device, 'shell', 'pm', 'uninstall'];
    if (opts.clearstorage) {
      args.push('-k');
    }
    args.push(packageName);

    return spawnWithLogger(api, 'adb', args, {})
      .catch (function () {
        // ignore uninstall errors
      });
  }

  function tryInstall(device) {
    return spawnWithLogger(api, 'adb', ['-s', device, 'install', '-r', apkPath])
      .catch (function () {
        // ignore install errors
      });
  }

  function tryOpen(device) {
    var startCmd = packageName + '/' + packageName + '.' + activityName;
    return spawnWithLogger(api, 'adb', ['-s', device, 'shell', 'am', 'start', '-n', startCmd], {})
        .catch (function () {
          // ignore open errors
        });
  }

  return Promise.try(function () {
      return getDevices();
    })
    .tap(function (devices) {
      if (!devices.length) {
        logger.error('tried to install to device, but no devices found');
      }
    })
    .map(function(device) {
      return tryUninstall(device)
        .then(function () {
          return tryInstall(device);
        })
        .then(function () {
          if (opts.open) {
            return tryOpen(device);
          }
        });
    });
}
