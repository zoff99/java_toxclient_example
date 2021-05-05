#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

echo $_HOME_
cd $_HOME_

mkdir -p release/
cd release/ && rm -Rf *

cd $_HOME_

cp -a \
  assets \
  com \
  org \
  io \
  i18n \
  \
  json-20210307.jar \
  emoji-java-5.1.1.jar \
  bridj-0.7.0.jar \
  slf4j-api-1.7.2.jar \
  webcam-capture-0.3.12.jar \
  sqlite-jdbc-*.jar \
  flatlaf-1.0.jar \
  \
  jni-c-toxcore.dll \
  libjni-c-toxcore.jnilib* \
  libjni-c-toxcore.so \
  \
  do_compile.bat \
  do_compile.sh \
  do_run.bat \
  do_run.sh \
  \
  trifa.desktop \
  trifa_icon.png \
  \
  release/ || exit 1


cd release && zip -r ../releases/trifa_desktop_1.0.xx.zip *

