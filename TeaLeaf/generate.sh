#!/bin/sh

cd ../barista/
npm install
cd ../TeaLeaf/
../barista/bin/barista -e v8 -o jni/gen/ jni/core/templates/*.json

