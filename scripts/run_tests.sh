#!/bin/bash
root_dir=`pwd`
jni_dir=$root_dir/TeaLeaf/jni
android_test_dir=$root_dir/AndroidTest
test_app_dir=$root_dir/AndroidTestApp

cmds=("cd $jni_dir"
	"ndk-build"
	"cd $root_dir"
	"android update project --subprojects --target android-19 -p ."
	"cd $root_dir/social-android"
	"android update project --subprojects --target android-19 -p ."
	"cd $test_app_dir"
	"ant clean"
	"ant debug"
	"ant installd"
	"cd $android_test_dir"
	"ant clean"
	"ant debug"
	"ant installd"
	"adb shell am instrument -w com.tealeaf.test/android.test.InstrumentationTestRunner  | tee output.txt")

for num in ${!cmds[*]}; do
	cmd=${cmds[num]}
	eval $cmd
	if [ $? != 0 ]; then
		exit 1
	fi
done

# can't get return code from adb shell - lame solution
grep "FAILURE" output.txt
if [ $? == 0 ]; then
	exit 1
fi
rm output.txt

