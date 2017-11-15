#! /bin/bash

cd $(dirname "$0")
export _HOME_=$(pwd)
echo $_HOME_


export _SRC_=$_HOME_/src/
export _INST_=$_HOME_/inst/

export LD_LIBRARY_PATH=$_INST_/lib/

cd java_toxclient_example/java_ref_client/app/src/main/java/
bash do_run.sh
