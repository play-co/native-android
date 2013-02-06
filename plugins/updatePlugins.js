var sys = require('sys')
var exec = require('child_process').exec;
var fs = require('fs');
var path = require('path');

//read config
var config = JSON.parse(fs.readFileSync(__dirname + '/config.json'));

for (var c in config) {
	var pluginDir = path.resolve(__dirname, config[c]);
	var pluginConfig = JSON.parse(fs.readFileSync(path.join(pluginDir, "config.json")));
		
	var libDir = path.join(pluginDir, pluginConfig.library.srcPath, pluginConfig.library.libName);
	exec('android update project  --target android-15 --subprojects -p ' + libDir, function (error, stdout, stderr) {
		sys.print('stdout: ' + stdout);
		sys.print('stderr: ' + stderr);
		if (error !== null) {
			console.log('exec error: ' + error);
		}
	});

}

