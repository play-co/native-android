#!/usr/bin/env bash

NDK_MESSAGE="\033[1;31mERROR\033[0m: You must install android and android-ndk. \n\
Then, return to where you installed native-android, and run ./install.sh"

command -v android >/dev/null 2>&1 || { echo -e $NDK_MESSAGE; exit 1; }
command -v ndk-build >/dev/null 2>&1 || { echo -e $NDK_MESSAGE; exit 1; }

git submodule init

remoteurl=`git config --get remote.origin.url`

if [[ "$remoteurl" == *native-android-priv* ]]
then
	cd native-core
	git remote set-url origin "https://github.com/gameclosure/native-core-priv.git"
	cd ../
fi

npm install
git submodule update --init --recursive
make setup
make clean
