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
var XML_START_PLUGINS_MANIFEST = "<!--START_PLUGINS_MANIFEST-->";
var XML_END_PLUGINS_MANIFEST = "<!--END_PLUGINS_MANIFEST-->";
var XML_START_PLUGINS_APPLICATION = "<!--START_PLUGINS_APPLICATION-->";
var XML_END_PLUGINS_APPLICATION = "<!--END_PLUGINS_APPLICATION-->";
var PROP_START_PLUGINS = "#START_PLUGINS";
var PROP_END_PLUGINS = "#END_PLUGINS";

//read config
var config = JSON.parse(fs.readFileSync(__dirname + "/config.json"));

//set up blank strs for injection of xml
var manifestXmlManifestStr = "";
var manifestXmlApplicationStr = "";
var libraries = [];
var hasBilling = false;

for (var c in config) {

	var pluginDir = path.resolve(__dirname, config[c]);
	var pluginConfig = JSON.parse(fs.readFileSync(path.join(pluginDir, "/config.json")));


	//copy to the correct place
	var copyFiles = pluginConfig.copyFiles;
	for (var cf in copyFiles) {
		var fileInfo = copyFiles[cf];
		var packageDir = fileInfo.packageName.replace(/\./g, "/");
		var destFilePath = path.join(__dirname, "../TeaLeaf/src/" ,packageDir ,fileInfo.name);
		wrench.mkdirSyncRecursive(path.dirname(destFilePath));
		copyFileSync(path.join(pluginDir, fileInfo.srcPath,  fileInfo.name), destFilePath, "utf-8")
	}

	if (pluginConfig.hasBilling && pluginConfig.hasBilling == true) {
		hasBilling = true;
	}


	//collect plugins xml text to replace and inject after going through all plugins
	var pluginXml = fs.readFileSync(path.join(pluginDir, pluginConfig.injectionXML.srcPath, pluginConfig.injectionXML.name), "utf-8");
	manifestXmlManifestStr += getTextBetween(pluginXml, XML_START_PLUGINS_MANIFEST,	XML_END_PLUGINS_MANIFEST);
	manifestXmlApplicationStr += getTextBetween(pluginXml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION);


	//collect library names to inject / add later
	libraries.push(path.join(pluginDir, pluginConfig.library.srcPath, pluginConfig.library.libName));

}

//inject and write all the collected plugin xml to AndroidManifest.xml
var xml = fs.readFileSync(path.join(__dirname, "../GCTestApp/AndroidManifest.xml"), "utf-8");
if (xml.length > 0) {
	xml = replaceTextBetween(xml, XML_START_PLUGINS_MANIFEST, XML_END_PLUGINS_MANIFEST, manifestXmlManifestStr);
	xml = replaceTextBetween(xml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION, manifestXmlApplicationStr);
	fs.writeFileSync(path.join(__dirname, "../GCTestApp/AndroidManifest.xml"), xml, "utf-8");
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

}

//remove plugins from tealeaf androidmanifest
var xml = fs.readFileSync(path.join(__dirname, "/../TeaLeaf/AndroidManifest.xml"), "utf-8");
if (xml.length > 0) {
	xml = replaceTextBetween(xml, XML_START_PLUGINS_MANIFEST, XML_END_PLUGINS_MANIFEST, "");
	xml = replaceTextBetween(xml, XML_START_PLUGINS_APPLICATION, XML_END_PLUGINS_APPLICATION, "");
	fs.writeFileSync(path.join(__dirname, "../TeaLeaf/AndroidManifest.xml"), xml, "utf-8");
}
