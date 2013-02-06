if [ "x${SDKDIR}" = "x" ]; then
	echo "You need to set the SDKDIR environment variable in order to use this script! Nothing will work until you do."
fi

if [ "x${NDKDIR}" = "x" ]; then
	echo "You need to set the NDKDIR environment variable in order to use this script! Nothing will work until you do."
fi

if [ "x${GCSDKDIR}" = "x" ]; then
	echo "You need to set the GCSDKDIR environment variable in order to use this script! Nothing will work until you do."
fi

PATH="${SDKDIR}/platform-tools:${SDKDIR}/tools:${NDKDIR}:${PATH}"

function readmanifest () {
	local source
	source=$(cat <<END
import json,sys;
o = json.load(open(sys.argv[1]));
try:
	exec("print(o['" + "']['".join(sys.argv[2].split('.')) + "'])");
except KeyError:
	pass
END
)
	echo `python -c "$source" manifest.json "$1"`
}

function serve () {
	if [ ! -e "manifest.json" ]; then echo "You must be inside a project to do that!"; return -1; fi

	source ${GCSDKDIR}/gc_env/bin/activate
	tealeaf serve
	deactivate
	return 0
}

function deploy () {
	local studio
	local debug

	if [ ! -e "manifest.json" ]; then echo "You must be inside a project to do that!"; return -1; fi

	source "${GCSDKDIR}/gc_env/bin/activate"

	tealeaf deploy --target android $*
	if [ $? -ne 0 ];  then
		echo "Deploy failed!"
		return -1
	fi
	deactivate
	return 0
}

function debug () {
	if [ ! -e "AndroidManifest.xml" ]; then echo "You must be inside an Android project directory to do that!"; return -1; fi

	rm -rf libs jni obj
	for dir in jni obj libs; do
		ln -s "${ANDROIDDIR}/TeaLeaf/${dir}" "${dir}"
	done
	ndk-gdb --start --force
	rm -rf libs jni obj
}

function install () {
	local apk
	local shortname

	if [ ! -e "manifest.json" ]; then echo "You must be inside a project to do that!"; return -1; fi

	# does the apk exist already?
	shortname=`readmanifest shortName`
	apk="build/${shortname}.apk"
	if [ ! -e "${apk}" ]; then
		# nope, run deploy
		deploy
		if [ $? -ne 0 ]; then
			echo "Install failed!"
			return -1
		fi
	fi
	adb install ${apk}
	return $?
}

function uninstall () {
	local pkg

	if [ ! -e "manifest.json" ]; then echo "You must be inside a project to do that!"; return -1; fi

	# TODO support other studio names
	shortname=`readmanifest shortName`
	pkg="cat.wee.${pkg}"

	adb uninstall ${pkg}
	return $?
}

function reinstall () {
	uninstall
	install
}
