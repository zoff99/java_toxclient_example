#! /bin/bash

cd $(dirname "$0")
export _HOME_=$(pwd)
echo $_HOME_


export _SRC_=$_HOME_/src/
export _INST_=$_HOME_/inst/

export LD_LIBRARY_PATH=$_INST_/lib/

cp -av java_toxclient_example/jni-c-toxcore/libjni-c-toxcore.so \
  java_toxclient_example/java_ref_client/app/src/main/java/
cd java_toxclient_example/java_ref_client/app/src/main/java/
bash do_compile.sh
