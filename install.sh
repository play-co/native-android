#!/usr/bin/env bash

NDK_MESSAGE="\033[1;31mERROR\033[0m: You must install android and android-ndk. \n\
Then, return to where you installed native-android, and run ./install.sh"

command -v android >/dev/null 2>&1 || { echo -e $NDK_MESSAGE; exit 1; }
command -v ndk-build >/dev/null 2>&1 || { echo -e $NDK_MESSAGE; exit 1; }

remoteurl=`git config --get remote.origin.url`

PRIV_SUBMODS=false && [[ "$remoteurl" == *native-android-priv* ]] && PRIV_SUBMODS=true

if $PRIV_SUBMODS; then
	echo "Using private submodules..."
	cp .gitmodules.priv .gitmodules
fi

if ! git submodule sync; then
	error "Unable to sync git submodules"
	exit 1
fi

git submodule update --init --recursive

if $PRIV_SUBMODS; then
	git checkout .gitmodules
fi

npm install
git submodule update --init --recursive
make setup
make clean
