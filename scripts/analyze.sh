#!/bin/sh

which crystax-ndk-build >/dev/null
IN_PATH=$?
NDK_BUILD=crystax-ndk-build && [[ $IN_PATH != 0 ]] &&  NDK_BUILD="$CRYSTAX_NDK_ROOT/ndk-build"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

UNAME=`uname`

NUM_CPUS=1
if [[ $UNAME == 'Darwin' ]]; then
	NUM_CPUS=`sysctl -n hw.ncpu`
elif [[ $UNAME == 'Linux' ]]; then
	NUM_CPUS=`grep -c ^processor /proc/cpuinfo`
fi

cd "$DIR/../TeaLeaf" >/dev/null
$NDK_BUILD clean
$NDK_BUILD -j$NUM_CPUS ANALYZE=1
STATUS=$?
cd - >/dev/null
exit $STATUS
