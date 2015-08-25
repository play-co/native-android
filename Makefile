MODE = release
GPROF_FLAG = 0
RELEASE_FLAG = 0
PROFILE_FLAG = 0
V8_SYMBOLS = 0

# Enable DEBUG mode and JavaScript profiling but not Gprof
debug: GPROF_FLAG = 0
debug: RELEASE_FLAG = 0
debug: PROFILE_FLAG = 1
debug: MODE = debug
debug: all

# This version links with libv8_g.a to allow for better tracing of V8-related crashes
v8symbols: V8_SYMBOLS = 1
v8symbols: debug

# Enable RELEASE mode and Gprof but not JavaScript profiling
gprof: GPROF_FLAG = 1
gprof: RELEASE_FLAG = 1
gprof: PROFILE_FLAG = 0
gprof: MODE = release
gprof: all

# Enable RELEASE mode and Gprof and also JavaScript profiling
jsprof: GPROF_FLAG = 1
jsprof: RELEASE_FLAG = 1
jsprof: PROFILE_FLAG = 1
jsprof: MODE = release
jsprof: all

# Enable RELEASE mode but not Gprof or JavaScript profiling
release: GPROF_FLAG = 0
release: RELEASE_FLAG = 1
release: PROFILE_FLAG = 0
release: MODE = release
release: all

# update the proper projects / subprojects, and update any plugin projects
# this will also build the native code and TeaLeaf
all:
	android update project -p TeaLeaf --target android-19 --subprojects
	android update project -p GCTestApp --target android-19 --subprojects
	ndk-build -C TeaLeaf -Bj8 RELEASE=$(RELEASE_FLAG) JSPROF=$(PROFILE_FLAG) GPROF=${GPROF_FLAG} V8SYMBOLS=${V8_SYMBOLS}
	ant -f TeaLeaf/build.xml $(MODE)


# install plugins for the test app (uninstalling them from Tealeaf)
# then build and install the test app
install:
	adb uninstall com.tealeaf.test_app
	cp GCTestApp/AndroidManifest.xml GCTestApp/.AndroidManifest.xml
	ant -f GCTestApp/build.xml debug
	ant -f GCTestApp/build.xml installd
	cp GCTestApp/.AndroidManifest.xml GCTestApp/AndroidManifest.xml
	adb shell am start -n com.tealeaf.test_app/com.tealeaf.test_app.TestAppActivity


# cleans the  TeaLeaf native code as well as supporting project
# also cleans plugins
clean:
	ndk-build -C TeaLeaf clean
	ant -f TeaLeaf/build.xml clean
	ant -f GCTestApp/build.xml clean

analyze:
	./scripts/analyze.sh

test:
	bash ./scripts/run_tests.sh

#updates requried projects and plugins
setup:
	node checkSymlinks
	android update project -p TeaLeaf --target android-19 --subprojects
	android update project -p GCTestApp --target android-19 --subprojects
