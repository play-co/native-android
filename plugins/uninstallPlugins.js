var fs = require('fs');
var path = require('path');

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
var XML_START_PLUGINS_MANIFEST = "<!--START_PLUGINS_MANIFEST-->";
var XML_END_PLUGINS_MANIFEST = "<!--END_PLUGINS_MANIFEST-->";
var XML_START_PLUGINS_APPLICATION = "<!--START_PLUGINS_APPLICATION-->";
var XML_END_PLUGINS_APPLICATION = "<!--END_PLUGINS_APPLICATION-->";

//read config
var config = JSON.parse(fs.readFileSync(__dirname + "/config.json"));

//set up blank strs for injection of xml

for (var i in config) {

	var pluginDir = config[i];
	var pluginConfig = JSON.parse(fs.readFileSync(path.join(__dirname, pluginDir, "/config.json")));

	var copyFiles = pluginConfig.copyFiles;
	for (var cf in copyFiles) {
		var fileInfo = copyFiles[cf];
		var packageDir = fileInfo.packageName.replace(/\./g, "/");
		var destFilePath = path.join(__dirname, "../TeaLeaf/src/" ,packageDir ,fileInfo.name);
		fs.unlink(destFilePath, function(err) {});

	}

}

var xml = fs.readFileSync(path.join(__dirname, "../GCTestApp/AndroidManifest.xml"), "utf-8");
if (xml.length > 0) {
	xml = replaceTextBetween(xml, XML_START_PLUGINS_MANIFEST, XML_END_PLUGINS_MANIFEST, "");
	xml = replaceTextBetween(xml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION, "");
	fs.writeFileSync(path.join(__dirname, "../GCTestApp/AndroidManifest.xml"), xml, "utf-8");
}

//go through and all library references to project.properties
var properties = fs.readFileSync(path.join(TEALEAF_DIR, "project.properties"), "utf-8");
if (properties.length > 0 ) {
	var start = properties.indexOf("#START_PLUGINS");		
	var end = properties.indexOf("#END_PLUGINS");		

	properties = replaceTextBetween(properties, "#START_PLUGINS", "#END_PLUGINS", "");
	fs.writeFileSync(path.join(TEALEAF_DIR, "project.properties"), properties, "utf-8");

}

