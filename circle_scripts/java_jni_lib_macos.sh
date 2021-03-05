#! /bin/bash

_HOME_="$(pwd)"
export _HOME_

id -a
pwd
ls -al

export _SRC_=$_HOME_/src/
export _INST_=$_HOME_/inst/


mkdir -p $_SRC_
mkdir -p $_INST_

export LD_LIBRARY_PATH=$_INST_/lib/
export PKG_CONFIG_PATH=$_INST_/lib/pkgconfig


# ----------- config ------------
ORIGPATH=$PATH
export ORIGPATH
NEWPATH=$PATH # /usr/x86_64-w64-mingw32/bin:$PATH
export NEWPATH
export PATH=$NEWPATH

MAKEFLAGS=j$(nproc)
export MAKEFLAGS

WGET_OPTIONS="--timeout=10"
export WGET_OPTIONS

# ----------- config ------------

echo "--------------"
ls -al $_INST_/lib/libtoxcore.a
echo "--------------"


## ---------------------------
mkdir -p /root/work/
cd /root/work/
git clone https://github.com/zoff99/ToxAndroidRefImpl
cd /root/work/ToxAndroidRefImpl/jni-c-toxcore/
pwd
ls -al
## ---------------------------

echo "JAVADIR1------------------"
find /usr -name 'jni.h'
echo "JAVADIR1------------------"

echo "JAVADIR2------------------"
find /usr -name 'jni_md.h'
echo "JAVADIR2------------------"

dirname $(find /usr -name 'jni.h' 2>/dev/null|grep -v 'libavcodec'|head -1) > /tmp/xx1
dirname $(find /usr -name 'jni_md.h' 2>/dev/null|head -1) > /tmp/xx2
export JAVADIR1=$(cat /tmp/xx1)
export JAVADIR2=$(cat /tmp/xx2)
echo "JAVADIR1:""$JAVADIR1"
echo "JAVADIR2:""$JAVADIR2"

export CFLAGS=" -fPIC -std=gnu99 -I$_INST_/include/ -L$_INST_/lib -fstack-protector-all "

gcc $CFLAGS \
-Wall \
-DJAVA_LINUX \
$C_FLAGS $CXX_FLAGS $LD_FLAGS \
-D_FILE_OFFSET_BITS=64 -D__USE_GNU=1 \
-I$JAVADIR1/ \
-I$JAVADIR2/ \
jni-c-toxcore.c \
$_INST_/lib/libtoxcore.a \
$_INST_/lib/libtoxav.a \
$_INST_/lib/libtoxencryptsave.a \
$_INST_/lib/libavcodec.a \
$_INST_/lib/libavdevice.a \
$_INST_/lib/libavformat.a \
$_INST_/lib/libavutil.a \
$_INST_/lib/libopus.a \
$_INST_/lib/libvpx.a \
$_INST_/lib/libx264.a \
$_INST_/lib/libtoxav.a \
$_INST_/lib/libtoxcore.a \
$_INST_/lib/libtoxencryptsave.a \
$_INST_/lib/libsodium.a \
-lpthread \
-lm \
-shared \
-o libjni-c-toxcore.jnilib || exit 1

ls -al libjni-c-toxcore.jnilib || exit 1

ldd libjni-c-toxcore.jnilib
pwd
file libjni-c-toxcore.jnilib
cp -a libjni-c-toxcore.jnilib /workspace/data/java_ref_client/app/src/main/java/ || exit 1

# -------------- now compile the JNI lib ----------------------

# --------- compile java example ---------
cd /workspace/data/java_ref_client/app/src/main/java/
javac com/zoffcc/applications/trifa/ToxVars.java
javac com/zoffcc/applications/trifa/TRIFAGlobals.java
javac com/zoffcc/applications/trifa/MainActivity.java
javac com/zoffcc/applications/trifa/TrifaToxService.java
# --------- package java example ---------
cd /workspace/data/java_ref_client/app/src/main/java/
tar -cvf /artefacts/install_macos.tar com *.sh *.jnilib || tar -cvf ~/work/artefacts/install_macos.tar com *.sh *.jnilib
# --------- run test java application ---------
java -Djava.library.path="." com.zoffcc.applications.trifa.MainActivity > trifa.log 2>&1 &
# --------- run test java application ---------
sleep 10
cat ./trifa.log|head -20
echo
echo
cat ./trifa.log | grep 'MyToxID:' | cut -d':' -f 3
echo
echo
sleep 40

