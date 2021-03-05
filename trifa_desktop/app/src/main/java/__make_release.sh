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
  i18n \
  \
  bridj-0.7.0.jar \
  slf4j-api-1.7.2.jar \
  webcam-capture-0.3.12.jar \
  sqlite-jdbc-3.32.3.2.jar \
  flatlaf-1.0.jar \
  \
  jni-c-toxcore.dll \
  libjni-c-toxcore.jnilib \
  libjni-c-toxcore.so \
  \
  do_compile.bat \
  do_compile.sh \
  do_run.bat \
  do_run.sh \
  \
  \
  \
  release/




