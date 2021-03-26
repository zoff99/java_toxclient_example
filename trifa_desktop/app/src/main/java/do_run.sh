#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

echo $_HOME_
cd $_HOME_

if [ "$1""x" != "x" ]; then
    scale='-Dsun.java2d.uiScale='"$1"
else
    scale=""
fi

java $scale \
-Dcom.apple.mrj.application.apple.menu.about.name=TRIfA \
-Djava.library.path="." \
-classpath ".:sqlite-jdbc-3.32.3.2.jar:webcam-capture-0.3.12.jar:bridj-0.7.0.jar:slf4j-api-1.7.2.jar:flatlaf-1.0.jar" \
com.zoffcc.applications.trifa.MainActivity 2>&1 | tee trifa.log
