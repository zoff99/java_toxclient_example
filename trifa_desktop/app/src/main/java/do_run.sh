#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

echo $_HOME_
cd $_HOME_

lf=""

if [ "$1""x" == "sysx" ]; then
  lf="sys"
else
  if [ "$1""x" != "x" ]; then
      scale='-Dsun.java2d.uiScale='"$1"
      if [ "$2""x" == "sysx" ]; then
        lf="sys"
      fi
  else
      scale=""
  fi
fi

# https://stackoverflow.com/a/53351433
# https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8214083
# https://bugs.openjdk.java.net/browse/JDK-8225571
# GTK2 vs. GTK3

# -Djdk.gtk.version=2 \

java $scale \
-Dcom.apple.mrj.application.apple.menu.about.name=TRIfA \
-Djava.library.path="." \
-classpath ".:json-20210307.jar:emoji-java-5.1.1.jar:sqlite-jdbc-3.34.0.jar:webcam-capture-0.3.12.jar:bridj-0.7.0.jar:slf4j-api-1.7.2.jar:flatlaf-1.0.jar" \
com.zoffcc.applications.trifa.MainActivity $lf 2>&1 | tee trifa.log
