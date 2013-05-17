var fs = require('fs');
var wrench = require('wrench');
var path = require('path');

//copy file func
var copyFileSync = function(srcFile, destFile, encoding) {
	var content = fs.readFileSync(srcFile, encoding);
	fs.writeFileSync(destFile, content, encoding);
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


var TEALEAF_DIR = path.join(__dirname, "../TeaLeaf");
var PROP_START_PLUGINS = "#START_PLUGINS";
var PROP_END_PLUGINS = "#END_PLUGINS";

//read config
var config = JSON.parse(fs.readFileSync(__dirname + "/config.json"));

var libraries = [];
var jars = [];
var hasBilling = false;

for (var p in config) {

	var pluginDir = path.resolve(__dirname, config[p]);
	var pluginConfig = JSON.parse(fs.readFileSync(path.join(pluginDir, "config.json")));

	// Remove old Java plugins
	var tealeafPluginsPath = path.join(__dirname, "../TeaLeaf/src/com/tealeaf/plugin/plugins");
	wrench.rmdirSyncRecursive(tealeafPluginsPath, true);

	var copyFiles = pluginConfig.copyFiles;
	for (var i = 0; i < copyFiles.length; i++) {
		var fileInfo = copyFiles[i];
		var packageDir = fileInfo.packageName.replace(/\./g, "/");
		var destFilePath = path.join(__dirname, "../TeaLeaf/src/" ,packageDir ,fileInfo.name);
		wrench.mkdirSyncRecursive(path.dirname(destFilePath));
		copyFileSync(path.join(pluginDir, fileInfo.srcPath,  fileInfo.name), destFilePath, "utf-8")
	}

	// Remove old JavaScript plugins
	var timestepPluginsPath = path.join(__dirname, "../../../lib/timestep/src/platforms/native/addons");
	wrench.rmdirSyncRecursive(timestepPluginsPath, true);

	var copyJS = pluginConfig.copyJS;
	for (var i = 0; i < copyJS.length; ++i) {
		var fileInfo = copyJS[i];
		var destFilePath = path.join(__dirname, "../../../lib/timestep/src/platforms/native/addons", fileInfo.name);
		wrench.mkdirSyncRecursive(path.dirname(destFilePath));
		copyFileSync(path.join(pluginDir, fileInfo.srcPath,  fileInfo.name), destFilePath, "utf-8")
	}

	if (pluginConfig.hasBilling && pluginConfig.hasBilling == true) {
		hasBilling = true;
	}

	if (pluginConfig.library) {
			libraries.push(path.join(pluginDir, pluginConfig.library.srcPath, pluginConfig.library.libName));
	}

	if (pluginConfig.jars) {
		for (var i = 0; i < pluginConfig.jars.length; i++) {
			jars.push(path.join(pluginDir, pluginConfig.jars[i]));
		}
	}

}

//go through and all library references to project.properties
var properties = fs.readFileSync(path.join(TEALEAF_DIR, "project.properties"), "utf-8");
if (properties.length > 0 ) {
	var sourceDirs = "src";
	if (hasBilling) {
		sourceDirs = "src/com/tealeaf";
	}
	properties = properties.replace(/source\.dir([^\n]*)/, 'source.dir=' + sourceDirs);
	var start = properties.indexOf(PROP_START_PLUGINS);		
	var end = properties.indexOf(PROP_END_PLUGINS);		

	//find largest uncommented library reference number
	var i = 0;
	var refStr = "android.library.reference.";
	var refNum = 1;
	while (true) {
		var offset = properties.substring(i).indexOf("android.library.reference.");
		i = offset + i;
		if (offset == -1) {
			break;
		}
		if (i > start) {
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
		libStr += refStr + refNum + "=" + path.relative(path.join(__dirname, "../TeaLeaf"), libraries[i]) + "\n";
		refNum++;
	}
	properties = replaceTextBetween(properties, PROP_START_PLUGINS, PROP_END_PLUGINS, libStr);
	fs.writeFileSync(path.join(TEALEAF_DIR, "project.properties"), properties, "utf-8");

	//copy libary jars
	for (var i = 0; i < jars.length; i++) {
		var jarName = path.basename(jars[i]);
		var copyPath = path.join(__dirname, '..', 'TeaLeaf', 'libs', jarName);
		fs.createReadStream(jars[i]).pipe(fs.createWriteStream(copyPath));
	}
}

