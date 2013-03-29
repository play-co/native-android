var fs = require('fs');
var wrench = require('wrench');
var path = require('path');

var androidPluginDir = process.argv[2];
if (typeof androidPluginDir != "string") {
	console.log("ERROR: no android plugin dir given");
	return;
}

var pathToAndroidXml = process.argv[3];
if (typeof pathToAndroidXml != "string") {
	console.log("ERROR: no path given");
	return;
}

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
var XML_START_PLUGINS_MANIFEST = "<!--START_PLUGINS_MANIFEST-->";
var XML_END_PLUGINS_MANIFEST = "<!--END_PLUGINS_MANIFEST-->";
var XML_START_PLUGINS_APPLICATION = "<!--START_PLUGINS_APPLICATION-->";
var XML_END_PLUGINS_APPLICATION = "<!--END_PLUGINS_APPLICATION-->";
var PROP_START_PLUGINS = "#START_PLUGINS";
var PROP_END_PLUGINS = "#END_PLUGINS";

//read config
var config = JSON.parse(fs.readFileSync(androidPluginDir + "/config.json"));

//set up blank strs for injection of xml
var manifestXmlManifestStr = "";
var manifestXmlApplicationStr = "";

for (var i in config) {

	var pluginDir = path.resolve(androidPluginDir, config[i]);
	var pluginConfig = JSON.parse(fs.readFileSync(path.join(pluginDir, "config.json")));

	//collect plugins xml text to replace and inject after going through all plugins
	if (pluginConfig.injectionXML) {
		var pluginXml = fs.readFileSync(path.join(pluginDir, pluginConfig.injectionXML.srcPath, pluginConfig.injectionXML.name), "utf-8");
		manifestXmlManifestStr += getTextBetween(pluginXml, XML_START_PLUGINS_MANIFEST,	XML_END_PLUGINS_MANIFEST);
		manifestXmlApplicationStr += getTextBetween(pluginXml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION);
	}
}

//inject and write all the collected plugin xml to AndroidManifest.xml
var xml = fs.readFileSync(pathToAndroidXml, "utf-8");
if (xml.length > 0) {
	xml = replaceTextBetween(xml, XML_START_PLUGINS_MANIFEST, XML_END_PLUGINS_MANIFEST, manifestXmlManifestStr);
	xml = replaceTextBetween(xml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION, manifestXmlApplicationStr);
	fs.writeFileSync(pathToAndroidXml, xml, "utf-8");
}

