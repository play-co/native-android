#!/usr/bin/env bash

# npm preinstall hook for configuring submodules

NDK_MESSAGE="\033[1;31mERROR\033[0m: You must install android and android-ndk first"

#if [[ `uname` == MINGW32* ]]; then
#	command -v android.bat >/dev/null 2>&1 || { echo -e $NDK_MESSAGE; exit 1; }
#else
#	command -v android >/dev/null 2>&1 || { echo -e $NDK_MESSAGE; exit 1; }
#fi
#command -v ndk-build >/dev/null 2>&1 || { echo -e $NDK_MESSAGE; exit 1; }

remoteurl=`git config --get remote.origin.url`

node scripts/submodules.js

if ! git submodule sync; then
	error "Unable to sync git submodules"
	exit 1
fi

git submodule update --init --recursive

