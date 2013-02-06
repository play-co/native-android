var sys = require('sys')
var exec = require('child_process').exec;
var fs = require('fs');
var path = require('path');

//read config
var config = JSON.parse(fs.readFileSync(path.join(__dirname, 'config.json')));

for (var i in config) {
	var pluginDir = path.resolve(__dirname, config[i]);
	var pluginConfig = JSON.parse(fs.readFileSync(path.join(pluginDir, "/config.json")));

	if (pluginConfig.library) {
		var libDir = path.join(pluginDir, pluginConfig.library.srcPath, pluginConfig.library.libName);
		var buildXml = path.join(libDir, "build.xml");
		exec('ant -f ' + buildXml + '  clean', function (error, stdout, stderr) {
			sys.print('stdout: ' + stdout);
			sys.print('stderr: ' + stderr);
			if (error !== null) {
				console.log('exec error: ' + error);
			}
		});

	}
}

